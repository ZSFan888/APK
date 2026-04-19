# 网页打包APK

> 填写网址和基本信息，3~5 分钟自动生成可安装的 Android APK 文件。
> 无需本地安装任何工具，全程在云端完成编译、签名、打包。

**在线体验：** https://apk-c1m.pages.dev

---

## 功能特性

- 全自动构建 — 提交表单触发 GitHub Actions，自动完成编译 → 签名 → 打包
- 全屏 WebView — 沉浸式全屏显示，iOS 风格加载动画，进度条颜色跟随网页主题色
- Release 签名 — 自动使用 Keystore 签名，可直接侧载安装，支持版本升级
- 多架构输出 — 同时生成 `arm64-v8a` 和 `armeabi-v7a` 两个 APK，体积约 4MB
- 支持任意网址 — HTTP / HTTPS 均可，支持 Cookie 持久化、文件上传、摄像头权限
- Splash Screen — 带 App 图标的启动页，800ms 后进入主界面
- 表单记忆 — 自动记住上次填写内容，下次打开直接复用
- 打包历史 — 最近 5 次打包记录，一键复用参数

---

## 项目结构

```
APK/
├── .github/workflows/
│   ├── build.yml          # 主构建流程
│   └── gen-keystore.yml   # 生成签名 Keystore
├── Frontend/
│   └── index.html         # 前端页面（部署到 Cloudflare Pages）
├── Worker/
│   ├── worker.js          # Cloudflare Worker API
│   └── wrangler.toml      # Worker 配置
├── Scripts/
│   └── process_icon.py    # 图标处理脚本
├── app/                   # Android 项目源码
│   └── src/main/java/com/webviewapp/
│       ├── MainActivity.kt
│       ├── SplashActivity.kt
│       ├── TopProgressBar.kt
│       └── IOSSpinnerView.kt
├── build.gradle
└── settings.gradle
```

---

## 部署流程

### 前置要求

- GitHub 账号（用于 Actions 构建）
- Cloudflare 账号（用于 Pages + Worker）

---

### 第一步 — Fork 仓库

点击右上角 **Fork**，将本仓库 Fork 到你自己的账号下。

---

### 第二步 — 生成签名 Keystore

进入你 Fork 的仓库 → **Actions** → **gen-keystore** → **Run workflow**

运行后在 Actions 日志中复制输出的 Base64 Keystore 字符串，备用。

---

### 第三步 — 配置 GitHub Secrets

进入仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

依次添加以下 Secret：

| Secret 名称         | 说明                                      |
|---------------------|-------------------------------------------|
| `KEYSTORE_BASE64`   | 上一步输出的 Base64 Keystore 字符串        |
| `KEYSTORE_PASSWORD` | Keystore 密码（gen-keystore 时设置的）     |
| `KEY_ALIAS`         | Key 别名（默认 `release`）                 |
| `KEY_PASSWORD`      | Key 密码（同 Keystore 密码）               |
| `GH_PAT`            | GitHub Personal Access Token（需要 `repo` + `workflow` 权限） |

---

### 第四步 — 部署 Cloudflare Worker

Worker 负责接收前端请求、触发 GitHub Actions、查询构建状态、转发下载链接。

#### 方案 A：Dashboard 部署（推荐，无需安装任何工具）

1. 打开 [Cloudflare Dashboard](https://dash.cloudflare.com) → **Workers & Pages** → **Create** → **Worker**
2. 随意取名（如 `apk-builder-api`）→ 点击 **Deploy**
3. 进入刚创建的 Worker → **Edit Code** → 将 `Worker/worker.js` 全部内容粘贴进去 → 点击右上角 **Deploy**
4. 回到 Worker 主页 → **Settings** → **Variables and Secrets**，添加以下变量：

   | 变量名           | 值                  | 类型   |
   |------------------|---------------------|--------|
   | `GITHUB_OWNER`   | 你的 GitHub 用户名   | Text   |
   | `GITHUB_REPO`    | `APK`               | Text   |
   | `ALLOWED_ORIGIN` | `*`                 | Text   |
   | `GH_PAT`         | 你的 GitHub PAT      | Secret |

5. 记录 Worker URL：`https://apk-builder-api.<你的子域>.workers.dev`

#### 方案 B：Wrangler CLI 部署

1. 安装 Wrangler 并登录：

   ```bash
   npm install -g wrangler
   wrangler login
   ```

2. 编辑 `Worker/wrangler.toml`，修改为你自己的 GitHub 信息：

   ```toml
   [vars]
   GITHUB_OWNER = "你的用户名"
   GITHUB_REPO  = "APK"
   ALLOWED_ORIGIN = "*"
   ```

3. 添加 GitHub PAT（Secret 方式，不写入配置文件）：

   ```bash
   cd Worker
   wrangler secret put GH_PAT
   # 粘贴你的 PAT 回车确认
   ```

4. 部署：

   ```bash
   wrangler deploy
   ```

---

### 第五步 — 部署前端到 Cloudflare Pages

#### 方案 A：连接 Git 仓库（推荐，提交后自动同步更新）

1. 打开 [Cloudflare Dashboard](https://dash.cloudflare.com) → **Workers & Pages** → **Create** → **Pages**
2. 选择 **Connect to Git** → 授权并选择你 Fork 的仓库
3. 填写构建配置：

   | 配置项                  | 值          |
   |-------------------------|-------------|
   | Framework preset        | None        |
   | Build command           | （留空）     |
   | Build output directory  | `Frontend`  |

4. 点击 **Save and Deploy**，等待首次部署完成

#### 方案 B：直接上传文件（无需 Git，快速体验）

1. 打开 **Workers & Pages** → **Create** → **Pages** → **Upload assets**
2. 随意输入项目名称，点击 **Create project**
3. 将 `Frontend/index.html` 拖入上传区域，点击 **Deploy site**

---

### 第六步 — 更新前端 Worker 地址

编辑 `Frontend/index.html` 第一行 JS 常量，改为你自己的 Worker URL：

```js
const WORKER = 'https://apk-builder-api.<你的子域>.workers.dev';
```

提交后 Pages 自动重新部署（方案 A），或手动重新上传（方案 B）。

---

### 第七步 — 验证

打开前端页面，填写测试信息，点击「开始打包」。

构建状态实时更新，3~5 分钟后出现下载按钮，下载 APK 安装到 Android 设备验证效果。

---

## 构建流程

```
提交表单
   │
   ▼
Cloudflare Worker  ──→  触发 GitHub Actions (workflow_dispatch)
   │
   ▼
GitHub Actions
   ├── 1. Inject parameters   注入 URL / 包名 / 版本号等参数
   ├── 2. Process icon        下载图标，生成多尺寸 mipmap
   ├── 3. Build APK           Gradle 编译（arm64 + armeabi-v7a）
   └── 4. Sign & Upload       zipalign + apksigner 签名，上传 Artifact
   │
   ▼
前端轮询 /status 接口，构建完成后展示下载按钮
   │
   ▼
Worker /download 接口  ──→  重定向到 GitHub Artifact 下载链接
```

---

## 注意事项

- APK 文件保留 **7 天**，超期后需重新打包
- 仅供**侧载安装**，不可上架 Google Play
- GitHub Actions 免费账号每月有 **2000 分钟**额度，单次构建约消耗 3~5 分钟
- 如不配置 Keystore Secrets，会自动生成临时 Debug Key 签名，**不同次打包签名不同，无法升级安装**

---

## License

MIT
