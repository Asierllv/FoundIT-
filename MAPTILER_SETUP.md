# FoundIt – MapTiler migration

This version replaces osmdroid/OpenStreetMap public tile requests with the native MapTiler Kotlin SDK and a light streets style.

## API key setup

1. Open the project root (the folder containing `settings.gradle.kts`).
2. Open `local.properties` (create it if Android Studio has not created it).
3. Add this line, using your own MapTiler key:

   `MAPTILER_API_KEY=YOUR_REAL_KEY`

4. Sync Gradle. The app reads this value into `BuildConfig.MAPTILER_API_KEY`; the key is not included in this ZIP.
5. Use a dedicated mobile key in MapTiler Cloud. Do not set HTTP Origin restrictions for this native Android SDK key; restrict it using the controls MapTiler provides for mobile/API-key usage.

## What changed

- Added `com.maptiler:maptiler-sdk-kotlin:2.2.1`.
- Removed `org.osmdroid:osmdroid-android`.
- Replaced `MapView`, `TileSourceFactory`, `GeoPoint` and osmdroid markers with `MTMapView`, `MTMapViewController` and Compose `MTCustomAnnotationView`.
- Selected `MTMapReferenceStyle.STREETS` with `MTMapStyleVariant.LIGHT`.
- Existing text locations are still geocoded with Android `Geocoder` to place annotations. For reliable global results and exact placement, the next data migration should persist latitude/longitude in Room at publish time.

## Build

In Android Studio: Sync Project with Gradle Files → Build > Make Project. Then run on the connected phone.
