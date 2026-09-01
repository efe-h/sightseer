import {
  ApiRequestError,
} from "./AuthApi";

import type {
  ApiErrorResponse,
} from "../types/auth";

import type {
  Preferences,
} from "../types/preferences";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  "http://localhost:8080";

export async function getPreferences(
  token: string,
): Promise<Preferences> {
  const response = await fetch(
    `${API_BASE_URL}/api/mypreferences`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );

  const responseBody = await response.json();

  if (!response.ok) {
    throw new ApiRequestError(
      responseBody as ApiErrorResponse,
    );
  }

  return responseBody as Preferences;
}

export async function savePreferences(
  token: string,
  preferences: Preferences,
): Promise<Preferences> {
  const response = await fetch(
    `${API_BASE_URL}/api/mypreferences`,
    {
      method: "PUT",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify(preferences),
    },
  );

  const responseBody = await response.json();

  if (!response.ok) {
    throw new ApiRequestError(
      responseBody as ApiErrorResponse,
    );
  }

  return responseBody as Preferences;
}