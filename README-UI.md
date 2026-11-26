# FoundIt — Premium app update

## Implemented
- Premium cream / forest-green design system.
- Login and registration screens built with Jetpack Compose.
- Local persistent user accounts in Room; password hashes use PBKDF2-HMAC-SHA256 with a per-user random salt.
- Session persistence across app restarts using Android SharedPreferences.
- Main map exploration screen, search by city/place, category emoji filters and map markers.
- Map engine migrated from osmdroid/CARTO/OpenStreetMap public tiles to the native MapTiler Kotlin SDK 2.2.1, with a light streets style and Compose annotations.
- Publish flow with Lost / Found selector, photo picker, emoji category grid, location, description and contact details.
- Persistent favorites stored in Room.
- Profile screen with account details and the user's own publications; logout.
- Room migration from database v2 to v3 preserves existing found-item records and adds favorites, owner and status fields plus the users table.

## Important account limitation
Accounts are currently stored locally on this device. This gives persistent sign-in on the same installation, but it is not cloud authentication: accounts do not sync between phones and cannot be recovered on another device. For a production multi-device app, connect Firebase Authentication / Firestore or a real backend. No backend credentials were present in the supplied project, so no cloud service has been fabricated.

## Map notes
- No Google Maps API key is required.
- Map search uses Android Geocoder.
- Existing posts store a textual location, so map pin accuracy depends on the geocoder result. The production-ready next step is to add latitude/longitude columns and let users choose an exact point on the map when publishing.
- The map starts centered on London and does not access device location.

## Build
Open the `FoundIt` folder in Android Studio and allow Gradle sync. `local.properties` is intentionally not included because Android Studio generates it for each computer.

Build verification could not be completed in this environment: Gradle wrapper 9.6.0 is not cached and the environment cannot resolve `services.gradle.org`. Please run **Build > Make Project** in Android Studio after Gradle sync. If Android Studio reports a Kotlin/Gradle error, send the complete Build Output and it can be corrected against your local toolchain.

## Profile and member profile update
- Profile editing now supports display name, profile photo and a short bio.
- User profile fields are stored in Room; database migration 5→6 adds `avatarUri` and `bio` without deleting existing accounts.
- Public member profiles can be opened from an item detail or from the chat header/profile action. They show the member's bio, reviews and listings.
- In the owner's profile, a clearly labelled `DEMO EXAMPLE` review is displayed as sample UI content only; it is not stored as a real review and is excluded from the verified review average.
- In a chat, tap the listing title in the top bar to open the related item detail. Tap `Profile` or the member strip to open the other participant's profile.

Note: profile photos and profile edits are local to this installation, like the existing local-only account/chat storage. Cross-device profiles require a shared backend.
