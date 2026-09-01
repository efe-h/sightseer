import {
  useEffect,
  useId,
  useRef,
  useState,
} from "react";

import type {
  AttractionRecommendation,
} from "../types/recommendations";

interface AttractionDetailsModalProps {
  attraction: AttractionRecommendation;
  onClose: () => void;
  onShowOnMap: (
    attraction: AttractionRecommendation,
  ) => void;
}

function AttractionDetailsModal({
  attraction,
  onClose,
  onShowOnMap,
}: AttractionDetailsModalProps) {
  const dialogRef =
    useRef<HTMLDialogElement | null>(null);

  const titleId = useId();

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

  useEffect(() => {
    const dialog = dialogRef.current;

    if (!dialog) {
      return;
    }

    dialog.showModal();

    function handleClose() {
      onClose();
    }

    dialog.addEventListener(
      "close",
      handleClose,
    );

    return () => {
      dialog.removeEventListener(
        "close",
        handleClose,
      );
    };
  }, [onClose]);

  function closeDialog() {
    dialogRef.current?.close();
  }

  function handleShowOnMap() {
    closeDialog();
    onShowOnMap(attraction);
  }

  return (
    <dialog
      ref={dialogRef}
      aria-labelledby={titleId}
      className="fixed inset-0 m-auto max-h-[90vh] w-[min(92vw,56rem)] overflow-y-auto rounded-3xl bg-white p-0 text-left shadow-2xl backdrop:bg-stone-950/65"
    >
      <div className="relative">
        {showImage ? (
          <img
            src={imageUrl}
            alt={attraction.name}
            onError={() => setImageFailed(true)}
            className="h-72 w-full object-cover sm:h-96"
          />
        ) : (
          <div className="flex h-72 items-center justify-center bg-gradient-to-br from-emerald-700 to-emerald-950 px-8 text-center text-white sm:h-96">
            <span className="text-3xl font-bold">
              {attraction.name}
            </span>
          </div>
        )}

        <button
          type="button"
          onClick={closeDialog}
          autoFocus
          aria-label="Close attraction details"
          className="absolute right-4 top-4 flex h-11 w-11 items-center justify-center rounded-full bg-white text-2xl font-bold text-stone-700 shadow-md hover:bg-stone-100 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-emerald-700"
        >
          ×
        </button>
      </div>

      <div className="p-6 sm:p-8">
        <div className="flex flex-wrap items-center gap-3">
          <span className="rounded-full bg-emerald-100 px-3 py-1 text-sm font-bold text-emerald-800">
            {attraction.match_score.toFixed(0)}%
            match
          </span>

          <span className="text-sm font-medium capitalize text-stone-500">
            {attraction.category.replaceAll(
              "_",
              " ",
            )}
          </span>

          <span className="text-sm text-stone-500">
            {attraction.borough_name}
          </span>
        </div>

        <h2
          id={titleId}
          className="mt-4 text-3xl font-bold tracking-tight text-stone-900 sm:text-4xl"
        >
          {attraction.name}
        </h2>

        <p className="mt-5 text-base leading-8 text-stone-600">
          {attraction.summary}
        </p>

        <div className="mt-6 flex flex-wrap gap-2">
          {attraction.themes.map((theme) => (
            <span
              key={theme}
              className="rounded-full bg-stone-100 px-3 py-1.5 text-sm font-medium capitalize text-stone-700"
            >
              {theme}
            </span>
          ))}
        </div>

        <dl className="mt-8 grid gap-5 border-y border-stone-200 py-6 sm:grid-cols-2 lg:grid-cols-4">
          <div>
            <dt className="text-sm text-stone-500">
              Recommended time
            </dt>

            <dd className="mt-1 font-bold capitalize text-stone-900">
              {attraction.recommended_visit_time}
            </dd>
          </div>

          <div>
            <dt className="text-sm text-stone-500">
              Duration
            </dt>

            <dd className="mt-1 font-bold text-stone-900">
              {attraction.estimated_visit_mins} minutes
            </dd>
          </div>

          <div>
            <dt className="text-sm text-stone-500">
              Setting
            </dt>

            <dd className="mt-1 font-bold text-stone-900">
              {attraction.indoor
                ? "Indoor"
                : "Outdoor"}
            </dd>
          </div>

          <div>
            <dt className="text-sm text-stone-500">
              Price
            </dt>

            <dd className="mt-1 font-bold text-stone-900">
              {attraction.price_level}
            </dd>
          </div>
        </dl>

        {attraction.family_friendly && (
          <p className="mt-6 rounded-xl bg-amber-50 px-4 py-3 font-medium text-amber-800">
            This attraction is family-friendly.
          </p>
        )}

        <div className="mt-8 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
          <button
            type="button"
            onClick={closeDialog}
            className="rounded-xl border border-stone-300 px-5 py-3 font-semibold text-stone-700 hover:bg-stone-50"
          >
            Close
          </button>

          <button
            type="button"
            onClick={handleShowOnMap}
            className="rounded-xl bg-emerald-700 px-5 py-3 font-semibold text-white hover:bg-emerald-800"
          >
            Show on map
          </button>
        </div>
      </div>
    </dialog>
  );
}

export default AttractionDetailsModal;