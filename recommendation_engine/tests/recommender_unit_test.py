import numpy as np
import pandas as pd
import pytest

from recommendation_engine.recommender import (
    INTERESTS,
    validate_user_preferences,
    user_vector_from_preferences,
    attraction_vector_from_df,
    calculate_attraction_scores,
    rank_clusters,
    top_3_attractions_per_cluster,
)

VALID_PREFERENCES = {
    interest: 3
    for interest in INTERESTS
}

INVALID_PREFERENCES_TOO_BIG = {
    **VALID_PREFERENCES,
    "history": 6,
}

INVALID_PREFERENCES_TOO_SMALL = {
    **VALID_PREFERENCES,
    "history": 0,
}

INVALID_PREFERENCES_INCOMPLETE = {
    interest: score
    for interest, score in VALID_PREFERENCES.items()
    if interest != "family"
}

INVALID_PREFERENCES_EXTRA = {
    **VALID_PREFERENCES,
    "sports": 3,
}

INVALID_PREFERENCES_WRONG_TYPE = {
    **VALID_PREFERENCES,
    "family": "three",
}

INVALID_PREFERENCES_NONE_SCORE = {
    **VALID_PREFERENCES,
    "family": None,
}

INVALID_PREFERENCES_BOOLEAN = {
    **VALID_PREFERENCES,
    "family": True,
}

sample_attractions_df = pd.DataFrame(
    {
        "name": ["A", "B", "C", "D", "E", "F", "G", "H", "I"],
        "cluster_id": [1, 1, 1, 2, 2, 2, 3, 3, 3],
        "cluster_label": ["Alpha", "Alpha", "Alpha", "Beta", "Beta", "Beta", "Gamma", "Gamma", "Gamma"],
        "interest_scores": [
            {interest: 1 for interest in INTERESTS},
            {interest: 5 for interest in INTERESTS},
            {interest: 3 for interest in INTERESTS},
            {interest: 2 for interest in INTERESTS},
            {interest: 4 for interest in INTERESTS},
            {interest: 3 for interest in INTERESTS},
            {interest: 1 for interest in INTERESTS},
            {interest: 5 for interest in INTERESTS},
            {interest: 3 for interest in INTERESTS},
        ],
    }
)

sample_attractions_vectors = np.array([
    [scores[interest] for interest in INTERESTS]
    for scores in sample_attractions_df["interest_scores"]
], dtype=float)

mocked_user_vector = np.array([VALID_PREFERENCES[i] for i in INTERESTS], dtype=float)

mocked_attractions_distances = np.linalg.norm(
    sample_attractions_vectors - mocked_user_vector,
    axis=1,
)

@pytest.mark.parametrize(
    "invalid_preferences", [
        INVALID_PREFERENCES_TOO_BIG,
        INVALID_PREFERENCES_TOO_SMALL,
        INVALID_PREFERENCES_INCOMPLETE,
        INVALID_PREFERENCES_EXTRA,
        INVALID_PREFERENCES_WRONG_TYPE,
        INVALID_PREFERENCES_NONE_SCORE,
        INVALID_PREFERENCES_BOOLEAN,
        {},
        None,
    ]
)

# --------------------------------------
# tests for validate_user_preferences
# --------------------------------------

def test_validate_user_preferences_invalid(invalid_preferences):
    assert not validate_user_preferences(invalid_preferences, INTERESTS)


def test_validate_user_preferences_valid():
    assert validate_user_preferences(VALID_PREFERENCES, INTERESTS)

# --------------------------------------
# tests for user_vector_from_preferences
# --------------------------------------

def test_user_vector_from_preferences():
    user_vector = user_vector_from_preferences(VALID_PREFERENCES)

    assert isinstance(user_vector, np.ndarray)
    assert user_vector.shape == (len(INTERESTS),)
    assert np.all(user_vector == np.array([VALID_PREFERENCES[i] for i in INTERESTS]))

# -------------------------------------
# tests for test_attraction_vector_from_df
# -------------------------------------

def test_attraction_vector_from_df():
    attraction_vectors = attraction_vector_from_df(sample_attractions_df)
    assert attraction_vectors.shape == (9, len(INTERESTS))
    assert np.all(attraction_vectors == sample_attractions_vectors)

# -------------------------------------
# tests for calculate_attraction_scores
# -------------------------------------

def test_calculate_attraction_scores_sorts_by_similarity():
    result = calculate_attraction_scores(sample_attractions_df, VALID_PREFERENCES)
    assert result["recommendation_distance"].is_monotonic_increasing 
    assert np.allclose(result["recommendation_distance"].values, np.sort(mocked_attractions_distances))

# -------------------------------------
# tests for top_3_attractions_per_cluster
# -------------------------------------

def test_top_3_attractions_per_cluster_returns_best_three():
    recommendations = pd.DataFrame(
        {
            "cluster_id": [
                1, 1, 1, 1,
                2, 2, 2, 2,
            ],
            "match_score": [
                10, 40, 30, 20,
                50, 80, 60, 70,
            ],
            "name": [
                "a1", "a2", "a3", "a4",
                "b1", "b2", "b3", "b4",
            ],
            "cluster_label": [
                "A", "A", "A", "A",
                "B", "B", "B", "B",
            ],
        }
    )

    top = top_3_attractions_per_cluster(
        recommendations
    )

    assert len(top) == 6

    assert top.groupby("cluster_id").size().to_dict() == {
        1: 3,
        2: 3,
    }

    assert set(top.loc[top["cluster_id"] == 1, "name"]) == {
        "a2",
        "a3",
        "a4",
    }

    assert set(top.loc[top["cluster_id"] == 2, "name"]) == {
        "b2",
        "b3",
        "b4",
    }

    assert top.loc[
        top["cluster_id"] == 1,
        "match_score",
    ].tolist() == [40, 30, 20]

    assert top.loc[
        top["cluster_id"] == 2,
        "match_score",
    ].tolist() == [80, 70, 60]

# -------------------------------------
# tests for rank_clusters
# -------------------------------------

def test_rank_clusters_sorts_by_average_match_score():
    top_attractions = pd.DataFrame(
        {
            "cluster_id": [1, 1, 1, 2, 2, 2],
            "cluster_label": ["A", "A", "A", "B", "B", "B"],
            "match_score": [90, 80, 70, 60, 50, 40],
        }
    )

    ranked = rank_clusters(top_attractions)

    assert list(ranked.columns) == ["cluster_id", "cluster_label", "average_match_score", "rank"]
    assert ranked["average_match_score"].is_monotonic_decreasing
    assert ranked["rank"].tolist() == [1, 2]
    assert ranked["cluster_id"].tolist() == [1, 2]
    assert ranked["average_match_score"].tolist() == [80.0, 50.0]

# -------------------------------------
# tests for invalid user preferences in calculate_attraction_scores
# -------------------------------------

def test_calculate_attraction_scores_rejects_invalid_preferences():
    attractions_df = pd.DataFrame(
        {"interest_scores": [{interest: 3 for interest in INTERESTS}]}
    )

    with pytest.raises(ValueError):
        calculate_attraction_scores(attractions_df, INVALID_PREFERENCES_TOO_BIG)
