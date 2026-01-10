"use client";

import { useFormStatus } from "react-dom";
import { logoutAction } from "../actions/logout";

function LogoutButtonInner() {
  const { pending } = useFormStatus();
  
  return (
    <button
      type="submit"
      disabled={pending}
      className="inline-flex items-center gap-2 rounded-lg border border-red-300 bg-white px-4 py-2 text-sm font-medium text-red-700 shadow-sm transition hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50 dark:border-red-800 dark:bg-zinc-900 dark:text-red-400 dark:hover:bg-red-950/30"
    >
      {pending ? "Logging out..." : "Logout"}
    </button>
  );
}

export function LogoutButton() {
  return (
    <form action={logoutAction}>
      <LogoutButtonInner />
    </form>
  );
}
