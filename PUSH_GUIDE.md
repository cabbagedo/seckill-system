# GitHub Push 完整步骤

> 项目目录: `/Users/cabbagedog/IdeaProjects/seckill-system`  
> 项目已生成完毕,git 仓库已 init,所有文件已 staged.  
> 现在你只需要做 4 步.

---

## 步骤 1: 配置 Git 身份 (一次性,以后不用再做)

```bash
# 用你的真实邮箱(最好是 GitHub 注册邮箱),姓名建议英文
git config --global user.name  "YourName"
git config --global user.email "your_github_email@xxx.com"
```

验证一下:
```bash
git config --get user.name
git config --get user.email
```

---

## 步骤 2: 在 GitHub 网页上新建空仓库

1. 打开 https://github.com/new
2. **Repository name**: `seckill-system` (或你想要的名字)
3. **Description**: `高并发秒杀系统 - SpringBoot + Redis + Kafka + Lua + Caffeine`
4. 选 **Public**(简历项目要让 HR 看得到)
5. **不要勾选** "Initialize this repository with a README"——我们本地已有
6. 点 **Create repository**

创建完后页面会给你一个 URL,长这样:
- HTTPS: `https://github.com/<你的用户名>/seckill-system.git`
- SSH:   `git@github.com:<你的用户名>/seckill-system.git`

---

## 步骤 3: 创建首个 commit + 关联远程仓库 + push

复制下面这块命令,把 `<你的用户名>` 替换成自己的 GitHub 用户名后,**整段粘贴到终端**:

```bash
cd /Users/cabbagedog/IdeaProjects/seckill-system

# 已经 staged, 直接 commit
git commit -m "feat: 高并发秒杀系统(Redis+Lua+Kafka+两级缓存+乐观锁)"

# 关联远程仓库 (HTTPS 方式, 推荐第一次用)
git remote add origin https://github.com/<你的用户名>/seckill-system.git

# 推送, -u 设置默认上游(下次只需 git push)
git push -u origin main
```

第一次 push 会让你输入 GitHub 凭证:
- **Username**: 你的 GitHub 用户名
- **Password**: **不能用网页登录密码!**  必须用 **Personal Access Token (PAT)**

---

## 步骤 4: 生成 GitHub Personal Access Token (PAT)

如果没有 PAT,push 时会被拒绝.生成方式:

1. 浏览器打开: https://github.com/settings/tokens?type=beta
2. 点 **Generate new token (Fine-grained, recommended)**
3. **Token name**: `mac-seckill-push`
4. **Expiration**: 90 天 (或更长)
5. **Repository access**: 选 `Only select repositories` -> 选你刚建的 `seckill-system`
6. **Permissions** -> **Repository permissions**:
   - **Contents**: `Read and write`  ← 必须
   - **Metadata**: `Read-only` (默认勾上)
7. 点 **Generate token**, **立刻复制保存** (页面关掉就再也看不到了)

回到终端 push 时:
- Username 输 GitHub 用户名
- Password **粘贴 PAT** (粘进去看不见字符是正常的,直接回车)

> macOS 输入过一次后,会被 Keychain 缓存,以后免输.

---

## 步骤 5: 验证

push 成功后,刷新 GitHub 仓库页面,应该能看到:
- README.md 渲染出架构图
- 所有源码、pom.xml、sql、docker-compose

简历里的项目链接就填: `https://github.com/<你的用户名>/seckill-system`

---

## 常见问题

### Q1: push 时报 `Permission denied (publickey)`
你用的是 SSH URL 但没配 SSH key.切回 HTTPS:
```bash
git remote set-url origin https://github.com/<你的用户名>/seckill-system.git
```

### Q2: push 时报 `Updates were rejected`
说明远端不是空的(误勾了 init README).强制覆盖:
```bash
git push -u origin main --force
```
**仅在远端确实是你自己刚建的空仓库时用 --force.**

### Q3: 想用 SSH (推荐长期使用)
```bash
# 生成 key
ssh-keygen -t ed25519 -C "your_github_email@xxx.com"
# 一路回车

# 复制公钥, 在 https://github.com/settings/keys 粘贴
cat ~/.ssh/id_ed25519.pub

# 切换 remote 到 SSH
cd /Users/cabbagedog/IdeaProjects/seckill-system
git remote set-url origin git@github.com:<你的用户名>/seckill-system.git
```

### Q4: 之后改了代码怎么再 push?
```bash
git add -A
git commit -m "改动说明"
git push
```
