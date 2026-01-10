"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { login } from "../services/auth.service";
import type { ApiError } from "@/lib/api-client";

export interface LoginState {
  error?: string;
  success?: boolean;
}

export async function loginAction(
  prevState: LoginState | null,
  formData: FormData
): Promise<LoginState> {
  const username = formData.get("username") as string;
  const password = formData.get("password") as string;
  const remember = formData.get("remember") === "on";

  // Validate input
  if (!username || username.trim().length < 3) {
    return { error: "Username must be at least 3 characters" };
  }
  if (!password || password.length < 6) {
    return { error: "Password must be at least 6 characters" };
  }

  try {
    // Call actual BE endpoint
    const response = await login({
      username,
      password,
    });

    // Set HTTP-only cookie with JWT from BE
    const cookieStore = await cookies();
    const maxAge = remember ? 60 * 60 * 24 * 30 : 60 * 60 * 24; // 30 days or 1 day
    
    cookieStore.set("auth_token", response.token, {
      httpOnly: true,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge,
      path: "/",
    });

    // Optional: Store username in a separate non-httpOnly cookie for UI access
    cookieStore.set("username", response.username, {
      httpOnly: false,
      secure: process.env.NODE_ENV === "production",
      sameSite: "lax",
      maxAge,
      path: "/",
    });

    
  } catch (error) {
    const apiError = error as ApiError;
    return { 
      error: apiError.message || "Authentication failed. Please check your credentials." 
    };
  }
  console.log("Login successful, redirecting to /dashboard");
    // Redirect to dashboard after successful login
  redirect("/dashboard");
}
