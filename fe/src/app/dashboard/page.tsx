import { cookies } from "next/headers";
import { redirect } from "next/navigation";

export default async function DashboardPage() {
  const cookieStore = await cookies();
  const token = cookieStore.get("auth_token");

  console.log("DashboardPage: Retrieved token from cookies:", token);
  // Protect route: redirect to login if no token
  if (!token) {
    redirect("/");
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-zinc-50 via-white to-blue-50 p-8 dark:from-black dark:via-zinc-950 dark:to-blue-950/40">
      <div className="mx-auto max-w-4xl">
        <h1 className="mb-4 text-3xl font-bold text-zinc-900 dark:text-white">
          Dashboard
        </h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          You are logged in! Token stored in HTTP-only cookie.
        </p>
        <div className="mt-6 rounded-lg border border-zinc-200 bg-white p-6 dark:border-zinc-800 dark:bg-zinc-900">
          <p className="text-sm text-zinc-500 dark:text-zinc-400">
            Token (first 50 chars): {token.value.substring(0, 50)}...
          </p>
        </div>
      </div>
    </div>
  );
}
