
import { LoginForm } from "../features/auth/components/LoginForm";

export default function Home() {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-zinc-50 via-white to-blue-50 px-4 py-10 font-sans dark:from-black dark:via-zinc-950 dark:to-blue-950/40">
      <LoginForm />
    </div>
  );
}
