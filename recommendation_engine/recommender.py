from pathlib import Path
import pandas as pd
import numpy as np

RECOMMENDATION_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = RECOMMENDATION_DIR.parent

DATA_FILE = (
    PROJECT_ROOT
    / "rag-pipeline"
    / "data"
    / "london_clustered_attractions.json"
)

INTERESTS = [
    "history",
    "art",
    "architecture",
    "nature",
    "science",
    "food",
    "entertainment",
    "shopping",
    "views",
    "family",
]

MAX_DISTANCE = np.sqrt(len(INTERESTS) * (5 - 1) ** 2)


def validate_user_preferences(preferences: dict, interests: list) -> bool:
    """
    Validate user preferences against the list of interests and make sure 1<=interest_score<=5.

    Args:
        preferences (dict): User preferences.
        interests (list): List of valid interests.

    Returns:
        bool: True if all preferences are valid, False otherwise.
    """
    if not isinstance(preferences, dict):
        return False
    if set(preferences.keys()) != set(interests):
        return False

    return all(isinstance(score, (int, float)) and not isinstance(score, bool) and 1 <= score <= 5 for score in preferences.values())

def load_attractions(attractions_file=DATA_FILE):
    df = pd.read_json(attractions_file)
    return df

def user_vector_from_preferences(user_preferences: dict) -> np.ndarray:
    """
    Convert user preferences into a vector.

    Args:
        user_preferences (dict): User preferences.

    Returns:
        np.ndarray: User preference vector.
    """
    return np.array([user_preferences.get(interest, 0) for interest in INTERESTS])

def attraction_vector_from_df(attractions_df: pd.DataFrame) -> np.ndarray:
    """
    Convert attraction DataFrame into a matrix of attraction vectors.

    Args:
        attractions_df (pd.DataFrame): DataFrame containing attraction data.

    Returns:
        np.ndarray: Matrix of attraction vectors.
    """
    attraction_vectors = np.array([
        [scores[interest] for interest in INTERESTS]
        for scores in attractions_df["interest_scores"]
    ], dtype=float)
    return attraction_vectors

def calculate_attraction_scores(attractions_df: pd.DataFrame, user_preferences: dict) -> pd.DataFrame:
    """
    Calculate the attraction scores based on user preferences.

    Args:
        attractions_df (pd.DataFrame): DataFrame containing attraction data.
        user_preferences (dict): User preferences.

    Returns:
        pd.DataFrame: DataFrame with an additional 'attraction_score' column.
    """
    # Ensure that the user preferences are valid
    if not validate_user_preferences(user_preferences, INTERESTS):
        raise ValueError("Invalid user preferences. Ensure all interests are valid and scores are between 1 and 5.")

    # Calculate the attraction score for each attraction
    user_vector = user_vector_from_preferences(user_preferences)
    attraction_vectors = attraction_vector_from_df(attractions_df)
    distances = np.linalg.norm(
        attraction_vectors - user_vector,
        axis=1,
    )

    recommendations = attractions_df.copy()
    recommendations["recommendation_distance"] = distances

    recommendations = recommendations.sort_values(
        "recommendation_distance",
        ascending=True,
    )

    recommendations["match_score"] = ((
        1 - recommendations["recommendation_distance"] / MAX_DISTANCE
    ) * 100).round(2)

    return recommendations

def top_3_attractions_per_cluster(recommendations: pd.DataFrame) -> pd.DataFrame:
    """
    Get the top 3 attractions per cluster based on match score.

    Args:
        recommendations (pd.DataFrame): DataFrame containing recommendations with match scores.

    Returns:
        pd.DataFrame: DataFrame with the top 3 attractions per cluster.
    """
    top_attractions_per_cluster = (
        recommendations
        .sort_values(["cluster_id", "match_score", "name"], ascending=[True, False, True])
        .groupby("cluster_id", group_keys=False)
        .head(3)
        .reset_index(drop=True)
    )

    return top_attractions_per_cluster

def rank_clusters(top_attractions_per_cluster: pd.DataFrame) -> pd.DataFrame:
    """
    Rank clusters based on the average match score of their top 3 attractions.

    Args:
        top_attractions_per_cluster (pd.DataFrame): DataFrame containing the top 3 attractions per cluster.
    """
    cluster_rankings = (
        top_attractions_per_cluster
        .groupby("cluster_id", group_keys=False)
        .agg(
            cluster_label=("cluster_label", "first"),
            average_match_score=("match_score", "mean"),
        )
        .sort_values(
            ["average_match_score", "cluster_id"],
            ascending=[False, True],
        )
        .reset_index()
    )

    cluster_rankings["average_match_score"] = cluster_rankings["average_match_score"].round(2)

    cluster_rankings["rank"] = range(1, len(cluster_rankings) + 1)

    return cluster_rankings

def recommend(user_preferences: dict) -> dict:
    """
    Generate recommendations based on user preferences. gives top 3 attractions per cluster and ranks the clusters based on average match score.

    Args:
        user_preferences (dict): User preferences.

    Returns:
        dict: Dictionary containing ranked clusters and their top 3 attractions for the top 3 clusters and also all the clusters.
    """
    # load the attractions data
    attractions_df = load_attractions()
    recommendations = calculate_attraction_scores(attractions_df, user_preferences)
    top_attractions = top_3_attractions_per_cluster(recommendations)
    cluster_rankings = rank_clusters(top_attractions)

    # merge the cluster rankings with the top attractions to include the rank in the top attractions DataFrame
    top_attractions = (
    top_attractions
    .merge(
        cluster_rankings[["cluster_id", "rank"]],
        on="cluster_id",
        how="left",
        validate="many_to_one",
    )
    .sort_values(
        ["rank", "match_score", "name"],
        ascending=[True, False, True],
    )
    .drop(columns="rank")
    .reset_index(drop=True)
)

    return {"cluster_rankings": cluster_rankings, "top_attractions": top_attractions}
