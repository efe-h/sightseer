import { Link } from "react-router-dom";

function LandingPage() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-stone-50 px-6">
      <section className="max-w-2xl text-center">
        <p className="mb-4 text-sm font-semibold uppercase tracking-[0.25em] text-emerald-700">
          Personalised London discovery
        </p>

        <h1 className="text-5xl font-bold tracking-tight text-stone-900 sm:text-6xl">
          Discover the London that matches you.
        </h1>

        <p className="mx-auto mt-6 max-w-xl text-lg leading-8 text-stone-600">
          Rate your interests and explore personalised attractions
          across London’s most distinctive neighbourhoods.
        </p>

        <div className="mt-8 flex justify-center gap-4">
          <Link
            to="/register"
            className="rounded-full bg-emerald-700 px-7 py-3 font-semibold text-white shadow-sm transition hover:bg-emerald-800"
          >
            Get started
          </Link>

          <Link
            to="/login"
            className="rounded-full border border-stone-300 px-7 py-3 font-semibold text-stone-700 transition hover:bg-stone-100"
          >
            Log in
          </Link>
        </div>
      </section>
    </main>
  );
}

export default LandingPage;