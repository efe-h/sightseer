export interface ClusterRanking {
  cluster_id: number;
  cluster_label: string;
  average_match_score: number;
  rank: number;
}

export interface AttractionRecommendation {
  wikidata_id: string | null;
  name: string;
  category: string;
  summary: string;
  latitude: number;
  longitude: number;
  image_url: string | null;
  themes: string[];
  recommended_visit_time: string;
  estimated_visit_mins: number;
  indoor: boolean;
  family_friendly: boolean;
  price_level: string;
  borough_name: string;
  cluster_id: number;
  cluster_label: string;
  match_score: number;
}

export interface RecommendationResponse {
  cluster_rankings: ClusterRanking[];
  top_attractions: AttractionRecommendation[];
}