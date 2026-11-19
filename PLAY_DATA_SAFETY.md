# Google Play Data Safety Responses

**App:** Basketball Shot Tracker (`carl.basketballshotlog2`)  
**Version:** 3.0.0 (versionCode 100)  
**Last Updated:** November 2026  
**Target SDK:** 36

Use this document when filling out the Play Console **Data safety** form. The previous listing was removed for a missing Data safety form and an invalid encryption declaration (Amazon ads used HTTP).

---

## Overview

Shot data (locations, makes, misses, dates) is stored **only on-device** in a local SQLite database. The app does not create accounts and does not sync user content to a developer backend. Network use is for **Google AdMob** advertising and **Google UMP** consent.

Amazon Mobile Ads has been **removed**. Cleartext HTTP is disabled (`usesCleartextTraffic=false` + network security config).

---

## Data Collection Summary

| Data Type | Collected | Shared | Purpose |
|-----------|-----------|--------|---------|
| Shot / workout content (local DB) | Yes (on device) | No | App functionality |
| App interactions | Yes | Yes (AdMob) | Advertising |
| Advertising ID / Device or other IDs | Yes | Yes (AdMob) | Advertising |
| Approximate location (via ads SDK, if applicable) | Possibly via AdMob | Yes (AdMob) | Advertising |
| Diagnostics (ads SDK) | Possibly via AdMob | Yes (AdMob) | Advertising / fraud prevention |

The app itself does **not** collect email, name, photos, contacts, or precise location.

---

## App Permissions (declare in Play Console as needed)

| Permission | Why |
|------------|-----|
| `INTERNET` | Load ads / consent forms |
| `ACCESS_NETWORK_STATE` | Ads SDK connectivity checks |
| `ACCESS_WIFI_STATE` | Legacy ads-related connectivity (retained) |

No `POST_NOTIFICATIONS`, location, camera, mic, or storage permissions are requested by the app code.

---

## Third-Party SDKs

### Google Mobile Ads (AdMob)
- **Data:** Advertising ID, app interactions, device/app info; may share with Google advertising partners
- **Purpose:** Banner ads
- **Consent:** UMP shown before requesting ads where required
- **Privacy:** https://policies.google.com/privacy

### Google User Messaging Platform (UMP)
- **Purpose:** GDPR/consent messaging before personalized ads
- **Privacy:** https://policies.google.com/privacy

### GraphView (local UI library)
- No network / no data sharing

---

## Encryption

- **In transit:** Yes — HTTPS only for AdMob/UMP; cleartext traffic is not permitted.
- **At rest:** Shot data stays in the app’s private SQLite DB (Android app sandbox).

Previous Play rejection cited an invalid encryption declaration because Amazon ads used HTTP. That SDK is gone; declare encryption in transit as **Yes** only if AdMob/UMP traffic is HTTPS (it is).

---

## Children's Privacy / Families

This app is **not** directed at children under 13 (COPPA) or under 16 (GDPR age of consent where applicable).

- UMP uses `setTagForUnderAgeOfConsent(false)`
- If you enroll in Designed for Families, review AdMob Families / child-directed settings separately

---

## Play Console Form — Suggested Answers

1. **Does your app collect or share any of the required user data types?** Yes (via AdMob)
2. **Is all user data collected by your app encrypted in transit?** Yes
3. **Do you provide a way for users to request that their data is deleted?** Yes — uninstalling the app deletes on-device shot data; for AdMob-related data, users can use device ad settings / Google account tools. Optionally provide a developer contact email on the store listing.
4. **Data types to declare (typical for this app):**
   - Device or other IDs — Collected: Yes — Shared: Yes (AdMob) — Purpose: Advertising
   - App interactions — Collected: Yes — Shared: Yes (AdMob) — Purpose: Advertising
5. **Ads declaration:** App contains ads (AdMob banners)

---

## Owner Follow-ups Before Republish

1. Complete the **Data safety** form using this doc.
2. Confirm **Ads** declaration and any **Advertising ID** declaration.
3. Replace the TODO **AdMob App ID** in `app/src/main/res/values/ad_config.xml` with the real `ca-app-pub-8223005482588566~…` value from AdMob.
4. Create `keystore.properties` (gitignored) for release signing; do not commit keystores or passwords.
5. Remove or stop shipping the legacy `app/app-release.apk` from the repo when convenient (left untouched in this PR).
