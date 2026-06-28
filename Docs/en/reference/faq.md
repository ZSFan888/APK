# FAQ

## What should I do if packaging fails?

Open your forked repository → **Actions**, find the related workflow run, and inspect the detailed logs.

Common causes include:
- `GH_PAT` does not have both `repo` and `workflow` scopes
- Keystore secrets are missing or incorrectly configured
- The target website is unreachable

## Why can't the installed APK be upgraded over a previous one?

That usually means the two builds were signed with different keys. Configure `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` correctly so every build uses the same signing key.

## Why is build history missing?

Build history is stored in browser local storage, so clearing browser cache removes it. That is expected behavior.

## What if I run out of GitHub Actions minutes?

GitHub free accounts typically include 2000 minutes per month. If you exceed that, wait for the monthly reset or upgrade your GitHub plan.

## Does it support iOS?

Not at the moment. Pakr currently generates Android APKs only.

## Can it package websites that require login?

Yes. The WebView supports cookies, so login state can be preserved inside the packaged app.
