from typing import Any

from pydantic import BaseModel, Field

# ensures that the user preference values are between 1 and 5 before they are passed to the recommendation engine

class PreferenceRequest(BaseModel):
    history: int = Field(ge=1, le=5)
    art: int = Field(ge=1, le=5)
    architecture: int = Field(ge=1, le=5)
    nature: int = Field(ge=1, le=5)
    science: int = Field(ge=1, le=5)
    food: int = Field(ge=1, le=5)
    entertainment: int = Field(ge=1, le=5)
    shopping: int = Field(ge=1, le=5)
    views: int = Field(ge=1, le=5)
    family: int = Field(ge=1, le=5)

class ClusterRanking(BaseModel):
    cluster_id: int
    cluster_label: str
    average_match_score: float
    rank: int


class AttractionRecommendation(BaseModel):
    wikidata_id: str
    name: str
    category: str
    summary: str

    latitude: float
    longitude: float
    image_url: str | None

    themes: list[str]
    recommended_visit_time: str
    estimated_visit_mins: int
    indoor: bool
    family_friendly: bool
    price_level: str
    borough_name: str

    cluster_id: int
    cluster_label: str
    match_score: float


class RecommendationResponse(BaseModel):
    cluster_rankings: list[ClusterRanking]
    top_attractions: list[AttractionRecommendation]