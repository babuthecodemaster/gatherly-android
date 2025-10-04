# Design Document

## Overview

The Firebase authentication integration design builds upon the existing Firebase Auth setup in the Gatherly app to add Firestore database connectivity, persistent authentication state management, and logout functionality. The current implementation already has Firebase Auth initialized in multiple activities, but lacks proper state persistence, database integration, and centralized authentication management.

Key improvements needed:
1. **Add Firestore database integration** for user data storage
2. **Implement persistent authentication** using SharedPreferences and Firebase Auth state listeners
3. **Create centralized authentication manager** to handle auth state across the app
4. **Add logout functionality** with proper cleanup
5. **Implement auto-login flow** on app startup

## Architecture

The enhanced authentication system will follow a centralized architecture pattern:

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │    │  Business Layer  │    │   Data Layer    │
│                 │    │                  │    │                 │
│ - Activities    │◄──►│ - AuthManager    │◄──►│ - Firebase Auth │
│ - Fragments     │    │ - UserRepository │    │ - Firestore DB  │
│ - ViewModels    │    │ - AuthState      │    │ - SharedPrefs   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### Core Components:
- **AuthManager** - Centralized authentication state management
- **UserRepository** - Data access layer for user information
- **AuthStateManager** - Persistent authentication state handling
- **FirestoreUserService** - User data operations in Firestore

## Components and Interfaces

### 1. Firebase Configuration Enhancement

**Add Firestore to build.gradle.kts:**
```kotlin
// Add to existing Firebase dependencies
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx") // For future file uploads
```

**Firebase Security Rules (Firestore):**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Public read access for app configuration
    match /config/{document} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

### 2. Authentication Manager

**AuthManager Interface:**
```java
public interface AuthManager {
    // Authentication state
    LiveData<AuthState> getAuthState();
    boolean isUserLoggedIn();
    FirebaseUser getCurrentUser();
    
    // Authentication operations
    Task<AuthResult> signIn(String email, String password);
    Task<AuthResult> signUp(String email, String password);
    Task<Void> signOut();
    
    // Auto-authentication
    void checkAuthState();
    void enableAutoLogin(boolean enabled);
    
    // User data
    Task<UserProfile> getUserProfile();
    Task<Void> updateUserProfile(UserProfile profile);
}
```

**AuthManagerImpl Implementation:**
```java
public class AuthManagerImpl implements AuthManager {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mFirestore;
    private SharedPreferences mPrefs;
    private MutableLiveData<AuthState> authStateLiveData;
    
    // Singleton pattern for app-wide access
    private static AuthManagerImpl instance;
    
    public static AuthManagerImpl getInstance(Context context) {
        if (instance == null) {
            instance = new AuthManagerImpl(context);
        }
        return instance;
    }
}
```

### 3. User Data Models

**UserProfile Model:**
```java
public class UserProfile {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private long createdAt;
    private long lastLoginAt;
    private Map<String, Object> preferences;
    
    // Constructors, getters, setters
    public UserProfile() {} // Required for Firestore
    
    public UserProfile(FirebaseUser firebaseUser) {
        this.uid = firebaseUser.getUid();
        this.email = firebaseUser.getEmail();
        this.displayName = firebaseUser.getDisplayName();
        this.photoUrl = firebaseUser.getPhotoUrl() != null ? 
            firebaseUser.getPhotoUrl().toString() : null;
        this.createdAt = System.currentTimeMillis();
        this.lastLoginAt = System.currentTimeMillis();
        this.preferences = new HashMap<>();
    }
}
```

