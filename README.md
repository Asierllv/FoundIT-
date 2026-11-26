# FoundIt! — Lost & Found, Reconnected

**A location-based mobile platform designed to help communities report, discover, and recover lost personal belongings.**

<p align="center">
  <strong>Find what matters. Reconnect people with their belongings.</strong>
</p>

---

## 1. Project Overview

### About FoundIt!

FoundIt! is an Android application designed to simplify the process of reporting, searching for, and recovering lost personal belongings.

Losing everyday items such as keys, wallets, bags, identification cards, or electronic devices can cause considerable stress, inconvenience, and unnecessary waste. Existing solutions often rely on physical Lost & Found offices, informal social media posts, or unstructured community groups. These approaches can make it difficult to locate relevant information, identify an item, or contact the person who found it.

FoundIt! addresses this problem through a centralized, searchable, and location-aware mobile platform.

The application brings together people who have lost belongings and people who have found them, helping them reconnect through item listings, geographical search, categorization, and communication features.

### Our Vision

To make recovering lost belongings easier, faster, and more accessible by creating a connected community where people can help one another.

### The Social Problem

Losing personal belongings is more than a minor inconvenience. It can result in:

- Financial costs associated with replacing valuable possessions.
- Loss of important personal or identification documents.
- Stress, frustration, and disruption to everyday activities.
- Unnecessary consumption and environmental waste.
- Difficulty accessing reliable information about found objects.

Traditional recovery systems are often fragmented, geographically limited, or difficult to search.

FoundIt! aims to make this process more organized and accessible through mobile technology.

---

## 2. Objectives and Project Goals

The main objective is to develop a functional Android MVP that demonstrates how location-aware technology and community interaction can improve the recovery of lost belongings.

The project focuses on five key goals:

1. **Accessibility:** Make reporting and searching for lost belongings straightforward.
2. **Discoverability:** Organize item listings by category, description, and geographical location.
3. **Community engagement:** Encourage cooperation between people who lose and find belongings.
4. **Persistence:** Preserve relevant application data across sessions.
5. **Mobile integration:** Use device capabilities such as GPS and image selection to improve the user experience.

---

## 3. Target Audience

FoundIt! is primarily designed for individuals who frequently move through shared public environments.

| User group | Potential use case |
|---|---|
| University students | Recovering belongings lost around campus, libraries, or lecture halls. |
| University staff | Reporting objects found in classrooms, offices, or shared facilities. |
| Urban residents | Finding belongings lost in public spaces and neighbourhoods. |
| Commuters | Searching for items lost around transport hubs and public transport locations. |
| Visitors | Reporting or recovering belongings in unfamiliar places. |

Although university communities are an initial target audience, the application is designed to be adaptable to wider urban communities.

---

## 4. Core User Stories

The application is structured around three primary user journeys.

### 4.1 Reporting a Found Item

**As a finder**, I want to publish information about an item I have found so that its owner can identify and recover it.

Expected functionality:

- Add an item photograph.
- Provide a title and description.
- Select an appropriate category.
- Specify the location where the item was found.
- Publish the listing for other users to discover.

### 4.2 Searching for a Lost Item

**As someone who has lost an item**, I want to search and filter available listings so that I can identify belongings that may be mine.

Expected functionality:

- Search by item name or description.
- Search by city, neighbourhood, or place.
- Filter by one or more categories.
- Explore nearby listings.
- View item details and location information.

### 4.3 Recovering an Item

**As a user**, I want clear information and a way to communicate with the person who found an item so that I can arrange its safe recovery.

Expected functionality:

- View detailed listing information.
- Identify where the item was found.
- Access the relevant user profile.
- Use the available communication functionality.
- Arrange a safe and appropriate collection.

---

## 5. Main Application Features

### 5.1 Explore and Discovery

The Explore section is the main entry point to FoundIt!.

It provides a discovery-oriented interface where users can browse available listings and search for belongings.

Features include:

- Search by location, item, or category.
- Category-based filtering.
- Popular location suggestions.
- Recent and relevant item listings.
- A location-based discovery experience.

