import {
  ApiRequestError,
} from "./AuthApi";

import type {
  ApiErrorResponse,
} from "../types/auth";

import type {
  RecommendationResponse,
} from "../types/recommendations";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  "http://localhost:8080";

export async function getRecommendations(
  token: string,
): Promise<RecommendationResponse> {
  const response = await fetch(
    `${API_BASE_URL}/api/recommendations`,
    {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "application/json",
      },
    },
  );

  const responseBody = await response.json();

  if (!response.ok) {
    throw new ApiRequestError(
      responseBody as ApiErrorResponse,
    );
  }

  return responseBody as RecommendationResponse;
}