import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword,
  signOut,
  onAuthStateChanged,
  User as FirebaseUser,
  updateProfile
} from "firebase/auth";
import { auth } from "./firebase";

export interface FirebaseAuthUser {
  id: string;
  email: string;
  username: string;
  avatar?: string;
  status: "online" | "offline" | "away" | "busy";
}

export const firebaseAuth = {
  async login(email: string, password: string): Promise<FirebaseAuthUser> {
    const userCredential = await signInWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    
    return {
      id: user.uid,
      email: user.email!,
      username: user.displayName || email.split('@')[0],
      avatar: user.photoURL || undefined,
      status: "online" as const
    };
  },

  async register(username: string, email: string, password: string): Promise<FirebaseAuthUser> {
    const userCredential = await createUserWithEmailAndPassword(auth, email, password);
    const user = userCredential.user;
    
    // Update the user's display name
    await updateProfile(user, {
      displayName: username
    });

    return {
      id: user.uid,
      email: user.email!,
      username: username,
      avatar: user.photoURL || undefined,
      status: "online" as const
    };
  },

  async logout(): Promise<void> {
    await signOut(auth);
  },

  getCurrentUser(): Promise<FirebaseAuthUser | null> {
    return new Promise((resolve) => {
      const unsubscribe = onAuthStateChanged(auth, (user) => {
        unsubscribe();
        if (user) {
          resolve({
            id: user.uid,
            email: user.email!,
            username: user.displayName || user.email!.split('@')[0],
            avatar: user.photoURL || undefined,
            status: "online" as const
          });
        } else {
          resolve(null);
        }
      });
    });
  },

  onAuthStateChanged(callback: (user: FirebaseAuthUser | null) => void) {
    return onAuthStateChanged(auth, (user) => {
      if (user) {
        callback({
          id: user.uid,
          email: user.email!,
          username: user.displayName || user.email!.split('@')[0],
          avatar: user.photoURL || undefined,
          status: "online" as const
        });
      } else {
        callback(null);
      }
    });
  }
};