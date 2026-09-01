interface JwtPayload {
  exp?: number;
}

export function getTokenExpiration(
  token: string,
): number | null {
  try {
    const payloadPart = token.split(".")[1];

    if (!payloadPart) {
      return null;
    }

    /*
     * JWT uses URL-safe Base64, so convert it into
     * standard Base64 before decoding.
     */
    const base64 = payloadPart
      .replaceAll("-", "+")
      .replaceAll("_", "/");

    const paddedBase64 = base64.padEnd(
      Math.ceil(base64.length / 4) * 4,
      "=",
    );

    const payload = JSON.parse(
      atob(paddedBase64),
    ) as JwtPayload;

    return typeof payload.exp === "number"
      ? payload.exp * 1000
      : null;
  } catch {
    return null;
  }
}

export function isTokenExpired(
  token: string,
) {
  const expiration = getTokenExpiration(token);

  return (
    expiration === null ||
    expiration <= Date.now()
  );
}
