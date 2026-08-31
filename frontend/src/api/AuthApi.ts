import type {
  ApiErrorResponse,
  AuthResponse,
} from "../types/auth";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL ??
  "http://localhost:8080";

export class ApiRequestError extends Error {
  status: number;
  fieldErrors: Record<string, string>;

  constructor(response: ApiErrorResponse) {
    super(response.message);

    this.name = "ApiRequestError";
    this.status = response.status;
    this.fieldErrors = response.fieldErrors;
  }
}

export async function register(
  email: string,
  password: string,
): Promise<AuthResponse> {
  const response = await fetch(
    `${API_BASE_URL}/api/auth/register`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        email,
        password,
      }),
    },
  );

  const responseBody = await response.json();

  if (!response.ok) {
    throw new ApiRequestError(
      responseBody as ApiErrorResponse,
    );
  }

  return responseBody as AuthResponse;
}