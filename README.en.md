<div align="center">

# Pakr — One-Click Website to APK Packager

**Enter a website URL and app metadata, then automatically generate an installable Android APK in 3–5 minutes.**  
No local environment required. Compilation, signing, and packaging all run in the cloud.

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Platform-Android-3ddc84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![Cloudflare Pages](https://img.shields.io/badge/Hosted_on-Cloudflare_Pages-f6821f.svg?logo=cloudflare&logoColor=white)](https://pages.cloudflare.com)
[![GitHub Actions](https://img.shields.io/badge/CI-GitHub_Actions-2088ff.svg?logo=githubactions&logoColor=white)](https://github.com/features/actions)

[中文](README.md) · **English**

**[🚀 Live Demo](https://apk.091224.xyz) · [📖 中文文档](https://apk.091224.xyz/Docs) · [📖 English Docs](https://apk.091224.xyz/Docs/en/)**

</div>

---

## Features

| Feature | Description |
|------|------|
| Fully automated build | Triggers GitHub Actions to compile, sign, and package automatically |
| Real-time progress | Shows precise percentage and current build stage |
| Full-screen WebView | Immersive full-screen browsing experience |
| Release signing | Automatically signs with a keystore for upgrade installs |
| Multi-ABI output | Generates both arm64-v8a and armeabi-v7a APKs |
| Any website supported | Supports HTTP / HTTPS, cookies, file upload, and camera permission |
| System downloads | Website downloads are handed off to Android DownloadManager |
| Pull to refresh | Refresh page when pulled down at the top |
| Keyboard adaptation | Page moves up automatically when soft keyboard opens |
| Build history | Saves recent packaging history for quick reuse |
| Dark mode | Follows system theme and also supports manual switching |

---

## Architecture

Frontend and backend are deployed together on **Cloudflare Pages**, without needing a separate Worker deployment.

```text
Browser
  │
  ▼
Cloudflare Pages (index.html + _worker.js)
  │  Frontend page + API merged together
  ▼
GitHub Actions ── trigger build / query progress / download APK
```

---

## Project Structure

```text
Pakr/
├── .github/workflows/
│   ├── build.yml
│   └── gen-keystore.yml
├── Scripts/
│   └── process_icon.py
├── Docs/
│   ├── index.html
│   └── en/index.html
├── index.html
├── _worker.js
└── app/
```

---

## Quick Start

See the Chinese documentation at `/Docs` and the English entry page at `/Docs/en/` for deployment and usage guidance.

1. Fork this repository.
2. Generate a signing keystore in GitHub Actions.
3. Configure GitHub Secrets.
4. Deploy the repository to Cloudflare Pages.
5. Open the website, submit app info, and wait for APK generation.

---

## Notes

- `/Docs` is the default Chinese documentation entry.
- `/Docs/en/` is the English documentation entry.
- App-internal UI is unchanged; this update only adds multilingual project documentation links.

## Documentation Languages

- Default Chinese docs: `/Docs`
- English docs: `/Docs/en/`
- App-internal UI is unchanged in this update; multilingual support currently applies only to repository documentation and the docs website
