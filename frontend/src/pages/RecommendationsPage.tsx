import {
  useEffect,
  useState,
} from "react";

import {
  Link,
  useNavigate,
} from "react-router-dom";

import {
  ApiRequestError,
} from "../api/AuthApi";

import {
  getRecommendations,
} from "../api/recommendationsApi";

import AttractionCard from "../components/AttractionCard";
import { useAuth } from "../hooks/useAuth";

import type {
  RecommendationResponse,
} from "../types/recommendations";

const INITIAL_CLUSTER_COUNT = 5;

function getErrorMessage(error: ApiRequestError) {
  switch (error.status) {
    case 404:
      return "You need to save your interests before generating recommendations.";

    case 502:
      return "The recommendation service returned an invalid response.";

    case 503:
      return "The recommendation service is temporarily unavailable.";

    case 504:
      return "Generating recommendations took too long. Please try again.";

    default:
      return error.message;
  }
}

function RecommendationsPage() {
  const navigate = useNavigate();

  const {
    token,
    email,
    logout,
  } = useAuth();

  const [recommendations, setRecommendations] =
    useState<RecommendationResponse | null>(null);

  const [visibleClusterCount, setVisibleClusterCount] =
    useState(INITIAL_CLUSTER_COUNT);

  const [isLoading, setIsLoading] =
    useState(true);

  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) {
      return;
    }

    let cancelled = false;

    async function loadRecommendations() {
      try {
        const response =
          await getRecommendations(token!);

        if (!cancelled) {
          setRecommendations(response);
        }
      } catch (caughtError: unknown) {
        if (!cancelled) {
          setError(
            caughtError instanceof ApiRequestError
              ? getErrorMessage(caughtError)
              : "Unable to load your recommendations.",
          );
        }
      } finally {
        if (!cancelled) {
          setIsLoading(false);
        }
      }
    }

    loadRecommendations();

    return () => {
      cancelled = true;
    };
  }, [token]);

  function handleLogout() {
    logout();
    navigate("/login");
  }

  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-stone-50">
        <div className="text-center">
          <div className="mx-auto h-10 w-10 animate-spin rounded-full border-4 border-emerald-200 border-t-emerald-700" />

          <p className="mt-4 text-lg font-medium text-stone-600">
            Finding your best London matches...
          </p>
        </div>
      </main>
    );
  }

  if (error || !recommendations) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-stone-50 px-6">
        <section className="max-w-lg rounded-2xl border border-red-100 bg-white p-8 text-center shadow-sm">
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-red-600">
            Recommendations unavailable
          </p>

          <h1 className="mt-3 text-3xl font-bold text-stone-900">
            We couldn’t load your results
          </h1>

          <p className="mt-4 leading-7 text-stone-600">
            {error}
          </p>

          <div className="mt-6 flex flex-col justify-center gap-3 sm:flex-row">
            <button
              type="button"
              onClick={() => window.location.reload()}
              className="rounded-xl bg-emerald-700 px-5 py-3 font-semibold text-white hover:bg-emerald-800"
            >
              Try again
            </button>

            <Link
              to="/preferences"
              className="rounded-xl border border-stone-300 px-5 py-3 font-semibold text-stone-700 hover:bg-stone-50"
            >
              Update interests
            </Link>
          </div>
        </section>
      </main>
    );
  }

  const {
    cluster_rankings: clusterRankings,
    top_attractions: topAttractions,
  } = recommendations;

  const bestCluster = clusterRankings[0];

  if (!bestCluster) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-stone-50">
        <p className="text-lg text-stone-600">
          No recommendations were returned.
        </p>
      </main>
    );
  }

  const visibleClusters = clusterRankings.slice(
    0,
    visibleClusterCount,
  );

  const hasMoreClusters =
    visibleClusterCount < clusterRankings.length;

  return (
    <main className="min-h-screen bg-stone-50">
      <header className="border-b border-stone-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">
          <Link
            to="/"
            className="text-2xl font-bold tracking-tight text-emerald-800"
          >
            Sightseer
          </Link>

          <nav className="flex items-center gap-5">
            <Link
              to="/preferences"
              className="text-sm font-semibold text-stone-700 hover:text-emerald-700"
            >
              Preferences
            </Link>

            <span className="hidden text-sm text-stone-500 md:block">
              {email}
            </span>

            <button
              type="button"
              onClick={handleLogout}
              className="text-sm font-semibold text-stone-700 hover:text-emerald-700"
            >
              Log out
            </button>
          </nav>
        </div>
      </header>

      <section className="bg-emerald-900 px-6 py-14 text-white">
        <div className="mx-auto max-w-7xl">
          <p className="text-sm font-semibold uppercase tracking-[0.25em] text-emerald-300">
            Your strongest match
          </p>

          <div className="mt-4 flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
            <div className="max-w-4xl">
              <p className="text-lg font-semibold text-emerald-200">
                #{bestCluster.rank}
              </p>

              <h1 className="mt-2 text-4xl font-bold leading-tight sm:text-5xl">
                {bestCluster.cluster_label}
              </h1>
            </div>

            <div className="shrink-0 rounded-2xl bg-white/10 px-6 py-4 backdrop-blur">
              <p className="text-sm text-emerald-200">
                Average match
              </p>

              <p className="mt-1 text-4xl font-bold">
                {bestCluster.average_match_score.toFixed(0)}%
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-7xl px-6 py-12">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.2em] text-emerald-700">
              Ranked recommendations
            </p>

            <h2 className="mt-2 text-3xl font-bold tracking-tight text-stone-900">
              Explore your best-matching areas
            </h2>
          </div>

          <Link
            to="/preferences"
            className="font-semibold text-emerald-700 hover:text-emerald-800"
          >
            Change my interests
          </Link>
        </div>

        <div className="mt-10 space-y-14">
          {visibleClusters.map((cluster) => {
            const clusterAttractions =
              topAttractions.filter(
                (attraction) =>
                  attraction.cluster_id ===
                  cluster.cluster_id,
              );

            return (
              <section
                key={cluster.cluster_id}
                id={`cluster-${cluster.cluster_id}`}
              >
                <div className="mb-6 flex flex-col gap-3 border-b border-stone-200 pb-5 sm:flex-row sm:items-end sm:justify-between">
                  <div>
                    <p className="text-sm font-bold text-emerald-700">
                      Rank #{cluster.rank}
                    </p>

                    <h3 className="mt-1 text-2xl font-bold text-stone-900">
                      {cluster.cluster_label}
                    </h3>
                  </div>

                  <p className="text-lg font-bold text-stone-700">
                    {cluster.average_match_score.toFixed(0)}%
                    <span className="ml-1 text-sm font-normal text-stone-500">
                      average match
                    </span>
                  </p>
                </div>

                <div className="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
                  {clusterAttractions.map(
                    (attraction) => (
                      <AttractionCard
                        key={
                          attraction.wikidata_id ??
                          `${attraction.cluster_id}-${attraction.name}`
                        }
                        attraction={attraction}
                      />
                    ),
                  )}
                </div>
              </section>
            );
          })}
        </div>

        {hasMoreClusters && (
          <div className="mt-12 text-center">
            <button
              type="button"
              onClick={() =>
                setVisibleClusterCount(
                  (current) => current + 5,
                )
              }
              className="rounded-xl border border-emerald-700 px-6 py-3 font-semibold text-emerald-800 transition hover:bg-emerald-50"
            >
              Show more clusters
            </button>
          </div>
        )}
      </section>
    </main>
  );
}

export default RecommendationsPage;