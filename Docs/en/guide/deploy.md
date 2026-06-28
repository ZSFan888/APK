# Deployment Notes

## Important notes

- GitHub Actions free accounts usually include **2000 minutes per month**, and each build typically consumes about **3–5 minutes**.
- If keystore secrets are not configured, Pakr falls back to a temporary debug key. APKs built at different times will then have different signatures and cannot be upgraded over each other.
- Build history is stored in the browser, so clearing browser cache removes local history records.

## Custom domain

In your Cloudflare Pages project settings, open **Custom domains** and add your domain. SSL is configured automatically.

## Updating deployment

When the main repository receives new commits, you can manually redeploy from the Cloudflare Pages console or enable **Auto Deployment** to publish on every push.

## Project structure

```text
Pakr/
├── .github/workflows/
│   ├── build.yml              # Main build workflow
│   └── gen-keystore.yml       # Generate signing keystore
├── Scripts/
│   └── process_icon.py        # Icon processing script
├── index.html                 # Frontend page
├── _worker.js                 # API endpoint merged with frontend deployment
└── app/                       # Android project source
    └── src/main/java/com/webviewapp/
        ├── MainActivity.kt
        ├── SplashActivity.kt
        ├── TopProgressBar.kt
        └── IOSSpinnerView.kt
```
