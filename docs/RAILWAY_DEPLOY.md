# 🚂 Railway 部署手冊（AI Finance）

> **目標**：把 mysql + redis + backend + frontend 四個服務部署到 Railway，前端可訪問、後端可呼叫 DeepSeek。
> **預計時間**：第一次約 30 分鐘，之後每次推 commit 自動 redeploy。

---

## 🧭 整體架構

```
┌─────────────────┐     ┌──────────────────┐     ┌───────────────┐
│ Frontend (Vue)  │────▶│ Backend (Spring) │────▶│ MySQL (plugin)│
│  Nginx + Port   │     │  Java 21 + Port  │     └───────────────┘
└─────────────────┘     └────────┬─────────┘     ┌───────────────┐
                                 └──────────────▶│ Redis (plugin)│
                                                 └───────────────┘
```

---

## 0️⃣ 前置條件

- [x] GitHub repo: `ansonAA02/AIDataAnalysis`
- [x] Railway 帳號已註冊
- [ ] **DeepSeek API Key**（如果還沒有，到 https://platform.deepseek.com/api_keys 申請）

---

## 1️⃣ 建立 Railway Project

1. 登入 https://railway.app
2. 右上角 **New Project** → **Deploy from GitHub repo**
3. 授權 Railway 讀取 `ansonAA02/AIDataAnalysis`
4. **不要**勾選任何 service，先建空 project

---

## 2️⃣ 加 MySQL Plugin

1. Project 內 **+ New** → **Database** → **Add MySQL**
2. 等 plugin 變綠（約 1 分鐘）
3. 點進 MySQL 服務 → **Variables** 標籤，記下：
   - `MYSQL_URL`（這是給內部用的完整 jdbc URL）
   - `MYSQLHOST` / `MYSQLPORT` / `MYSQLDATABASE` / `MYSQLUSER` / `MYSQLPASSWORD`

---

## 3️⃣ 加 Redis Plugin

1. **+ New** → **Database** → **Add Redis**
2. 等 plugin 變綠
3. 記下 `REDIS_URL`

---

## 4️⃣ 部署 Backend Service

1. **+ New** → **GitHub Repo** → 選 `AIDataAnalysis`
2. Service 名字改為 `backend`
3. **Settings** 標籤：
   - **Root Directory**：`backend`
   - **Build Command**：留空（用 Dockerfile）
   - **Start Command**：留空（Dockerfile 已定義 ENTRYPOINT）
   - **Healthcheck Path**：`/api/health`
4. **Variables** 標籤，貼入以下變數（注意 `${{MySQL.XXX}}` 是 Railway 模板語法，會自動替換）：

```bash
# JDBC 連線（使用 Railway 內網）
SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci
SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}
SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}

# Redis（取連線資訊）
SPRING_DATA_REDIS_HOST=${{Redis.REDISHOST}}
SPRING_DATA_REDIS_PORT=${{Redis.REDISPORT}}
SPRING_DATA_REDIS_PASSWORD=${{Redis.REDISPASSWORD}}

# DeepSeek（你自己的 key）
DEEPSEEK_API_KEY=sk-你的key
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat
DEEPSEEK_REASONING_EFFORT=high
DEEPSEEK_THINKING_ENABLED=true

# CORS（先填 *，等前端部署完拿到網域後改成精確值）
APP_CORS_ALLOWED_ORIGINS=*

# JVM tuning（Railway free tier 記憶體有限）
JAVA_TOOL_OPTIONS=-Xmx400m -Xss256k
```

5. **Settings** → **Networking** → **Generate Domain**
   - 拿到 `backend-production-xxxx.up.railway.app`

---

## 5️⃣ 部署 Frontend Service

1. **+ New** → **GitHub Repo** → 選同一個 repo（**注意：是同 repo 第二次加 service**）
2. Service 名字改為 `frontend`
3. **Settings**：
   - **Root Directory**：`frontend`
4. **Variables**：

