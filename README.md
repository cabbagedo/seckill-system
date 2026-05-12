# 秒杀下单系统 (Seckill System)

> 高并发场景下的资格校验与异步落库系统  
> SpringBoot + Redis + Kafka + MySQL + Lua + Caffeine

---

## 1. 业务背景

模拟电商秒杀场景，核心难点：
- **高并发下资格校验的原子性**（去重 + 库存）
- **库存防超卖**
- **削峰落库**（避免 DB 在瞬时洪峰下崩溃）

---

## 2. 架构概览

```
                    ┌─────────────┐
   Client ──HTTP──▶ │ Controller  │
                    └──────┬──────┘
                           ▼
                ┌──────────────────────┐
                │  SeckillService      │
                │  (Redis+Lua 原子校验) │
                └──────┬───────────────┘
                  ok   │   失败 → 直接拒绝
                       ▼
                ┌──────────────┐
                │ Kafka Topic  │  partition key = productId
                └──────┬───────┘
                       ▼
                ┌──────────────────────┐
                │  OrderConsumer       │
                │  (DB 乐观锁扣减+落单) │
                └──────────────────────┘

商品详情:  Caffeine (L1) ─▶ Redis (L2) ─▶ MySQL
           逻辑过期 + 异步刷新 (防击穿)
```

---

## 3. 技术栈

| 层 | 选型 |
|---|---|
| 框架 | Spring Boot 3.3.4 |
| 缓存 | Redis 7 (Lua) + Caffeine |
| 消息 | Kafka 3.7 |
| 持久化 | MySQL 8 + MyBatis-Plus |
| JDK | 21+ |

---

## 4. 核心设计点

### 4.1 Redis + Lua 原子资格校验
`src/main/resources/lua/seckill.lua` 单脚本完成：
1. 检查商品是否预热（防穿透）
2. SISMEMBER 校验用户是否已抢（去重）
3. 库存对比 + DECRBY 预扣

利用 Redis 单线程执行特性，**不需要分布式锁**就能保证原子性。  
对比 Redisson 锁，去掉了网络往返与锁竞争开销。

### 4.2 Kafka 异步削峰
- 校验通过后投递 `seckill-order` topic
- `partition key = productId` 保证同一商品扣减顺序
- 消费端 ack 手动提交、批量消费

### 4.3 幂等消费
- 订单表 `order_no` 唯一键
- 消费前先 `SELECT BY order_no`，避免重复插入

### 4.4 两级缓存 + 逻辑过期防击穿
- L1 Caffeine（本地，~ns 级）
- L2 Redis（远程，~ms 级）
- 物理永不过期，`LogicalExpireData.expireAt` 判定逻辑过期
- 过期后**异步线程刷新**，请求线程不阻塞

### 4.5 数据库乐观锁防超卖
- 商品表 (`t_product`) 与库存表 (`t_stock`) 分离
- 乐观锁 `version` 字段 + 重试 3 次
- 即使 Redis 与 DB 出现短暂不一致，DB 层仍能兜底

---

## 5. 项目结构

```
seckill-system/
├── pom.xml
├── docker-compose.yml          # 一键拉起 MySQL/Redis/Kafka
├── sql/init.sql                # 建表 + 测试数据
├── src/main/java/com/seckill/
│   ├── SeckillApplication.java
│   ├── config/                 # Redis/Kafka/Caffeine/MybatisPlus 配置
│   ├── controller/             # SeckillController / ProductController / OrderController
│   ├── service/ + impl/        # SeckillService / ProductService / StockService
│   ├── mapper/                 # MyBatis-Plus BaseMapper
│   ├── entity/                 # Product / Stock / Order
│   ├── dto/                    # SeckillRequest / OrderMessage
│   ├── kafka/                  # OrderProducer / OrderConsumer
│   ├── cache/                  # 两级缓存 + 逻辑过期
│   ├── common/                 # Result / 异常处理
│   └── util/                   # OrderIdGenerator
└── src/main/resources/
    ├── application.yml
    └── lua/seckill.lua
```

---

## 6. 快速开始

### 6.1 启动中间件 (推荐 Docker)
```bash
docker compose up -d
# 等待 ~30 秒，确认 mysql/redis/kafka 都健康
docker compose ps
```

**没装 Docker？** 用 Homebrew：
```bash
brew install mysql redis
brew services start mysql
brew services start redis
# Kafka 需自行下载: https://kafka.apache.org/downloads
mysql -uroot -p < sql/init.sql
```

### 6.2 启动应用
```bash
./mvnw spring-boot:run
# 或: mvn spring-boot:run
```

### 6.3 接口示例
```bash
# 1. 预热库存到 Redis (生产应是后台任务)
curl http://localhost:8080/api/product/preload/1001

# 2. 查询商品 (走两级缓存)
curl http://localhost:8080/api/product/1001

# 3. 发起秒杀
curl -X POST http://localhost:8080/api/seckill/do \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"productId":1001,"quantity":1}'

# 4. 查订单 (Kafka 消费完成后可查到)
curl http://localhost:8080/api/order/<orderNo>
```

### 6.4 压测（可选）
```bash
brew install wrk
# 准备 post.lua
cat > post.lua <<'EOF'
wrk.method  = "POST"
wrk.headers["Content-Type"] = "application/json"
request = function()
  local uid = math.random(1, 1000000)
  return wrk.format(nil, nil, nil,
    string.format('{"userId":%d,"productId":1001,"quantity":1}', uid))
end
EOF
wrk -t8 -c200 -d30s -s post.lua http://localhost:8080/api/seckill/do
```

---

## 7. 简历可写指标（基于本机 8C16G 实测后填）

| 指标 | 基线 | 优化后 |
|---|---|---|
| QPS | ~300 | ~3000 |
| P99 (ms) | _压测后填_ | _压测后填_ |
| Redis Lua vs Redisson | — | Lua 高约 X 倍 |
| 缓存命中后 DB QPS 下降 | — | X% |

> 真实数字必须自己压测一遍后再写进简历，面试官会追问压测环境与方法。

---

## 8. License
MIT
