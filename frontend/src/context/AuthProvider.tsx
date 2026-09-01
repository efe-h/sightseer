import {
  useState,
  type PropsWithChildren,
} from "react";

import type { AuthResponse } from "../types/auth";
import {
  AuthContext,
  type AuthContextValue,
} from "./AuthContext";

function AuthProvider({ children }: PropsWithChildren) {
  const [token, setToken] = useState<string | null>(
    () => localStorage.getItem("authToken"),
  );

  const [email, setEmail] = useState<string | null>(
    () => localStorage.getItem("userEmail"),
  );

  function saveAuthentication(
    response: AuthResponse,
  ) {
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
  }

  function logout() {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userEmail");

    setToken(null);
    setEmail(null);
  }

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