### 5.2 Interactive Map

FoundIt! integrates MapTiler to provide a geographical representation of item listings.

The map helps users understand where belongings were reported and discover nearby listings.

Key capabilities:

- Interactive map rendering.
- Location-based item discovery.
- Search by city or place.
- Geographical filtering within a 10 km radius.
- Map centring based on a selected location.
- Integration with device location functionality.

### 5.3 Item Listings and Details

Each listing provides relevant information to help users identify an item.

Listing information may include:

- Item title.
- Description.
- Photograph.
- Category.
- Location.
- Listing type and relevant user information.

### 5.4 Reporting Items

The reporting workflow allows users to contribute to the community by publishing found belongings.

The interface supports the collection of information needed to create an informative listing.

### 5.5 Chats and Communication

FoundIt! includes a local conversation interface associated with item listings.

Users can access conversations, view messages, and navigate between conversations, listings, and public profiles.

**Implementation note:** The current prototype uses local application data. Real-time messaging and cross-device conversation synchronisation require a remote backend and are not claimed as implemented.

### 5.6 User Profiles

The profile experience provides a space for users to view personal information and relevant community activity.

The broader design includes profile information, listings, and user feedback.

### 5.7 Saved Items

Users can access saved listings through the Saved section, making it easier to revisit items of interest.

---

## 6. Technology Stack

FoundIt! is developed as a native Android application.

| Technology | Purpose |
|---|---|
| Kotlin | Main application programming language. |
| Android Studio | Development environment and Android project management. |
| Jetpack Compose | Declarative user interface development. |
| Material 3 | UI components and design foundations. |
| Room | Local database and persistent application storage. |
| Kotlin Coroutines | Asynchronous operations. |
| StateFlow | Reactive application state management. |
| Retrofit | HTTP communication and remote API integration. |
| MapTiler SDK | Interactive map rendering and geographical functionality. |
| Android Location APIs | Access to device location and GPS functionality. |

### Development Environment

- **Platform:** Android
- **IDE:** Android Studio
- **Language:** Kotlin
- **Minimum Android SDK:** API 26 (Android 8.0), subject to the MapTiler SDK requirement.
- **Target device:** Android smartphones.
- **Testing device:** Samsung SM-S938B.

---

## 7. Application Architecture

FoundIt! follows an evolving separation-of-concerns architecture intended to separate the user interface, application logic, and data management.

The intended architecture is based on the following layers:

### Presentation Layer

Responsible for displaying the application interface and handling user interactions.

Main components:

- Explore screens.
- Map screens.
- Item detail screens.
- Chat screens.
- Profile screens.
- Form components.

ViewModels manage screen-related state and coordinate application operations.

### Data Layer

Responsible for providing and managing application data.

It includes:

- Local database access through Room.
- Data Access Objects (DAOs).
- Database entities.
- Repository classes.
- Remote API communication through Retrofit.

### Device Integration Layer

Responsible for accessing mobile device capabilities.

It includes:

- Location permissions.
- GPS and location retrieval.
- Device-based location services.
- Image selection through Android's supported photo selection mechanisms.

### Architecture Diagram

```text
                 USER INTERFACE
              Jetpack Compose / UI
                       |
                       v
                  VIEWMODELS
             Application State and Logic
                       |
                       v
                  REPOSITORIES
                       |
              +--------+--------+
              |                 |
              v                 v
         LOCAL DATA        REMOTE DATA
            Room             Retrofit
              |                 |
              v                 v
         Local Storage      External API


              DEVICE CAPABILITIES
                       |
              +--------+--------+
              |                 |
              v                 v
             GPS            Photo Picker
```

The architecture is intended to make the application easier to maintain, test, and extend as additional functionality is introduced.

---

## 8. Data Persistence and Offline Considerations

FoundIt! uses Room as its local persistence solution.

Local persistence is important because users should be able to access previously stored information even after closing and reopening the application.

The local database supports application data such as:

- Item listings.
- User information.
- Conversations and messages.
- Saved items.

The application is designed to preserve local data across application restarts.

