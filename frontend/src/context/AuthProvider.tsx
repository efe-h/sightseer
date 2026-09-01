import {
  useCallback,
  useEffect,
  useState,
  type PropsWithChildren,
} from "react";

import type { AuthResponse } from "../types/auth";
import {
  getTokenExpiration,
  isTokenExpired,
} from "../utils/jwt";
import {
  AuthContext,
  type AuthContextValue,
} from "./AuthContext";

function loadStoredToken() {
  const storedToken =
    localStorage.getItem("authToken");

  if (
    !storedToken ||
    isTokenExpired(storedToken)
  ) {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userEmail");

    return null;
  }

  return storedToken;
}

function AuthProvider({ children }: PropsWithChildren) {
  const [token, setToken] = useState<string | null>(
    loadStoredToken,
  );

  const [email, setEmail] = useState<string | null>(
    () =>
      token
        ? localStorage.getItem("userEmail")
        : null,
  );

  const saveAuthentication = useCallback(
    (response: AuthResponse) => {
      localStorage.setItem(
        "authToken",
        response.token,
      );

      localStorage.setItem(
        "userEmail",
        response.email,
      );

      setToken(response.token);
      setEmail(response.email);
    },
    [],
  );

  const logout = useCallback(() => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userEmail");

    setToken(null);
    setEmail(null);
  }, []);

  useEffect(() => {
    if (!token) {
      return;
    }

    const expiration = getTokenExpiration(token);

    /*
     * An invalid or already-expired token gets a delay of
     * zero, so logout runs asynchronously after this effect.
     */
    const remainingTime =
      expiration === null
        ? 0
        : Math.max(expiration - Date.now(), 0);

    const timeoutId = window.setTimeout(
      logout,
      remainingTime,
    );

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [token, logout]);

  const value: AuthContextValue = {
    token,
    email,
    isAuthenticated: token !== null,
    saveAuthentication,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export default AuthProvider;