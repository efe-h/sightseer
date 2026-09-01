import {
  useEffect,
  useRef,
} from "react";

import {
  CircleMarker,
  MapContainer,
  Popup,
  TileLayer,
  useMap,
} from "react-leaflet";

import type {
  CircleMarker as LeafletCircleMarker,
  LatLngBoundsExpression,
} from "leaflet";

import type {
  AttractionRecommendation,
} from "../types/recommendations";

interface RecommendationMapProps {
  attractions: AttractionRecommendation[];
  selectedClusterId: number | null;
  selectedAttraction:
    AttractionRecommendation | null;
  onSelectCluster: (clusterId: number) => void;
  onSelectAttraction: (
    attraction: AttractionRecommendation,
  ) => void;
}

interface FitMapBoundsProps {
  attractions: AttractionRecommendation[];
}

function clusterColour(clusterId: number) {
  /*
   * Multiplying by 137 distributes neighbouring cluster IDs
   * around the colour wheel.
   */
  const hue = (clusterId * 137) % 360;

  return `hsl(${hue} 65% 42%)`;
}

interface FocusSelectedAttractionProps {
  attraction: AttractionRecommendation | null;
}

function FocusSelectedAttraction({
  attraction,
}: FocusSelectedAttractionProps) {
  const map = useMap();

  useEffect(() => {
    if (!attraction) {
      return;
    }

    map.flyTo(
      [
        attraction.latitude,
        attraction.longitude,
      ],
      Math.max(map.getZoom(), 14),
      {
        animate: true,
        duration: 0.8,
      },
    );
  }, [attraction, map]);

  return null;
}

function FitMapBounds({
  attractions,
}: FitMapBoundsProps) {
  const map = useMap();

  useEffect(() => {
    if (attractions.length === 0) {
      return;
    }

    const bounds: LatLngBoundsExpression =
      attractions.map((attraction) => [
        attraction.latitude,
        attraction.longitude,
      ]);

    map.fitBounds(bounds, {
      padding: [30, 30],
      maxZoom: 13,
    });
  }, [attractions, map]);

  return null;
}

interface AttractionMarkerProps {
  attraction: AttractionRecommendation;
  isClusterSelected: boolean;
  isAttractionSelected: boolean;
  onSelectCluster: (clusterId: number) => void;
  onSelectAttraction: (
    attraction: AttractionRecommendation,
  ) => void;
}

function AttractionMarker({
  attraction,
  isClusterSelected,
  isAttractionSelected,
  onSelectCluster,
  onSelectAttraction,
}: AttractionMarkerProps) {
  const markerRef =
    useRef<LeafletCircleMarker | null>(null);

  useEffect(() => {
    if (isAttractionSelected) {
      markerRef.current?.openPopup();
    }
  }, [isAttractionSelected]);

  const colour = clusterColour(
    attraction.cluster_id,
  );

  return (
    <CircleMarker
      ref={markerRef}
      center={[
        attraction.latitude,
        attraction.longitude,
      ]}
      radius={
        isAttractionSelected
          ? 13
          : isClusterSelected
            ? 9
            : 6
      }
      pathOptions={{
        color: colour,
        fillColor: colour,
        fillOpacity: isClusterSelected
          ? 0.9
          : 0.2,
        opacity: isClusterSelected
          ? 1
          : 0.3,
        weight: isAttractionSelected
          ? 4
          : isClusterSelected
            ? 3
            : 1,
      }}
      eventHandlers={{
        click: () => {
          onSelectCluster(
            attraction.cluster_id,
          );

          onSelectAttraction(attraction);
        },
      }}
    >
      <Popup>
        <div className="min-w-48">
          <p className="font-bold">
            {attraction.name}
          </p>

          <p className="mt-1">
            {attraction.borough_name}
          </p>

          <p className="mt-1 font-semibold text-emerald-700">
            {attraction.match_score.toFixed(0)}%
            match
          </p>
        </div>
      </Popup>
    </CircleMarker>
  );
}

function RecommendationMap({
  attractions,
  selectedClusterId,
  selectedAttraction,
  onSelectCluster,
  onSelectAttraction,
}: RecommendationMapProps) {
  return (
    <section
      aria-label="Map of recommended London attractions"
      className="overflow-hidden rounded-2xl border border-stone-200 bg-white shadow-sm"
    >
      <MapContainer
        center={[51.5074, -0.1278]}
        zoom={11}
        scrollWheelZoom
        className="h-[520px] w-full"
      >
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
        />

        <FitMapBounds attractions={attractions} />

        <FocusSelectedAttraction
          attraction={selectedAttraction}
        />

        {attractions.map((attraction) => {
          const isClusterSelected =
            selectedClusterId === null ||
            attraction.cluster_id === selectedClusterId;

          const isAttractionSelected =
            selectedAttraction === attraction;

          return (
            <AttractionMarker
              key={
                attraction.wikidata_id ??
                `${attraction.cluster_id}-${attraction.name}`
              }
              attraction={attraction}
              isClusterSelected={isClusterSelected}
              isAttractionSelected={isAttractionSelected}
              onSelectCluster={onSelectCluster}
              onSelectAttraction={onSelectAttraction}
            />
          );
        })}
      </MapContainer>
    </section>
  );
}

export default RecommendationMap;