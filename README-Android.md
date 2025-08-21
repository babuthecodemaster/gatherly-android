# Gatherlyy - Android Application

A cosmic-themed Discord-inspired chat application for Android, featuring real-time messaging, server management, and a stunning nebula UI design.

## 🚀 Project Overview

This Android application is based on the existing web version of Gatherlyy and provides the same Discord-like functionality with a native mobile experience:

- **User Authentication** - Secure login and registration with the existing web backend
- **Server Management** - Create, join, and manage cosmic-themed servers
- **Channel System** - Text and voice channel support within servers
- **Real-time Messaging** - Live chat with message history and user status
- **Cosmic UI** - Beautiful nebula-themed interface with space colors
- **Member Management** - View online members and manage server roles

## 🎨 Design Theme

### Color Palette
- **Jet Black**: `#0A0A0A` - Primary background
- **Deep Navy**: `#121826` - Panel backgrounds
- **Electric Blue**: `#3A86FF` - Primary accent
- **Neon Purple**: `#9D4EDD` - Secondary accent
- **Cosmic White**: `#FFFFFF` - Text color
- **Cosmic Gray**: `#9CA3AF` - Secondary text

### UI Features
- Gradient backgrounds with cosmic nebula effects
- Glowing buttons and interactive elements
- Space-themed icons and imagery
- Smooth animations and transitions
- Dark mode optimized for night viewing

## 🏗️ Architecture

### Technology Stack
- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Retrofit 2 + OkHttp
- **UI Components**: Material Design Components
- **Image Loading**: Glide
- **Local Storage**: SharedPreferences
- **Threading**: AsyncTask / ExecutorService

### Project Structure
```
app/
├── src/main/java/com/cosmic/gatherly/
│   ├── data/                   # Data layer
│   │   ├── api/               # API interfaces and client
│   │   ├── model/             # Data models
│   │   ├── repository/        # Repository pattern
│   │   ├── request/           # API request models
│   │   └── response/          # API response models
│   ├── ui/                    # UI layer
│   │   ├── auth/              # Authentication screens
│   │   ├── main/              # Main chat interface
│   │   └── splash/            # Splash screen
│   └── GatherlyApplication.java
└── src/main/res/              # Resources
    ├── drawable/              # Vector drawables and backgrounds
    ├── layout/                # XML layouts
    ├── values/                # Colors, dimensions, strings
    └── mipmap/                # App icons
```

## 🔧 Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or newer
- Android SDK API 24+ (Android 7.0)
- Java 8 or newer
- Gradle 7.0+

### Backend Connection
The app connects to the existing Gatherlyy web backend:
- **Base URL**: `http://10.0.2.2:5000/` (Android emulator localhost)
- **API Endpoints**: Same as web version (`/api/auth/*`, `/api/servers/*`, etc.)
- **Authentication**: Session-based with cookies

### Installation Steps

1. **Clone the project**
   ```bash
   git clone <repository-url>
   cd gatherlyy-android
   ```

2. **Open in Android Studio**
   - File → Open → Select the project directory
   - Wait for Gradle sync to complete

3. **Configure Backend URL**
   - Update `ApiClient.java` BASE_URL if needed
   - For physical device: Replace `10.0.2.2` with your computer's IP address

4. **Run the Application**
   - Select an emulator or connected device
   - Click Run (Ctrl+Shift+F10)

## 📱 Features Implementation

### Authentication Flow
- **Splash Screen**: Animated logo with auto-login check
- **Login/Register**: Tabbed interface with validation
- **Session Management**: Persistent login with SharedPreferences
- **Error Handling**: User-friendly error messages

### Main Interface
- **Server Sidebar**: Vertical list of joined servers with icons
- **Channel List**: Expandable channels within selected server
- **Chat Area**: Message list with rich formatting
- **Members List**: Online users with status indicators
- **User Panel**: Current user info with settings access

### Networking
- **Retrofit Integration**: RESTful API communication
- **Session Handling**: Cookie-based authentication
- **Error Recovery**: Network error handling and retries
- **Offline Support**: Cached data for offline viewing

## 🎯 API Integration

### Authentication Endpoints
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration
- `GET /api/auth/me` - Get current user
- `POST /api/auth/logout` - Logout

### Server Management
- `GET /api/servers` - Get user servers
- `POST /api/servers` - Create new server
- `GET /api/servers/{id}` - Get server details
- `GET /api/servers/{id}/members` - Get server members

### Messaging
- `GET /api/channels/{id}/messages` - Get channel messages
- `POST /api/channels/{id}/messages` - Send message
- `GET /api/channels/{id}` - Get channel info

## 🔮 Future Enhancements

### Phase 1 - Core Features
- [ ] Real-time messaging with WebSockets
- [ ] Push notifications for new messages
- [ ] Image/file sharing capabilities
- [ ] Voice channel integration

### Phase 2 - Advanced Features
- [ ] Message reactions and emojis
- [ ] Server roles and permissions
- [ ] Direct messaging between users
- [ ] Message search and filtering

### Phase 3 - Premium Features
- [ ] Custom server themes
- [ ] Advanced user profiles
- [ ] Server analytics dashboard
- [ ] Integration with external services

## 📋 Testing

### Unit Tests
- Repository pattern testing
- API response parsing
- Authentication flow validation

### UI Tests
- Login/register flow
- Message sending and receiving
- Navigation between screens

### Integration Tests
- Backend API connectivity
- Session management
- Error handling scenarios

## 🚀 Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Play Store Preparation
1. Generate signed APK/AAB
2. Update app version and build number
3. Add Play Store assets (screenshots, descriptions)
4. Configure release notes

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🌟 Acknowledgments

- Material Design Components for beautiful UI elements
- Retrofit team for excellent networking library
- Android community for best practices and resources
- Cosmic/space imagery inspiration from NASA and space photography

---

**Explore the cosmos of communication with Gatherlyy! 🚀✨**