# LitterApp 🌏

This is the repository for my final year uni project, building a location-based social media.

## Tech stack

- **Kotlin** for Android app development,
- **Google Firebase & Cloud Firestore** for cloud data storage,
- **Google Maps SDK for Android** for map functionality.

## Repo structure

```
📁 main/java
├── 🔐 AuthenticationActivity.kt         # Initial container shown before user logs in.
├── 📐 DataClasses.kt                    # Various helpers for things like distance calculations.
├── 📱 MainActivity.kt                   # Main container to render other fragments in.
├── 🚨 UIHelper.kt                       # Helper to display UI like alerts and progress bar. 
│   
📁 fragments
├── 🗣️ CommentsFragment.kt               # Code to create, edit and view comments.
├── 📝 EditMessageFragment.kt            # Code to edit an existing message.
├── 🚪 LoginFragment.kt                  # Code for login.
├── 🗺️ MapFragment.kt                    # Code to render map and map objects (including user).
├── 💬 MessagesFragment.kt               # Code to view all messages in a list.
├── ✍️ NewMessageFragment.kt             # Code to create + populate a new message.
├── 📨 RegisterFragment.kt               # Code to create + register a new account.
├── 🔙 ResetFragment.kt                  # Code to reset your account's password.
├── 📍 ReviewMessageFragment.kt          # Code to review + update location of message.
├── ⚙️ SettingsFragment.kt               # Code for viewing + updating settings.
├── 🔎 ViewMessageFragment.kt            # Code for viewing a message.
│   
│
📁 adapters
├── 📄 CommentAdapter.kt                 # Used to render comment functionality.
├── 📄 CommentHolder.kt                  # Binds comment fields to data.
├── 📄 MessageAdapter.kt                 # Used to render message functionality.
├── 📄 MessageHolder.kt                  # Binds message fields to data, code for creating markers on map.
└── 📄 ViewPagerAdapter.kt               # Used to render multipe views / pages.
```
