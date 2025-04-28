# Twitter Clone Android App

A Twitter clone Android application built with Java and Firebase, implementing core Twitter features and functionality.

## Features

- User Authentication (Sign up, Login, Logout)
- Tweet Creation with Image Support
- Home Timeline
- Like and Retweet Functionality
- User Profiles
- Follow/Unfollow Users
- Real-time Updates
- Search Functionality
- Notifications
- Direct Messages

## Technical Stack

- Language: Java
- Backend: Firebase (Authentication, Realtime Database, Storage)
- Image Loading: Glide
- UI Components: Material Design
- Architecture: MVVM

## Project Setup

1. Clone the repository
2. Create a Firebase project and add your `google-services.json` file
3. Enable Firebase Authentication, Realtime Database, and Storage
4. Update the package name in build.gradle and AndroidManifest.xml
5. Build and run the project

## Firebase Configuration

1. Create a new Firebase project
2. Add Android app to Firebase project
3. Download google-services.json and place it in the app folder
4. Enable Email/Password authentication
5. Set up Realtime Database rules
6. Configure Storage rules

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/socialmediaapp/
│   │   │       ├── adapter/
│   │   │       ├── data/
│   │   │       ├── ui/
│   │   │       └── utils/
│   │   └── res/
│   │       ├── drawable/
│   │       ├── layout/
│   │       ├── menu/
│   │       └── values/
└── build.gradle
```

## Dependencies

- AndroidX Libraries
- Material Design Components
- Firebase SDK
- Glide Image Loading
- CircleImageView

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
