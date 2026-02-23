# Aegis Android Client (Phase 1)

Native Android starter app for Aegis Agent support workflows.

## Features

- Chat request to backend `/api/chat`
- Retry and Escalate actions (`/api/chat` retry path and `/api/escalate`)
- Native file picker upload to `/api/analyze-logs`
- Incident timeline fetch from `/api/incidents/{correlationId}`
- Device metadata capture (`Build.MODEL`, `SDK_INT`, manufacturer)
- Hybrid AI tab with on-device response-pack diagnosis

## Run

1. Open `mobile/android` in Android Studio.
2. Let Gradle sync.
3. Ensure backend is running at `http://10.0.2.2:8080/api/` for emulator.
4. Run app on Android emulator/device.

## Notes

- API base URL comes from `BuildConfig.API_BASE_URL` in `app/build.gradle.kts`.
- Debug build uses emulator URL `http://10.0.2.2:8080/api/`; release defaults to HTTPS endpoint placeholder.
- Optional backend API key header is sent from `BuildConfig.API_AUTH_KEY` as `X-API-Key` when configured.
- For physical device, change base URL to your machine IP.
