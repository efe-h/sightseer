import { createContext } from "react";

// AuthResponse is the shape returned by the backend after login/register.
import type { AuthResponse } from "../types/auth";

// This defines the shared authentication state used across the app.
export interface AuthContextValue {
  // JWT/session token for authenticated API calls.
  token: string | null;
  // User's email address, or null if not logged in.
  email: string | null;
  // Whether the user is currently authenticated.
  isAuthenticated: boolean;
  // Stores the auth response after login/register.
  saveAuthentication: (
    response: AuthResponse,
  ) => void;
  // Clears stored auth data and logs the user out.
  logout: () => void;
}

// This gives any component access to the auth state without passing props manually.
export const AuthContext =
  createContext<AuthContextValue | undefined>(
    undefined,
  );