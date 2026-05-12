-- ============================================
-- 秒杀资格校验 Lua 脚本 (Redis 单线程原子执行)
--   KEYS[1] = stock:{productId}        库存键 (value=剩余库存)
--   KEYS[2] = seckill:user:{productId} 已抢用户去重 Set
--   ARGV[1] = userId
--   ARGV[2] = buyCount
-- 返回:
--    1  - 成功
--   -1  - 库存不足
--   -2  - 重复抢购
--   -3  - 商品不存在(未预热)
-- ============================================
if redis.call('exists', KEYS[1]) == 0 then
    return -3
end

if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -2
end

local stock = tonumber(redis.call('get', KEYS[1]))
local buy = tonumber(ARGV[2])
if stock < buy then
    return -1
end

redis.call('decrby', KEYS[1], buy)
redis.call('sadd', KEYS[2], ARGV[1])
return 1
