# Aegis Android Client (Phase 1)

Native Android starter app for Aegis Agent support workflows.

## Features

- Chat request to backend `/api/chat`
- Native file picker upload to `/api/analyze-logs`
- Incident timeline fetch from `/api/incidents/{correlationId}`
- Device metadata capture (`Build.MODEL`, `SDK_INT`, manufacturer)

## Run

1. Open `mobile/android` in Android Studio.
2. Let Gradle sync.
3. Ensure backend is running at `http://10.0.2.2:8080/api/` for emulator.
4. Run app on Android emulator/device.

## Notes

- API base URL comes from `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`.
- For physical device, change base URL to your machine IP.
