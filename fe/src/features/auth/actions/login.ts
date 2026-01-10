"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export interface LoginState {
  error?: string;
  success?: boolean;
}

export async function loginAction(
  prevState: LoginState | null,
  formData: FormData
): Promise<LoginState> {
  const email = formData.get("email") as string;
  const password = formData.get("password") as string;
  const remember = formData.get("remember") === "on";

  // Validate input
  if (!email || !email.includes("@")) {
    return { error: "Invalid email address" };
  }
  if (!password || password.length < 6) {
    return { error: "Password must be at least 6 characters" };
  }

  // TODO: Replace with actual BE call to /api/auth/login
  // Mock authentication delay
  await new Promise((resolve) => setTimeout(resolve, 700));

  // Mock validation (replace with actual API call)
  const isValid = email.includes("@") && password.length >= 6;
  if (!isValid) {
    return { error: "Invalid credentials" };
  }

  // Mock JWT token (replace with actual token from BE)
  const mockToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.mock-token";

  // Set HTTP-only cookie
  const cookieStore = await cookies();
  const maxAge = remember ? 60 * 60 * 24 * 30 : 60 * 60 * 24; // 30 days or 1 day
  
  cookieStore.set("auth_token", mockToken, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    maxAge,
    path: "/",
  });

  // Optional: Set refresh token if your BE supports it
  // cookieStore.set("refresh_token", mockRefreshToken, { ... });

  // Redirect to dashboard/home after successful login
  redirect("/dashboard");
}