```bash
# 把後端剛剛拿到的網域填這裡（不含 https://）
VITE_API_BASE_URL=https://backend-production-xxxx.up.railway.app
```

⚠️ **重要**：因為 Vue SPA 是 build-time 注入，改了這個變數**必須點 Redeploy**才生效。

5. **Settings** → **Networking** → **Generate Domain**
   - 拿到 `frontend-production-yyyy.up.railway.app`

---

## 6️⃣ 回頭修 Backend CORS

1. 回到 backend service → **Variables**
2. 修改 `APP_CORS_ALLOWED_ORIGINS`：

```bash
APP_CORS_ALLOWED_ORIGINS=https://frontend-production-yyyy.up.railway.app
```

3. backend 自動 redeploy

---

## 7️⃣ 驗證部署

依序在瀏覽器訪問：

| 檢查項 | URL | 期望 |
|---|---|---|
| 後端健康 | `https://backend-prod-xxxx.up.railway.app/api/health` | `{"status":"ok"}` |
| 後端期間 API | `https://backend-prod-xxxx.up.railway.app/api/finance/periods` | JSON 陣列 12 筆 |
| 前端首頁 | `https://frontend-prod-yyyy.up.railway.app/` | 看到 Dashboard |
| 前端 → 後端 | F12 → Network → 看 XHR 請求是否 200 | 無 CORS 錯誤 |

---

## 🛠 常見坑與排查

### 坑 1：後端啟動失敗，log 顯示 `OutOfMemoryError`
**原因**：Railway free tier 預設 512MB RAM，Spring Boot + JVM 默認配置吃太多。
**解法**：已在 ENV 中加 `JAVA_TOOL_OPTIONS=-Xmx400m -Xss256k`，若仍失敗再降到 `-Xmx300m`。

### 坑 2：前端 build 後 API 還是打 localhost:8080
**原因**：`VITE_API_BASE_URL` 是 build-time 變數，改了沒 redeploy。
**解法**：frontend service → Settings → Redeploy 按鈕。

### 坑 3：MySQL Connection Refused
**原因**：用了 `MYSQL_URL`（公開 URL）而非內網 host，內網才不收費也更快。
**解法**：用本手冊步驟 4 給的 `${{MySQL.MYSQLHOST}}` 模板。

### 坑 4：CORS preflight 403
**原因**：`APP_CORS_ALLOWED_ORIGINS` 還是 `*`，但 Spring Security 對 credentialed 請求不允許 `*`。
**解法**：填精確網域（含 `https://`，不含尾斜線）。

### 坑 5：schema.sql 沒跑
**原因**：Spring 預設 `spring.sql.init.mode=embedded`，MySQL 不會自動執行。
**解法**：本專案已寫死 `mode: always`（見 `application.yml`），無需手動。

---

## 💰 成本控制

| 服務 | 預估 RAM | 預估月費（以 $0.000463/GB-min 計） |
|---|---|---|
| MySQL plugin | 256 MB | ~$5 |
| Redis plugin | 64 MB | ~$1 |
| Backend (Java) | 400 MB | ~$8 |
| Frontend (Nginx) | 32 MB | ~$0.5 |
| **合計** | | **~$15/月** |

> Railway 新帳號送 $5 trial credit，能撐約 10 天。要長期跑要綁卡升 Hobby ($5/月 包 $5 額度)。

---

## 🚀 一鍵更新

每次本地 `git push origin main` 後，Railway 會自動：
1. 偵測 commit
2. backend / frontend 各自 rebuild Dockerfile
3. 健康檢查通過後切流量
4. Slack / Email 推送結果

無需手動操作。

---

## 📞 需要我幫忙的時候

如果在 Railway 介面卡住，把以下三項貼給我：
1. 哪個 service 失敗（backend/frontend）
2. **Deployments** 標籤最新一筆的 build log 末 50 行
3. **Variables** 標籤的 keys（**不要貼 value**）
