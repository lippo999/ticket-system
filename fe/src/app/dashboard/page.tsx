import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { LogoutButton } from "@/features/auth/components/LogoutButton";

export default async function DashboardPage() {
  const cookieStore = await cookies();
  const token = cookieStore.get("auth_token");
  const username = cookieStore.get("username");

  console.log("DashboardPage: Retrieved token from cookies:", token);
  // Protect route: redirect to login if no token
  if (!token) {
    redirect("/");
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-zinc-50 via-white to-blue-50 p-8 dark:from-black dark:via-zinc-950 dark:to-blue-950/40">
      <div className="mx-auto max-w-4xl">
        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-zinc-900 dark:text-white">
              Dashboard
            </h1>
            {username && (
              <p className="mt-1 text-sm text-zinc-600 dark:text-zinc-400">
                Welcome back, <span className="font-medium">{username.value}</span>
              </p>
            )}
          </div>
          <LogoutButton />
        </div>

        <div className="space-y-4">
          <div className="rounded-lg border border-zinc-200 bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900">
            <h2 className="mb-2 text-lg font-semibold text-zinc-900 dark:text-white">
              Authentication Status
            </h2>
            <p className="text-sm text-zinc-600 dark:text-zinc-400">
              You are logged in! Token stored in HTTP-only cookie.
            </p>
            <div className="mt-4 rounded-md bg-zinc-50 p-3 dark:bg-zinc-950">
              <p className="text-xs font-mono text-zinc-500 dark:text-zinc-400">
                Token (first 50 chars): {token.value.substring(0, 50)}...
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
