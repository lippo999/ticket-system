export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080",
  ENDPOINTS: {
    AUTH: {
      LOGIN: "/api/auth/login",  // Gateway routes to lb://AUTHSERVICE/api/auth/login
      LOGOUT: "/api/auth/logout",
      REFRESH: "/api/auth/refresh",
    },
  },
  TIMEOUT: 10000,
} as const;