### Offline Strategy

The intended data strategy combines local persistence with remote data retrieval.

When remote connectivity is unavailable, locally stored information can remain accessible.

Remote integration should handle:

- Loading states.
- Successful responses.
- Empty responses.
- Network failures.
- Unexpected server responses.
- Retry operations.

A production implementation would additionally require a defined synchronisation strategy to resolve differences between local and remote data.

---

## 9. Location-Based Functionality

Geographical context is one of the defining features of FoundIt!.

The application uses MapTiler for map rendering and Android location services to support location-aware discovery.

### Location Features

- Search by city, neighbourhood, or place.
- Centre the map around a selected location.
- Use device location when permission is granted.
- Display nearby item listings.
- Restrict geographical search to a 10 km radius.
- Provide manual location selection as an alternative.

### Permission Handling

Location access is requested when the user activates the location functionality.

The application should support the following scenarios:

| Scenario | Expected behaviour |
|---|---|
| Precise location granted | Retrieve the device's location and centre the map. |
| Approximate location granted | Use the available approximate coordinates. |
| Permission denied | Allow manual location search. |
| Location unavailable | Allow the user to select a location manually. |

Location functionality is designed to remain optional so users can continue using the application without granting GPS access.

---

## 10. Privacy, Safety and Responsible Use

Because FoundIt! involves personal belongings and geographical information, user safety and responsible information sharing are important design considerations.

The application should encourage users to:

- Avoid publishing sensitive personal information.
- Avoid displaying complete identification or payment card details.
- Arrange item collection in safe, public locations.
- Verify ownership before returning valuable belongings.
- Avoid sharing precise personal addresses unnecessarily.

Location permissions should only be requested when the relevant functionality is used.

A production deployment would also require appropriate privacy documentation, secure authentication, backend access controls, and data-retention policies.

---

## 11. Accessibility and User Experience

FoundIt! aims to provide a clear and accessible experience for users with different needs and levels of technical familiarity.

Accessibility considerations include:

- Readable typography and appropriate contrast.
- Scalable text where supported.
- Clear labels for interactive components.
- Meaningful content descriptions for icons.
- TalkBack compatibility.
- Understandable validation and error messages.
- Consistent navigation.
- Large, accessible touch targets.

The visual identity uses a warm cream background combined with forest-green accents to create a calm, recognisable, and consistent interface.

---

## 12. Minimum Viable Product — Development Roadmap

The MVP is developed incrementally, with emphasis on functional implementation and testing.

### Core Application

- [x] Android project setup.
- [x] Kotlin-based application.
- [x] Jetpack Compose interface.
- [x] Initial application navigation.
- [x] Explore screen.
- [x] MapTiler map integration.
- [x] Local Room persistence.
- [x] Initial chat and profile interfaces.
- [ ] Complete ViewModel integration across the main screens.
- [ ] Complete repository-based architecture.

### Search and Discovery

- [x] Search interface.
- [x] Category selection interface.
- [x] Location-based search interface.
- [x] Map-based discovery.
- [x] 10 km geographical search logic.
- [ ] Comprehensive search and filtering tests.

### Item Management

- [x] Local item data model.
- [x] Item detail interface.
- [x] Initial item reporting interface.
- [ ] Verify complete Create, Read, Update, and Delete operations.
- [ ] Complete form validation and error handling.

### Remote Data and Offline Behaviour

- [x] Retrofit integration foundation.
- [ ] Complete remote data integration with the main application flow.
- [ ] Implement and verify loading, success, empty, and error states.
- [ ] Implement retry behaviour.
- [ ] Verify offline data access and recovery.

### Mobile Capabilities

- [x] Map integration.
- [x] Initial device location functionality.
- [x] Location permission handling foundation.
- [ ] Verify accurate current-location retrieval.
- [ ] Verify precise and approximate location behaviour.
- [ ] Verify all permission-denial scenarios.
- [ ] Complete GPS integration testing.

### Testing and Documentation