**AuthState Model:**
```java
public class AuthState {
    public enum Status {
        LOADING,
        AUTHENTICATED,
        UNAUTHENTICATED,
        ERROR
    }
    
    private Status status;
    private FirebaseUser user;
    private UserProfile userProfile;
    private String errorMessage;
    
    // Static factory methods
    public static AuthState loading() {
        return new AuthState(Status.LOADING, null, null, null);
    }
    
    public static AuthState authenticated(FirebaseUser user, UserProfile profile) {
        return new AuthState(Status.AUTHENTICATED, user, profile, null);
    }
    
    public static AuthState unauthenticated() {
        return new AuthState(Status.UNAUTHENTICATED, null, null, null);
    }
    
    public static AuthState error(String message) {
        return new AuthState(Status.ERROR, null, null, message);
    }
}
```

### 4. Firestore User Service

**FirestoreUserService:**
```java
public class FirestoreUserService {
    private static final String USERS_COLLECTION = "users";
    private FirebaseFirestore mFirestore;
    
    public FirestoreUserService() {
        mFirestore = FirebaseFirestore.getInstance();
    }
    
    public Task<Void> createUserProfile(UserProfile profile) {
        return mFirestore.collection(USERS_COLLECTION)
            .document(profile.getUid())
            .set(profile);
    }
    
    public Task<UserProfile> getUserProfile(String uid) {
        return mFirestore.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .continueWith(task -> {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    return document.toObject(UserProfile.class);
                } else {
                    throw new Exception("User profile not found");
                }
            });
    }
    
    public Task<Void> updateUserProfile(UserProfile profile) {
        profile.setLastLoginAt(System.currentTimeMillis());
        return mFirestore.collection(USERS_COLLECTION)
            .document(profile.getUid())
            .set(profile, SetOptions.merge());
    }
}
```

### 5. Enhanced Application Class

**GatherlyApplication:**
```java
public class GatherlyApplication extends Application {
    private AuthManager authManager;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
        }
        
        // Initialize AuthManager
        authManager = AuthManagerImpl.getInstance(this);
        
        // Set up global auth state listener
        authManager.checkAuthState();
    }
    
    public AuthManager getAuthManager() {
        return authManager;
    }
}
```

### 6. Enhanced Splash Activity

**Updated SplashActivity with Auto-Login:**
```java
public class EnhancedSplashActivity extends AppCompatActivity {
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        authManager = ((GatherlyApplication) getApplication()).getAuthManager();
        
        // Observe auth state changes
        authManager.getAuthState().observe(this, authState -> {
            switch (authState.getStatus()) {
                case AUTHENTICATED:
                    navigateToMain();
                    break;
                case UNAUTHENTICATED:
                    navigateToAuth();
                    break;
                case ERROR:
                    handleAuthError(authState.getErrorMessage());
                    break;
                case LOADING:
                    // Show loading indicator
                    break;
            }
        });
        
        // Start auth state check
        authManager.checkAuthState();
    }
}
```

### 7. Logout Implementation

**MainActivity with Logout:**
```java
public class MainActivity extends AppCompatActivity {
    private AuthManager authManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        authManager = ((GatherlyApplication) getApplication()).getAuthManager();
        
        // Set up logout button
        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> performLogout());
        
        // Observe auth state for automatic logout handling
        authManager.getAuthState().observe(this, authState -> {
            if (authState.getStatus() == AuthState.Status.UNAUTHENTICATED) {
                navigateToAuth();
            }
        });
    }
    
    private void performLogout() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes", (dialog, which) -> {
                authManager.signOut().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Navigation handled by auth state observer
                        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Logout failed", Toast.LENGTH_SHORT).show();
                    }
                });
            })
            .setNegativeButton("No", null)
            .show();
    }
}
```

## Data Models

### Firestore Collections Structure

```
/users/{userId}
├── uid: string
├── email: string
├── displayName: string
├── photoUrl: string
├── createdAt: timestamp
├── lastLoginAt: timestamp
└── preferences: map
    ├── theme: string
    ├── notifications: boolean
    └── language: string

/config/app
├── minVersion: number
├── forceUpdate: boolean
└── maintenanceMode: boolean
```

### SharedPreferences Keys

