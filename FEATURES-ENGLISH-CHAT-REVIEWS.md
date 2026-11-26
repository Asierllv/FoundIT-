# FoundIt update: English UI, filters, 10 km search, chat and reviews

## Included
- User-facing app copy changed to English.
- Category filters now support multiple simultaneous selections (for example Bags + Jewellery).
- City searches are geocoded with MapTiler. Listings are geocoded from their saved location text and filtered to a 10 km radius from the searched place.
- Item details include a message action and a post-handover review action (not shown to the listing owner).
- Profile includes received ratings and reviews linked to listing IDs.
- Room database migration 3 -> 4 creates persistent local chat-message and review tables.

## Important limitations
- Chat and reviews are stored in the local Room database on this device. They are not synchronized between different users' phones. A production multi-user chat/review system needs a shared backend such as Firebase or Supabase, authentication rules, and server-side moderation/abuse controls.
- The 10 km calculation geocodes the listing's free-text location. If a listing only says a city name (for example, "Lodz"), MapTiler returns a city-level point, not the exact place where the item was lost. For precise 10 km filtering, add a map pin / GPS coordinate picker to the listing form and store latitude/longitude with each listing.
- The map currently geocodes each listing when the map screen loads. For a larger production dataset, persist coordinates in Room and cache geocoding results to reduce API calls.

## Setup
Keep your MapTiler key in the project-root `local.properties` file:

```properties
MAPTILER_API_KEY=YOUR_REAL_KEY
```

Do not commit `local.properties` or share the key publicly. Ensure the key has the correct mobile-app restrictions in MapTiler Cloud.


## Chat inbox update
- Added a dedicated Chats tab in the bottom navigation.
- Conversations are grouped by listing and conversation partner and sorted by latest message.
- Inbox rows show the related item photo/category, partner email display name, latest message, time, and unread count.
- Added conversation search and an empty state.
- Added local read receipts (`isRead`) with Room migration 4 -> 5.
- Chat remains local to this device/database; a shared backend is required for real cross-device messaging.
