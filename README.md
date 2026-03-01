# Iron Will Lite (Android)

A no-login, no-notification Android app for tracking and reducing bad habits.

## Features in this build
- No login or registration
- No notifications/reminders
- Local Room database persistence
- Add habits with motivation
- Log urges (trigger, intensity, note)
- Log relapses (trigger, note)
- Dashboard, insights summary, history summary, coping tools screen

## Build APK (debug)
1. Install Android SDK (API 34) and build tools.
2. Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) and Java 17+ / 21.
3. Run:

```bash
gradle :app:assembleDebug
```

APK output path:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Notes
This repo includes a wireframe/spec in `WIREFRAME_SPEC.md` and a runnable implementation scaffold in the `app/` module.
