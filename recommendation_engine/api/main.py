from fastapi import FastAPI
import pandas as pd
from recommendation_engine.api.models import (
    PreferenceRequest,
    RecommendationResponse,
)
from recommendation_engine.recommender import recommend


app = FastAPI(
    title="Sightseer Recommendation API",
    version="1.0.0",
)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok"}



def dataframe_to_records(dataframe: pd.DataFrame) -> list[dict]:
    cleaned_dataframe = (
        dataframe
        .astype(object)
        .where(pd.notna(dataframe), None)
    )

    return cleaned_dataframe.to_dict(
        orient="records"
    )


@app.post("/recommendations", response_model=RecommendationResponse)
def get_recommendations(request: PreferenceRequest) -> RecommendationResponse:

    # model dump() is used to convert the Pydantic model into a dictionary, which is the expected input format for the recommend function
    results = recommend(request.model_dump())

    return RecommendationResponse(
        cluster_rankings=dataframe_to_records(results["cluster_rankings"]),
        top_attractions=dataframe_to_records(results["top_attractions"]),
    )