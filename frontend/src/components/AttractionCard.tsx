import { useState } from "react";

import type {
  AttractionRecommendation,
} from "../types/recommendations";

interface AttractionCardProps {
  attraction: AttractionRecommendation;
  isSelected: boolean;
  onSelect: (
    attraction: AttractionRecommendation,
  ) => void;
}

function formatCategory(category: string) {
  return category
    .replaceAll("_", " ")
    .replace(/\b\w/g, (letter) =>
      letter.toUpperCase(),
    );
}

function AttractionCard({
  attraction,
  isSelected,
  onSelect,
}: AttractionCardProps) {
  const [imageFailed, setImageFailed] =
    useState(false);

  const imageUrl = attraction.image_url?.replace(
    /^http:/,
    "https:",
  );

  const showImage =
    imageUrl !== undefined &&
    imageUrl !== null &&
    !imageFailed;

  return (
    <article
      role="button"
      tabIndex={0}
      aria-label={`Show ${attraction.name} on the map`}
      onClick={() => onSelect(attraction)}
      onKeyDown={(event) => {
        if (
          event.key === "Enter" ||
          event.key === " "
        ) {
          event.preventDefault();
          onSelect(attraction);
        }
      }}
      className={
        isSelected
          ? "cursor-pointer overflow-hidden rounded-2xl border border-emerald-600 bg-white shadow-lg ring-4 ring-emerald-200 transition"
          : "cursor-pointer overflow-hidden rounded-2xl border border-stone-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg focus-visible:outline-2 focus-visible:outline-offset-4 focus-visible:outline-emerald-700"
      }
    >
      <div className="relative aspect-[16/10] overflow-hidden bg-emerald-100">
        {showImage ? (
          <img
            src={imageUrl}
            alt={attraction.name}
            onError={() => setImageFailed(true)}
            className="h-full w-full object-cover"
          />
        ) : (
          <div className="flex h-full items-center justify-center bg-gradient-to-br from-emerald-700 to-emerald-900 px-6 text-center text-white">
            <span className="text-xl font-bold">
              {attraction.name}
            </span>
          </div>
        )}

        <span className="absolute right-3 top-3 rounded-full bg-white/95 px-3 py-1.5 text-sm font-bold text-emerald-800 shadow-sm">
          {attraction.match_score.toFixed(0)}% match
        </span>
      </div>

      <div className="p-5">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="rounded-full bg-emerald-50 px-2.5 py-1 font-semibold text-emerald-800">
            {formatCategory(attraction.category)}
          </span>

          <span className="text-stone-500">
            {attraction.borough_name}
          </span>
        </div>

        <h3 className="mt-3 text-xl font-bold tracking-tight text-stone-900">
          {attraction.name}
        </h3>

        <p className="mt-3 line-clamp-4 text-sm leading-6 text-stone-600">
          {attraction.summary}
        </p>

        <div className="mt-4 flex flex-wrap gap-2">
          {attraction.themes.map((theme) => (
            <span
              key={theme}
              className="rounded-full bg-stone-100 px-2.5 py-1 text-xs font-medium capitalize text-stone-600"
            >
              {theme}
            </span>
          ))}
        </div>

        <dl className="mt-5 grid grid-cols-2 gap-x-4 gap-y-3 border-t border-stone-100 pt-4 text-sm">
          <div>
            <dt className="text-stone-500">
              Visit time
            </dt>

            <dd className="mt-1 font-semibold capitalize text-stone-800">
              {attraction.recommended_visit_time}
            </dd>
          </div>

          <div>
            <dt className="text-stone-500">
              Duration
            </dt>

            <dd className="mt-1 font-semibold text-stone-800">
              {attraction.estimated_visit_mins} mins
            </dd>
          </div>

          <div>
            <dt className="text-stone-500">
              Setting
            </dt>

            <dd className="mt-1 font-semibold text-stone-800">
              {attraction.indoor
                ? "Indoor"
                : "Outdoor"}
            </dd>
          </div>

          <div>
            <dt className="text-stone-500">
              Price
            </dt>

            <dd className="mt-1 font-semibold text-stone-800">
              {attraction.price_level}
            </dd>
          </div>
        </dl>

        {attraction.family_friendly && (
          <p className="mt-4 rounded-lg bg-amber-50 px-3 py-2 text-sm font-medium text-amber-800">
            Family-friendly
          </p>
        )}

        <p className="mt-4 text-sm font-semibold text-emerald-700">
          {isSelected
            ? "Selected on map"
            : "Select to show on map"}
        </p>
      </div>
    </article>
  );
}

export default AttractionCard;