import { 
  useState, 
  type SubmitEvent as ReactSubmitEvent 
} from "react";

import { 
  Link, 
  useNavigate 
} from "react-router-dom";

import { 
  ApiRequestError,
  register,
} from "../api/AuthApi";

function RegisterPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  async function handleSubmit(event: ReactSubmitEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setFieldErrors({});

    if (password !== confirmPassword) {
      setFieldErrors({
        confirmPassword: "Passwords do not match",
      });
      return;
    }

    if (password.length < 8 || password.length > 30) {
      setFieldErrors({
        password:
          "Password must be between 8 and 30 characters",
      });
      return;
    }

    setIsSubmitting(true);

    /*
     * We will call Spring here next.
     */
    try {
      const response = await register(email, password);
      /*
      * Store the JWT so protected requests can use it later.
      * We will move this responsibility into AuthContext
      * when we create shared authentication state.
      */
      localStorage.setItem("authToken", response.token);
      localStorage.setItem("userEmail", response.email);

      // move on to the next page
      navigate("/preferences");
    } catch (caughtError) {
      if (caughtError instanceof ApiRequestError) {
        setError(caughtError.message);
        setFieldErrors(caughtError.fieldErrors);
      } else {
        setError("Unable to connect to Sightseer. Please try again.");
      } 
    }finally {
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
            Personalised discovery
          </p>

          <h1 className="mt-4 text-5xl font-bold leading-tight">
            Your version of London starts here.
          </h1>

          <p className="mt-6 text-lg leading-8 text-emerald-100">
            Tell us what interests you and discover attractions
            matched to your tastes across London.
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
            Create an account
          </p>

          <h2 className="mt-3 text-4xl font-bold tracking-tight text-stone-900">
            Start exploring London
          </h2>

          <p className="mt-3 text-stone-600">
            Already have an account?{" "}
            <Link
              to="/login"
              className="font-semibold text-emerald-700 hover:text-emerald-800"
            >
              Log in
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
                autoComplete="new-password"
                required
                minLength={8}
                maxLength={30}
                className="w-full rounded-xl border border-stone-300 bg-white px-4 py-3 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
                placeholder="8–30 characters"
              />
              {fieldErrors.password && (
                <p className="mt-2 text-sm text-red-600">
                  {fieldErrors.password}
                </p>
              )}
            </div>

            <div>
              <label
                htmlFor="confirm-password"
                className="mb-2 block text-sm font-semibold text-stone-700"
              >
                Confirm password
              </label>

              <input
                id="confirm-password"
                type="password"
                value={confirmPassword}
                onChange={(event) =>
                  setConfirmPassword(event.target.value)
                }
                autoComplete="new-password"
                required
                className="w-full rounded-xl border border-stone-300 bg-white px-4 py-3 outline-none transition focus:border-emerald-600 focus:ring-4 focus:ring-emerald-100"
                placeholder="Enter your password again"
              />
              {fieldErrors.confirmPassword && (
                <p className="mt-2 text-sm text-red-600">
                  {fieldErrors.confirmPassword}
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
                ? "Creating account..."
                : "Create account"}
            </button>
          </form>
        </div>
      </section>
    </main>
  );
}

export default RegisterPage;