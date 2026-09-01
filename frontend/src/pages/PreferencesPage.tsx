import {
  useEffect,
  useState,
  type SubmitEvent as ReactSubmitEvent,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  ApiRequestError,
} from "../api/AuthApi";

import {
  getPreferences,
  savePreferences,
} from "../api/preferencesApi";

import { useAuth } from "../hooks/useAuth";

import type {
  PreferenceName,
  Preferences,
} from "../types/preferences";

const DEFAULT_PREFERENCES: Preferences = {
  history: 1,
  art: 1,
  architecture: 1,
  nature: 1,
  science: 1,
  food: 1,
  entertainment: 1,
  shopping: 1,
  views: 1,
  family: 1,
};

const INTERESTS: Array<{
  name: PreferenceName;
  label: string;
  description: string;
}> = [
  {
    name: "history",
    label: "History",
    description: "Historic places, stories and heritage",
  },
  {
    name: "art",
    label: "Art",
    description: "Galleries, exhibitions and creative spaces",
  },
  {
    name: "architecture",
    label: "Architecture",
    description: "Distinctive buildings and urban design",
  },
  {
    name: "nature",
    label: "Nature",
    description: "Parks, gardens and natural spaces",
  },
  {
    name: "science",
    label: "Science",
    description: "Scientific discovery and technology",
  },
  {
    name: "food",
    label: "Food",
    description: "Markets, culinary culture and local flavours",
  },
  {
    name: "entertainment",
    label: "Entertainment",
    description: "Performances, activities and nightlife",
  },
  {
    name: "shopping",
    label: "Shopping",
    description: "Markets, independent shops and retail",
  },
  {
    name: "views",
    label: "Views",
    description: "Skylines, viewpoints and scenic locations",
  },
  {
    name: "family",
    label: "Family",
    description: "Attractions suitable for family visits",
  },
];

