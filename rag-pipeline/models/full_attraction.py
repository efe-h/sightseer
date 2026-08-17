from dataclasses import dataclass
from models.interest_scores import InterestScores
from typing import Literal

@dataclass
class FullAttraction:
    # the raw attraction fields
    wikidata_id: str
    name: str
    wikidata_category: str
    description: str
    latitude: float
    longitude: float
    # no category yet, this will be done later in the pipeline using an LLM
    image_url: str | None
    sitelinks: int | None

    # the additional fields

    summary: str 
    themes: list[str]
    interest_scores: InterestScores
    recommended_visit_time: Literal["morning", "afternoon", "evening", "anytime"]
    estimated_visit_mins: int
    indoor: bool
    family_friendly: bool
    price_level: Literal["free", "£", "££", "£££"]

    # clustering

    cluster_id: int | None
