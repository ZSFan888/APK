# Introduction

**Pakr** is a one-click website-to-APK packaging tool. After you enter a target URL and app metadata, it automatically generates an installable Android APK in 3–5 minutes. No local Android environment is required because compilation, signing, and packaging all run in the cloud.

## How it works

The frontend and backend are deployed together on **Cloudflare Pages**, so you do not need a separate Worker deployment:

```text
Browser
  │
  ▼
Cloudflare Pages (index.html + _worker.js)
  │  Frontend page + API endpoint merged together
  ▼
GitHub Actions ── trigger build / query progress / download APK
```

## Build pipeline

```text
Submit form
   │
   ▼
_worker.js ──→ Trigger GitHub Actions (workflow_dispatch)
   │
   ▼
GitHub Actions
   ├── 1. Inject parameters   Insert URL, package name, version, and app metadata
   ├── 2. Process icon        Download icon and generate multiple mipmap sizes
   ├── 3. Build APK           Run Gradle build (arm64 + armeabi-v7a)
   └── 4. Sign & Upload       zipalign + apksigner signing, then upload artifacts
   │
   ▼
Frontend polls /status every 5 seconds to update build percentage and current step
   │
   ▼
Build finished → show multi-ABI download buttons and store the result in history
```

## Online resources

- **Live demo:** [apk.091224.xyz](https://apk.091224.xyz)
- **GitHub:** [ZhangShengFan/Pakr](https://github.com/ZhangShengFan/Pakr)
