# FoundIt — M2 audit (Weeks 5–9)

This audit is based on the project source included in this ZIP and the Week 5–9 assessment criteria previously shared in the conversation. It is a gap analysis, not a claim that the project has received a mark.

## Current evidence in the source

| Week / criterion | Evidence found | Status | What is still needed for a top mark |
|---|---|---|---|
| Week 5 — core journey | Compose screens for authentication, explore/map, item details, create item, chat and profile are present in `MainActivity.kt`. | Partial | Demonstrate and test the complete journey end-to-end; make state and screen logic maintainable outside the activity. |
| Week 5 — forms and validation | Registration checks name, email and password length; item and profile forms exist. | Partial | Add field-level errors, required-field validation, success/error feedback and tests for invalid/valid submissions. |
| Week 5 — ViewModel architecture | No ViewModel classes were found. Most UI state, navigation, DAO access and coroutine work are in `MainActivity.kt`. | Missing | Introduce ViewModels and repositories; keep composables focused on rendering and user events. |
| Week 5 — sample data / AI_USE | No dedicated, reliable sample-data seeding flow was identified in the application source. | Partial / verify | Add deterministic demo data or a documented demo account; document AI assistance and what was changed/verified in `AI_USE.md`. |
| Week 6 — Room persistence | Room entities, DAOs and database exist for items, users and community data. | Present, needs testing | Add DAO/database tests; prove create/read/update/delete and persistence after process/app restart. |
| Week 6 — CRUD | Item creation and favourites/profile/community writes exist. | Partial | Verify update/delete paths for listings and messages/reviews, handle failures, and test each CRUD path. |
| Week 6 — configuration changes | UI state uses Compose `remember`; durable data is in Room, but screen state is not owned by ViewModels. | Partial | Verify rotation/process recreation and retain transient screen state appropriately with ViewModels/SavedStateHandle. |
| Week 7 — API interaction | Retrofit API and repository target JSONPlaceholder (`/posts`). They are not connected to the main listing flow. | Partial | Connect remote data to the user journey; map DTOs to domain/local entities; use a real API suitable for the app or clearly justify the mock service. |
| Week 7 — loading/success/empty/error | No integrated UI state flow for remote requests was found. | Missing | Implement explicit Loading, Success, Empty and Error states, visible retry, and robust unexpected-response handling. |
| Week 7 — offline/cache | Room stores local data, but no verified remote-sync/offline-cache/retry policy is implemented. | Partial | Define source of truth, cache remote results in Room, detect connectivity, retry safely and test offline/online transitions. |
| Week 8 — mobile capability | Location permissions are declared in the manifest; MapTiler map and manual place search exist. | Partial | Request runtime location permission, obtain device location, handle denial/permanent denial, and offer manual location fallback. |
| Week 8 — permission/fallback | Manifest declarations alone do not implement runtime permission handling or GPS retrieval. | Missing | Implement permission rationale, permission launcher, location provider, error/loading states and manual fallback. |
| Week 9 — runnable MVP | Main screens and local data flows exist. | Partial | Run on target device, resolve build/runtime issues, record evidence and test the critical journeys. |
| Week 9 — architecture diagram | Diagram can be included in documentation, but it must match the implemented architecture. | To complete | Update after ViewModel/repository refactor and ensure every arrow/component exists in code. |
| Week 9 — checklist/report | This audit provides a gap checklist; it does not replace the required submitted progress report or tutor evidence. | Partial | Update with dated test results, screenshots, exact implementation evidence and honest known limitations. |

## Priority order to maximise the M2 mark

1. Fix and regression-test navigation paths: map → listing → chat → public profile → profile listing → back to chat → back to original listing → back to map; repeat using Android system Back.
2. Refactor `MainActivity.kt`: add ViewModels for Explore/listings, authentication/profile, and chat; introduce repositories as the only data access boundary.
3. Finish the Week 7 API flow: loading/success/empty/error, retry, response validation, DTO/domain mapping, Room cache and offline behaviour.
4. Finish Week 8 GPS: runtime permission request, current-location retrieval, denial handling and manual-location fallback.
5. Add automated tests: validation, DAO CRUD/persistence, repository success/failure/empty/unexpected responses, ViewModel state transitions and navigation regression.
6. Run the app on the target Samsung device; record evidence for every required journey and failure state.
7. Complete the final architecture diagram, `AI_USE.md`, test checklist and progress report using only verified results.

## Navigation regression scenario

- Start at Explore/map and open a listing.
- Open a chat from that listing.
- Open the other user's public profile.
- Open a listing from that public profile.
- Press Back: must return to the public profile.
- Press Back: must return to the same chat.
- Press Back: must return to the original listing opened from the map.
- Press Back: must return to Explore/map.
- Repeat with the Android system Back gesture/button, not only the in-app arrow.

Expected behaviour: every Back action returns exactly one level in the user's journey. No screen should navigate forward to a previous screen, and no pair of screens should trap the user in a loop.

## Verification limitation

A Gradle test/build was attempted in this environment, but the Gradle wrapper distribution was not cached and the environment could not resolve `services.gradle.org` (`UnknownHostException`). Therefore compilation and device execution are **not verified** here. Run `./gradlew testDebugUnitTest` and `./gradlew assembleDebug` in Android Studio or a network-enabled development environment, then execute the navigation scenario above on the target device.
