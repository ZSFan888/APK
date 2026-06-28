# Quick Start

This page shows how to deploy Pakr in a few minutes and start generating APKs.

## Requirements

- **GitHub account** — used for GitHub Actions builds
- **Cloudflare account** — used for Cloudflare Pages hosting

## Step 1: Fork the repository

Click **Fork** in the top-right corner and fork [ZhangShengFan/Pakr](https://github.com/ZhangShengFan/Pakr) to your own account.

## Step 2: Generate a signing keystore

Open your forked repository → **Actions** → **gen-keystore** → **Run workflow**

Enter the password values, run the workflow, and copy the generated **Base64 keystore string** from the Actions log.

## Step 3: Configure GitHub Secrets

Open the repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64 keystore string generated in the previous step |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias, usually `release` |
| `KEY_PASSWORD` | Key password, usually the same as the keystore password |
| `GH_PAT` | GitHub PAT with `repo` and `workflow` scopes |

## Step 4: Deploy to Cloudflare Pages

1. Open [Cloudflare Dashboard](https://dash.cloudflare.com) → **Workers & Pages** → **Create** → **Pages** → **Connect to Git**
2. Select your forked repository and use the following build settings:

   | Option | Value |
   |---|---|
   | Framework preset | None |
   | Build command | Leave empty |
   | Build output directory | `/` |

3. Add these environment variables in **Settings** → **Environment variables**:

   | Variable | Value |
   |---|---|
   | `GITHUB_OWNER` | Your GitHub username |
   | `GITHUB_REPO` | `Pakr` |
   | `GH_PAT` | Your GitHub PAT |

4. Click **Save and Deploy** and wait for deployment to finish.

## Step 5: Verify

Open the Pages domain, fill in test app information, click the build button, and wait about 3–5 minutes to download and install the generated APK.