```java
public class AuthPreferences {
    public static final String PREF_NAME = "gatherly_auth";
    public static final String KEY_AUTO_LOGIN_ENABLED = "auto_login_enabled";
    public static final String KEY_LAST_LOGIN_EMAIL = "last_login_email";
    public static final String KEY_USER_PROFILE_CACHE = "user_profile_cache";
    public static final String KEY_LAST_AUTH_CHECK = "last_auth_check";
}
```

## Error Handling

### 1. Firebase Connection Errors
```java
public class FirebaseErrorHandler {
    public static String getErrorMessage(Exception exception) {
        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException = (FirebaseAuthException) exception;
            switch (authException.getErrorCode()) {
                case "ERROR_NETWORK_REQUEST_FAILED":
                    return "Network connection failed. Please check your internet connection.";
                case "ERROR_USER_NOT_FOUND":
                    return "No account found with this email address.";
                case "ERROR_WRONG_PASSWORD":
                    return "Incorrect password. Please try again.";
                case "ERROR_EMAIL_ALREADY_IN_USE":
                    return "An account with this email already exists.";
                default:
                    return "Authentication failed: " + authException.getMessage();
            }
        } else if (exception instanceof FirebaseFirestoreException) {
            return "Database error: " + exception.getMessage();
        } else {
            return "An unexpected error occurred: " + exception.getMessage();
        }
    }
}
```

### 2. Offline Handling
```java
public class OfflineAuthHandler {
    public static void enableOfflineSupport() {
        FirebaseFirestore.getInstance()
            .enableNetwork()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d("Firestore", "Network enabled");
                } else {
                    Log.w("Firestore", "Failed to enable network", task.getException());
                }
            });
    }
    
    public static void handleOfflineState(Context context) {
        // Cache user profile locally
        // Show offline indicator
        // Queue operations for when online
    }
}
```

## Testing Strategy

### 1. Unit Tests
- AuthManager authentication methods
- UserProfile model validation
- FirestoreUserService CRUD operations
- Error handling scenarios
- AuthState transitions

### 2. Integration Tests
- Firebase Auth integration
- Firestore read/write operations
- SharedPreferences persistence
- Auto-login flow
- Logout functionality

### 3. UI Tests
- Login/logout user flows
- Auto-login on app restart
- Error message display
- Loading state management
- Navigation between auth and main screens

## Implementation Approach

### Phase 1: Core Infrastructure
1. Add Firestore dependency to build.gradle
2. Create AuthManager interface and implementation
3. Set up UserProfile and AuthState models
4. Create FirestoreUserService for database operations

### Phase 2: Authentication Enhancement
1. Implement persistent authentication state
2. Create enhanced Application class with AuthManager
3. Update SplashActivity with auto-login logic
4. Add SharedPreferences for auth state persistence

### Phase 3: User Interface Integration
1. Update existing auth activities to use AuthManager
2. Add logout functionality to MainActivity
3. Implement proper loading states and error handling
4. Add user profile management screens

### Phase 4: Database Integration
1. Set up Firestore security rules
2. Implement user profile creation on registration
3. Add user data synchronization
4. Implement offline support and caching

### Phase 5: Testing and Optimization
1. Add comprehensive unit and integration tests
2. Test offline scenarios and error handling
3. Optimize performance and memory usage
4. Add analytics and crash reporting

## Key Design Decisions

1. **Centralized AuthManager**: Single source of truth for authentication state across the app
2. **LiveData for State Management**: Reactive UI updates based on authentication state changes
3. **Firestore for User Data**: Scalable cloud database with offline support
4. **SharedPreferences for Local State**: Fast local storage for authentication preferences
5. **Singleton Pattern**: Ensure single AuthManager instance across the application
6. **Observer Pattern**: UI components observe auth state changes for automatic updates
7. **Error Handling Strategy**: Comprehensive error handling with user-friendly messages
8. **Security First**: Proper Firestore security rules and data validation