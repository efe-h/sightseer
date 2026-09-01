import {
  useState,
  type SubmitEvent as ReactSubmitEvent,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  ApiRequestError,
  login,
} from "../api/AuthApi";

import { useAuth } from "../hooks/useAuth";

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { saveAuthentication } = useAuth();

  async function handleSubmit(
    event: ReactSubmitEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setError("");
    setFieldErrors({});
    setIsSubmitting(true);

    try {
      const response = await login(email, password);

      saveAuthentication(response);

      navigate("/preferences");
    } catch (caughtError: unknown) {
      if (caughtError instanceof ApiRequestError) {
        setError(caughtError.message);
        setFieldErrors(caughtError.fieldErrors);
      } else {
        setError(
          "Unable to connect to Sightseer. Please try again.",
        );
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="flex min-h-screen bg-stone-50">
      <section className="hidden w-1/2 bg-emerald-900 p-12 text-white lg:flex lg:flex-col lg:justify-between">
        <Link
          to="/"
          className="text-2xl font-bold tracking-tight"
        >
          Sightseer
        </Link>

        <div className="max-w-lg">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-emerald-300">
            Welcome back
          </p>

          <h1 className="mt-4 text-5xl font-bold leading-tight">
            London is waiting to be rediscovered.
          </h1>

          <p className="mt-6 text-lg leading-8 text-emerald-100">
            Return to your personalised recommendations and
            continue exploring the parts of London that match
            you.
          </p>
        </div>

        <p className="text-sm text-emerald-200">
          Recommendations powered by your interests.
        </p>
      </section>

      <section className="flex w-full items-center justify-center px-6 py-12 lg:w-1/2">
        <div className="w-full max-w-md">
          <Link
            to="/"
            className="mb-10 block text-2xl font-bold text-emerald-800 lg:hidden"
          >
            Sightseer
          </Link>

          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-700">
            Welcome back
          </p>

          <h2 className="mt-3 text-4xl font-bold tracking-tight text-stone-900">
            Log in to Sightseer
          </h2>

          <p className="mt-3 text-stone-600">
            Don&apos;t have an account?{" "}
            <Link
              to="/register"
              className="font-semibold text-emerald-700 hover:text-emerald-800"
            >
              Create one
            </Link>
          </p>

          <form
            onSubmit={handleSubmit}
            className="mt-8 space-y-5"
          >
            <div>
              <label
                htmlFor="email"
                className="mb-2 block text-sm font-semibold text-stone-700"
              >
                Email address
              </label>

              <input
                id="email"
                type="email"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                autoComplete="email"
                required
                className="w-full rounded-xl border border-stone-300 bg-white px-4 py-3 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
                placeholder="you@example.com"
              />

              {fieldErrors.email && (
                <p className="mt-2 text-sm text-red-600">
                  {fieldErrors.email}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-2 block text-sm font-semibold text-stone-700"
              >
                Password
              </label>

              <input
                id="password"
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                autoComplete="current-password"
                required
                className="w-full rounded-xl border border-stone-300 bg-white px-4 py-3 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
                placeholder="Enter your password"
              />

              {fieldErrors.password && (
                <p className="mt-2 text-sm text-red-600">
                  {fieldErrors.password}
                </p>
              )}
            </div>

            {error && (
              <p
                role="alert"
                className="rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700"
              >
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full rounded-xl bg-emerald-700 px-5 py-3 font-semibold text-white shadow-sm transition hover:bg-emerald-800 disabled:cursor-not-allowed disabled:bg-emerald-400"
            >
              {isSubmitting
                ? "Logging in..."
                : "Log in"}
            </button>
          </form>
        </div>
      </section>
    </main>
  );
}

export default LoginPage;