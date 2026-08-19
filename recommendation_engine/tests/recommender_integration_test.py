from recommendation_engine.recommender import (
    INTERESTS,
    recommend,
)

# this is an integartion test, 
# so it will use the real attractions data file, 
# I just need to test it on a mocked user preference

TEST_USER = {
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


def test_recommend_runs_complete_pipeline():
    results = recommend(TEST_USER)

    assert set(results.keys()) == {
        "cluster_rankings",
        "top_attractions",
    }

    cluster_rankings = results["cluster_rankings"]
    top_attractions = results["top_attractions"]

    assert not cluster_rankings.empty
    assert not top_attractions.empty

    # Every cluster should have no more than three recommendations
    assert (
        top_attractions
        .groupby("cluster_id")
        .size()
        .le(3)
        .all()
    )

    # Clusters should be ordered from best to worst
    assert cluster_rankings[
        "average_match_score"
    ].is_monotonic_decreasing

    # Ranks should be consecutive
    assert cluster_rankings["rank"].tolist() == list(
        range(1, len(cluster_rankings) + 1)
    )

    # Match scores should be valid percentages
    assert top_attractions["match_score"].between(
        0,
        100,
    ).all()

    # Every recommended cluster should appear in the rankings
    assert set(top_attractions["cluster_id"]) == set(
        cluster_rankings["cluster_id"]
    )