function PreferencesPage() {
  const navigate = useNavigate();

  const {
    token,
    email,
    logout,
  } = useAuth();

  const [preferences, setPreferences] =
    useState<Preferences>(DEFAULT_PREFERENCES);

  const [
    hasExistingPreferences,
    setHasExistingPreferences,
  ] = useState(false);

  const [isLoading, setIsLoading] =
    useState(true);

  const [isSubmitting, setIsSubmitting] =
    useState(false);

  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) {
      return;
    }

    /*
     * React may remove a component before an asynchronous
     * request finishes. This flag prevents an old request
     * from updating a component that is no longer active.
     */
    let cancelled = false;

    async function loadPreferences() {
      try {
        const savedPreferences =
          await getPreferences(token!);

        if (!cancelled) {
          setPreferences(savedPreferences);
          setHasExistingPreferences(true);
        }
      } catch (caughtError: unknown) {
        if (
          caughtError instanceof ApiRequestError &&
          caughtError.status === 404
        ) {
          /*
           * A new user has no saved preferences yet.
           * Keep the default scores and show the form.
           */
          return;
        }

        if (!cancelled) {
          setError(
            caughtError instanceof ApiRequestError
              ? caughtError.message
              : "Unable to load your preferences.",
          );
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    loadPreferences();

    return () => {
      cancelled = true;
    };
  }, [token]);

  function updatePreference(
    name: PreferenceName,
    value: number,
  ) {
    setPreferences((currentPreferences) => ({
      ...currentPreferences,
      [name]: value,
    }));
  }

  async function handleSubmit(
    event: ReactSubmitEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    if (!token) {
      return;
    }

    setError("");
    setIsSubmitting(true);

    try {
      await savePreferences(token, preferences);
      navigate("/recommendations");
    } catch (caughtError: unknown) {
      setError(
        caughtError instanceof ApiRequestError
          ? caughtError.message
          : "Unable to save your preferences.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleLogout() {
    logout();
    navigate("/login");
  }

  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-stone-50">
        <p className="text-lg text-stone-600">
          Loading your preferences...
        </p>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-stone-50">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
          <Link
            to="/"
            className="text-2xl font-bold tracking-tight text-emerald-800"
          >
            Sightseer
          </Link>

          <div className="flex items-center gap-4">
            <span className="hidden text-sm text-stone-500 sm:block">
              {email}
            </span>

            <button
              type="button"
              onClick={handleLogout}
              className="text-sm font-semibold text-stone-700 hover:text-emerald-700"
            >
              Log out
            </button>
          </div>
        </div>
      </header>

      <section className="mx-auto max-w-4xl px-6 py-12">
        <p className="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-700">
          Personalise your experience
        </p>

        <h1 className="mt-3 text-4xl font-bold tracking-tight text-stone-900">
          What interests you?
        </h1>

        <p className="mt-4 max-w-2xl text-lg leading-8 text-stone-600">
          Score each interest from 1 to 5. We’ll use your
          answers to find the London attractions and areas
          that match you best.
        </p>

        <div className="mt-6 flex justify-between text-sm text-stone-500">
          <span>1 — Not interested</span>
          <span>5 — Love it</span>
        </div>

        <form
          onSubmit={handleSubmit}
          className="mt-8"
        >
          <div className="grid gap-5 md:grid-cols-2">
            {INTERESTS.map((interest) => (
              <fieldset
                key={interest.name}
                className="rounded-2xl border border-stone-200 bg-white p-5 shadow-sm"
              >
                <legend className="sr-only">
                  {interest.label}
                </legend>

                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h2 className="text-lg font-bold text-stone-900">
                      {interest.label}
                    </h2>

                    <p className="mt-1 text-sm text-stone-500">
                      {interest.description}
                    </p>
                  </div>

                  <span
                    aria-live="polite"
                    className="shrink-0 rounded-full bg-emerald-100 px-3 py-1 text-sm font-bold text-emerald-800"
                  >
                    {preferences[interest.name]} / 5
                  </span>
                </div>

                <div className="mt-5 grid grid-cols-5 gap-2">
                  {[1, 2, 3, 4, 5].map((score) => {
                    const isSelected =
                      preferences[interest.name] === score;

                    return (
                      <button
                        key={score}
                        type="button"
                        onClick={() =>
                          updatePreference(
                            interest.name,
                            score,
                          )
                        }
                        aria-pressed={isSelected}
                        aria-label={`${interest.label}: ${score} out of 5`}
                        className={
                          isSelected
                            ? "scale-105 rounded-lg bg-emerald-700 py-2.5 font-bold text-white shadow-md ring-2 ring-emerald-300 ring-offset-2 transition"
                            : "rounded-lg border border-stone-200 bg-stone-50 py-2.5 font-semibold text-stone-600 transition hover:border-emerald-400 hover:bg-emerald-50"
                        }
                      >
                        {score}
                      </button>
                    );
                  })}
                </div>
              </fieldset>
            ))}
          </div>

          {error && (
            <p
              role="alert"
              className="mt-6 rounded-xl bg-red-50 px-4 py-3 text-sm text-red-700"
            >
              {error}
            </p>
          )}

          <div className="mt-8 flex flex-col-reverse gap-3 sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm text-stone-500">
              {hasExistingPreferences
                ? "Your previous scores have been loaded."
                : "All interests currently start at a score of 1."}
            </p>

            <button
              type="submit"
              disabled={isSubmitting}
              className="rounded-xl bg-emerald-700 px-6 py-3 font-semibold text-white shadow-sm transition hover:bg-emerald-800 disabled:cursor-not-allowed disabled:bg-emerald-400"
            >
              {isSubmitting
                ? "Saving..."
                : hasExistingPreferences
                  ? "Update recommendations"
                  : "Find my recommendations"}
            </button>
          </div>
        </form>
      </section>
    </main>
  );
}

export default PreferencesPage;