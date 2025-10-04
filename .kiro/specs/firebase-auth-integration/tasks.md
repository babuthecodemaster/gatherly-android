# Implementation Plan

- [x] 1. Add missing Firebase Firestore dependency

  - Add firebase-firestore-ktx dependency to app/build.gradle.kts
  - Add firebase-storage-ktx dependency for future file uploads
  - Sync project and verify dependencies are resolved correctly
  - _Requirements: 1.1, 1.2_

- [x] 2. Create core authentication data models

  - [x] 2.1 Create UserProfile model class with Firestore annotations

    - Implement UserProfile class with all required fields (uid, email, displayName, etc.)
    - Add constructors including default constructor for Firestore
    - Add constructor that takes FirebaseUser parameter
    - Add getters and setters for all fields
    - _Requirements: 6.1, 6.2_

  - [x] 2.2 Create AuthState model for authentication state management

    - Implement AuthState class with Status enum (LOADING, AUTHENTICATED, UNAUTHENTICATED, ERROR)
    - Add static factory methods for creating different auth states
    - Include user, userProfile, and errorMessage fields
    - _Requirements: 2.1, 2.2, 5.1_

- [x] 3. Create Firestore user service for database operations

  - Create FirestoreUserService with FirebaseFirestore instance
  - Implement createUserProfile method to save user data to Firestore
  - Implement getUserProfile method to retrieve user data from Firestore
  - Implement updateUserProfile method with merge options
  - Add proper error handling and Task return types
  - _Requirements: 6.1, 6.2, 6.3_

- [x] 4. Create centralized AuthManager for persistent authentication

  - [x] 4.1 Define AuthManager interface

    - Create AuthManager interface with all required methods
    - Define methods for authentication state, operations, and user data
    - Include LiveData<AuthState> for reactive state management
    - Add auto-authentication and user profile methods
    - _Requirements: 2.1, 2.2, 4.1, 4.2_

  - [x] 4.2 Implement AuthManagerImpl class

    - Create AuthManagerImpl as singleton with getInstance method
    - Initialize FirebaseAuth, FirebaseFirestore, and SharedPreferences
    - Implement MutableLiveData<AuthState> for state management
    - Add Firebase Auth state listener for automatic state updates
    - Implement signIn method with Firestore user profile creation/update
    - Implement signUp method with user profile creation in Firestore
    - Implement signOut method with proper cleanup
    - Add checkAuthState method for auto-authentication
    - _Requirements: 2.1, 2.2, 3.1, 3.2, 4.1, 4.2_

- [x] 5. Create authentication preferences helper for persistent login

  - Create AuthPreferences class for SharedPreferences management
  - Define preference keys as constants (auto_login_enabled, last_login_email, etc.)
  - Implement methods to save and retrieve authentication preferences
  - Add methods for auto-login settings and user profile caching
  - _Requirements: 2.1, 2.3_

- [x] 6. Update MinimalApplication class with centralized auth management

  - Initialize Firebase in onCreate (already partially done)
  - Create AuthManager singleton instance
  - Set up global auth state checking
  - Add getter method for AuthManager access
  - _Requirements: 1.1, 1.3, 2.1_

- [x] 7. Create Firebase error handling utility












  - Create static method to convert Firebase exceptions to user-friendly messages
  - Handle FirebaseAuthException with specific error codes
  - Handle FirebaseFirestoreException for database errors
  - Add generic error handling for unknown exceptions
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 8. Update existing SplashActivity with auto-authentication






  - Get AuthManager instance from Application class
  - Observe AuthState LiveData for reactive navigation
  - Implement navigation logic based on authentication status
  - Add loading indicator during authentication check
  - Handle authentication errors with proper user feedback
  - Call authManager.checkAuthState() to trigger auto-login
  - _Requirements: 2.1, 2.2, 2.3, 2.4_

- [x] 9. Update existing authentication activities to use centralized AuthManager







  - [x] 9.1 Update FirebaseAuthActivity to use centralized AuthManager



    - Replace direct FirebaseAuth usage with AuthManager
    - Update login method to use authManager.signIn()
    - Update registration method to use authManager.signUp()
    - Observe AuthState for navigation and UI updates
    - Remove duplicate Firebase initialization code
    - _Requirements: 4.1, 4.2, 4.3_


  - [x] 9.2 Update other auth activities (MinimalFirebaseAuthActivity, UltraMinimalAuthActivity)

    - Apply same AuthManager integration pattern
    - Ensure consistent authentication flow across all auth activities
    - Remove duplicate Firebase initialization and auth handling
    - _Requirements: 4.1, 4.2_

- [x] 10. Add logout functionality to existing main activities






  - [x] 10.1 Update MainActivity with logout button and functionality



    - Add logout button to main activity layout
    - Implement logout confirmation dialog
    - Use authManager.signOut() for logout operation
    - Observe AuthState for automatic navigation to auth screen
    - Add proper error handling for logout failures
    - Add logout button click listener and make sure it's in the settings option
    - _Requirements: 3.1, 3.2, 3.3, 3.4_

  - [x] 10.2 Update other main activities (SimpleMainActivity, WorkingMainActivity)


    - Apply same logout functionality pattern
    - Ensure consistent logout behavior across all main activities
    - Add AuthState observation for automatic auth state handling
    - _Requirements: 3.1, 3.2, 3.3_

- [x] 11. Implement user profile management with Firestore






  - [x] 11.1 Add user profile creation on successful registration



    - Modify registration flow to create Firestore user profile
    - Handle user profile creation errors gracefully
    - Update user profile with last login timestamp on each login
    - _Requirements: 6.1, 6.2_

  - [x] 11.2 Add user profile retrieval and caching


    - Implement user profile loading on authentication
    - Cache user profile in SharedPreferences for offline access
    - Handle profile loading errors and provide fallback data
    - _Requirements: 6.2, 6.3_
