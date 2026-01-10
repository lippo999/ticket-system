"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { logout } from "../services/auth.service";

export async function logoutAction() {
  const cookieStore = await cookies();
  const token = cookieStore.get("auth_token");

  // Call BE to blacklist token if exists
  if (token?.value) {
    try {
      await logout(token.value);
    } catch (error) {
      // Log error but continue with logout
      console.error("Failed to blacklist token:", error);
    }
  }

  // Delete cookies
  cookieStore.delete("auth_token");
  cookieStore.delete("username");

  // Redirect to login
  redirect("/");
}
