# Navigation fix notes

The navigation bug was caused by reusing `detailReturnPage` for several nested journeys. In particular, opening a listing from a public profile overwrote the return destination for the original listing that launched the chat. When the user returned to chat, the overwritten destination remained and Back could bounce between the original listing and public profile.

The current fix snapshots the detail parent before entering chat and before opening a public profile. Those snapshots are restored when leaving the nested screen. The Android system Back handler follows the same restoration rules as the in-app Back actions.

Regression path to test on device:
`Explore/map → listing → chat → public profile → profile listing → public profile → chat → original listing → Explore/map`.

This is a targeted correction to the existing single-activity navigation state, not a full Navigation Compose back-stack migration. A proper typed navigation graph/back stack remains a worthwhile architectural improvement for the final submission.
