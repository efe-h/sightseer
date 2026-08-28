import math

import pandas as pd
import pytest
from fastapi.testclient import TestClient

from recommendation_engine.api.main import (
    app,
    dataframe_to_records,
)


client = TestClient(app)


VALID_PREFERENCES = {
    "history": 5,
    "art": 3,
    "architecture": 4,
    "nature": 2,
    "science": 2,
    "food": 1,
    "entertainment": 2,
    "shopping": 1,
    "views": 3,
    "family": 1,
}


def test_health_returns_ok():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {
        "status": "ok",
    }


def test_recommendations_returns_valid_response():
    response = client.post(
        "/recommendations",
        json=VALID_PREFERENCES,
    )

    assert response.status_code == 200

    body = response.json()

    assert set(body.keys()) == {
        "cluster_rankings",
        "top_attractions",
    }

    assert body["cluster_rankings"]
    assert body["top_attractions"]

    first_cluster = body["cluster_rankings"][0]

    assert set(first_cluster.keys()) == {
        "cluster_id",
        "cluster_label",
        "average_match_score",
        "rank",
    }

    assert first_cluster["rank"] == 1

    first_attraction = body["top_attractions"][0]

    expected_attraction_fields = {
        "wikidata_id",
        "name",
        "category",
        "summary",
        "latitude",
        "longitude",
        "image_url",
        "themes",
        "recommended_visit_time",
        "estimated_visit_mins",
        "indoor",
        "family_friendly",
        "price_level",
        "borough_name",
        "cluster_id",
        "cluster_label",
        "match_score",
    }

    assert set(first_attraction.keys()) == (
        expected_attraction_fields
    )


def test_recommendations_rejects_missing_preference():
    invalid_preferences = {
        key: value
        for key, value in VALID_PREFERENCES.items()
        if key != "family"
    }

    response = client.post(
        "/recommendations",
        json=invalid_preferences,
    )

    assert response.status_code == 422

    errors = response.json()["detail"]

    assert any(
        error["loc"] == ["body", "family"]
        for error in errors
    )


@pytest.mark.parametrize("invalid_score", [0, 6])
def test_recommendations_rejects_score_outside_range(
    invalid_score,
):
    invalid_preferences = {
        **VALID_PREFERENCES,
        "history": invalid_score,
    }

    response = client.post(
        "/recommendations",
        json=invalid_preferences,
    )

    assert response.status_code == 422


def test_dataframe_to_records_converts_nan_to_none():
    dataframe = pd.DataFrame(
        {
            "name": ["Attraction"],
            "image_url": [math.nan],
        }
    )

    records = dataframe_to_records(dataframe)

    assert records == [
        {
            "name": "Attraction",
            "image_url": None,
        }
    ]