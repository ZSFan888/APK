# 网页打包APK

> 填写网址和基本信息，3~5 分钟自动生成可安装的 Android APK 文件。
> 无需本地安装任何工具，全程在云端完成编译、签名、打包。

**在线体验：** https://apk-c1m.pages.dev

---

## 功能特性

- **全自动构建** — 提交表单触发 GitHub Actions，自动完成编译 → 签名 → 打包
- **全屏 WebView** — 沉浸式全屏显示，iOS 风格加载动画，进度条颜色跟随网页主题色
- **Release 签名** — 自动使用 Keystore 签名，可直接侧载安装，支持版本升级
- **多架构输出** — 同时生成 `arm64-v8a` 和 `armeabi-v7a` 两个 APK，体积约 4MB
- **支持任意网址** — HTTP / HTTPS 均可，支持 Cookie 持久化、文件上传、摄像头权限
- **Splash Screen** — 带 App 图标的启动页，800ms 后进入主界面
- **表单记忆** — 自动记住上次填写内容，下次打开直接复用
- **打包历史** — 最近 5 次打包记录，一键复用参数

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
- Node.js（用于 Wrangler CLI）

---

### Step 1 — Fork 仓库

点击右上角 **Fork**，将本仓库 Fork 到你自己的账号下。

---

### Step 2 — 生成签名 Keystore

进入你 Fork 的仓库 → **Actions** → **gen-keystore** → **Run workflow**，运行后会在 Actions 日志中输出 Base64 编码的 Keystore 字符串，复制备用。

---

### Step 3 — 配置 GitHub Secrets

进入仓库 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**，依次添加：

| Secret 名称         | 说明                                      |
|---------------------|-------------------------------------------|
| `KEYSTORE_BASE64`   | Step 2 中输出的 Base64 Keystore 字符串     |
| `KEYSTORE_PASSWORD` | Keystore 密码（gen-keystore 时设置的）     |
| `KEY_ALIAS`         | Key 别名（默认 `release`）                 |
| `KEY_PASSWORD`      | Key 密码（同 Keystore 密码）               |
| `GH_PAT`            | GitHub Personal Access Token（需要 `repo` + `workflow` 权限） |

---

### Step 4 — 部署 Cloudflare Worker

```bash
# 安装 Wrangler
npm install -g wrangler

# 登录 Cloudflare
wrangler login

# 进入 Worker 目录
cd Worker

# 配置 wrangler.toml（修改为你自己的 GitHub 用户名和仓库名）
# GITHUB_OWNER = "你的用户名"
# GITHUB_REPO  = "APK"

# 添加 GitHub PAT（Secrets 方式，不写进配置文件）
wrangler secret put GH_PAT
# 粘贴你的 PAT 回车确认

# 部署 Worker
wrangler deploy
```

部署成功后记录 Worker 的 URL，格式为 `https://apk-builder-api.<你的子域>.workers.dev`。

---

### Step 5 — 部署前端到 Cloudflare Pages

1. 进入 [Cloudflare Dashboard](https://dash.cloudflare.com) → **Workers & Pages** → **Create** → **Pages**
2. 选择 **Connect to Git** → 选择你 Fork 的仓库
3. 构建配置：
   - **Framework preset**：None
   - **Build command**：（留空）
   - **Build output directory**：`Frontend`
4. 点击 **Save and Deploy**

部署完成后，打开 Pages 分配的域名（如 `apk-xxx.pages.dev`）。

---

### Step 6 — 更新前端 Worker 地址

编辑 `Frontend/index.html`，将第一行 JS 常量改为你自己的 Worker URL：

```js
const WORKER = 'https://apk-builder-api.<你的子域>.workers.dev';
```

提交后 Pages 自动重新部署。

---

### Step 7 — 验证

打开前端页面，填写一个测试网址（如 `https://example.com`），点击「开始打包」。

构建状态会实时更新，3~5 分钟后出现下载按钮，下载 APK 安装到 Android 设备验证效果。

---

## 构建流程说明

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
- 如不配置 Keystore Secrets，会自动生成临时 Debug Key 签名，**不同次打包的签名不同**，无法升级安装

---

## License

MIT