- [x] Initial project documentation.
- [ ] Complete unit testing.
- [ ] Complete persistence testing.
- [ ] Complete network failure testing.
- [ ] Complete location permission testing.
- [ ] Final architecture documentation.
- [ ] Final MVP demonstration.

**Note:** Checklist entries describe the development status and remaining verification work of the prototype. A feature's interface or initial implementation does not necessarily mean that all its acceptance tests have been completed.

---

## 13. Testing Strategy

Testing is an essential part of ensuring that FoundIt! behaves reliably.

The project should be validated through several testing categories.

### Functional Testing

- Verify that users can navigate between application sections.
- Verify that listings display the correct information.
- Verify that search and category filters work correctly.
- Verify that item details correspond to the selected listing.
- Verify that chat and profile navigation behave consistently.

### Persistence Testing

- Create an item and restart the application.
- Verify that the item remains stored.
- Update and delete records.
- Verify that saved items and messages remain available.

### Network Testing

- Test successful remote requests.
- Test empty responses.
- Test server errors.
- Test disconnected network conditions.
- Verify retry behaviour.
- Verify that locally stored information remains accessible offline.

### Location Testing

- Test precise location permission.
- Test approximate location permission.
- Test denied permissions.
- Test unavailable GPS.
- Verify that the map centres on the retrieved coordinates.
- Verify the 10 km geographical filtering.

### User Experience Testing

- Verify navigation and back-button behaviour.
- Check empty states and error feedback.
- Verify text readability and accessibility.
- Test the application on a physical Android device.

---

## 14. Current Prototype Limitations

FoundIt! is an academic mobile application prototype and is not intended to represent a fully deployed commercial platform.

The following capabilities would require further development:

- Remote authentication and account management.
- Cross-device synchronisation.
- Real-time messaging.
- Production backend infrastructure.
- Secure remote image storage.
- Push notifications.
- Moderation and reporting mechanisms.
- Production-level privacy and security controls.
- Google Play Store deployment.

These limitations define the current scope of the prototype and provide opportunities for future development.

---

## 15. Future Development

Potential future improvements include:

1. **Backend integration:** Introduce a secure backend to synchronise listings and user information between devices.
2. **Real-time messaging:** Enable instant communication between finders and owners.
3. **Push notifications:** Notify users when relevant items are published or messages are received.
4. **Image-based discovery:** Explore image recognition to help identify visually similar belongings.
5. **Advanced geographical search:** Improve proximity-based discovery and filtering.
6. **Community verification:** Introduce mechanisms to support trustworthy item recovery.
7. **Campus deployment:** Adapt FoundIt! for universities and educational institutions.
8. **Environmental impact:** Encourage reuse and recovery rather than unnecessary replacement of lost belongings.

---

## 16. Team and Contributions

**Project:** FoundIt! — Lost & Found Social App

**Team Members:**
- Asier Llopis
- June Conde

**Course:** Mobile for Social Good

**Academic Project:** Studio Milestone 1 (M1)

The project is developed collaboratively, with a focus on mobile application development, user-centred design, software architecture, and the use of technology to address a practical social problem.

---

## 17. Getting Started

### Requirements

- Android Studio.
- Android SDK API 26 or later.
- A compatible Android device or emulator.
- Internet access for remote services and map functionality.
- A valid MapTiler API key.

### Installation

1. Clone or download the project repository.
2. Open the project in Android Studio.
3. Configure the required MapTiler API key in the local development configuration.
4. Allow Gradle to synchronise the project.
5. Connect an Android device or start an emulator.
6. Build and run the application.

**Configuration note:** API keys and other private configuration values should not be committed to the public repository. Use local configuration and appropriate secret-management practices.

---

## 18. Acknowledgements

FoundIt! is developed as part of an academic project exploring how mobile technology can address everyday social problems.

The project builds on the Android development ecosystem and uses technologies including Kotlin, Jetpack Compose, Room, Retrofit, and MapTiler.

---

<p align="center">
  <strong>FoundIt! — Lost & Found, Reconnected.</strong><br/>
  <em>Helping people find what matters, one connection at a time.</em>
</p>
