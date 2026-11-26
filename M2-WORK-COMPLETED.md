# FoundIt M2 — Weeks 5, 7 and 8 work completed in this pass

## Week 5
- Added an `ExploreRepository` boundary between Room/remote access and presentation logic.
- Added Android lifecycle ViewModels for Explore, Chat and Profile. Explore exposes local Room listings as StateFlow and explicit remote feed states.
- Added reusable form validation rules and JVM unit tests for required fields, email and password length.
- Added the ViewModel/coroutines dependencies.

**Remaining Week 5 integration:** the existing `MainActivity.kt` still contains much of the original screen state and business logic. These ViewModels are new foundations but have not all been wired into the existing Compose screens. Authentication ViewModel, SavedStateHandle navigation state, full field-level error rendering, and end-to-end ViewModel tests remain to be completed.

## Week 7
- Kept Retrofit API/repository and introduced `ExploreRepository` to isolate remote sample calls from local Room listings.
- `ExploreViewModel` exposes Idle, Loading, Success, Empty and Error states and retry is available by calling `refreshRemoteSample()` again.
- Network failures are caught and surfaced as Error state.
- Room remains the durable local listing source, so local listings remain observable without a network connection.

**Important limitation:** JSONPlaceholder is a demonstration API, not a real FoundIt backend. Its POST endpoint simulates creation and does not persist real community listings. Remote posts are deliberately not inserted into `found_items`, to avoid ID collisions or overwriting local user-created data. A real backend and explicit cache/sync policy are still required for genuine multi-device listings. The remote state is not yet rendered in the Explore UI.

## Week 8
- Added runtime fine/coarse location permission request component.
- Added permission checks and retrieval of the most recent available Android `LocationManager` last-known location.
- Denial or unavailable location invokes callbacks for manual-location fallback.

**Important limitation:** this implementation reads last-known location; it does not actively request a fresh GPS fix or subscribe to location updates. The reusable `LocationPermissionControl` still needs to be placed in the Explore/map screen and its coordinates passed into the map camera/geospatial filter. Manual city search already exists in the app and should remain available.

## Week 6
The existing Room implementation is retained unchanged. The user's report that data survives app restart is useful evidence, but DAO CRUD and migration tests should still be run and documented.

## Verification status
Compilation and device testing have not been certified in this environment. Run:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Then install on the Samsung device and test permission granted, denied, location unavailable, offline mode, remote API error, and manual city fallback. Do not report these as passed until observed.
