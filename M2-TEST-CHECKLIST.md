# M2 test checklist — record actual outcomes before submission

| Area | Test | Expected result | Result / evidence |
|---|---|---|---|
| Week 5 | Submit blank required field | Field rejected with useful message | Not run |
| Week 5 | Invalid email / short password | Validation returns field-specific error | Unit test added; execution not verified |
| Week 5 | Rotate/recreate activity on Explore, Chat and Profile | Screen state remains coherent | Not run |
| Week 6 | Create/read/update/delete listing | Each operation reflected in Room/UI | User reports data survives restart; CRUD test not run |
| Week 6 | Close and relaunch app | Existing local data remains | User reports passed; attach screenshot/video |
| Week 7 | API success | Loading then Success/Empty | Not run |
| Week 7 | Disable network and refresh | Error state; local Room listings remain available | Not run |
| Week 7 | Restore network and retry | Request can be retried | Not run |
| Week 8 | Grant location permission | Last known device location returned when available | Not run |
| Week 8 | Deny location permission | Manual location fallback remains usable | Not run |
| Week 8 | No last-known location | Unavailable callback and manual fallback | Not run |
| Week 9 | Full navigation regression | Back returns one level each time | Not run |
| Build | `./gradlew testDebugUnitTest` | All tests pass | Not run |
| Build | `./gradlew assembleDebug` | Debug APK builds | Not run |
