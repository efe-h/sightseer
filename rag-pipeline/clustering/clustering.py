import json
from pathlib import Path
import geopandas as gpd
import pandas as pd
from sklearn.cluster import KMeans


# Build paths relative to this script, not the terminal's current directory
CLUSTERING_DIR = Path(__file__).resolve().parent
PIPELINE_DIR = CLUSTERING_DIR.parent

DATA_FILE = PIPELINE_DIR / "data" / "london_full_attractions.json"
BOROUGHS_FILE = CLUSTERING_DIR / "data" / "London_Boroughs.gpkg"
OUTPUT_FILE = PIPELINE_DIR / "data" / "london_clustered_attractions.json"

NUMBER_OF_CLUSTERS = 28
RANDOM_STATE = 42


# NLP-assisted labels selected using processed summaries,
# LLM-generated themes, TF-IDF terms and geographical inspection.
CLUSTER_LABELS = {
    0: "South London — Art, Nature and Science",
    1: "Westminster — Royalty, Art and Heritage",
    2: "Kew and West London — Transport, Music and Heritage",
    3: "Waltham Forest — Industrial and Local History",
    4: "Croydon — Mental Health Heritage and Parks",
    5: "Barnet — Local Heritage, Literature and Culture",
    6: "Havering — Ancient Roads and Archaeology",
    7: "Sutton — Ancient Landscapes and Archaeology",
    8: "East London — Curiosities, Craft and Social History",
    9: "Merton — Sport, Cricket and Local History",
    10: "Hounslow — Steam, Science and Archaeology",
    11: "Hackney — Art, Sport and Cemetery Heritage",
    12: "Bromley — Roman Archaeology and Mental Health History",
    13: "Harrow — Ancient Earthworks and Military History",
    14: "Barking and Dagenham — Women’s History and Family Parks",
    15: "Woolwich and Plumstead — Ancient Burials and Local Heritage",
    16: "Richmond — Music, Art and Landscapes",
    17: "Enfield — Historic Estates and Military Heritage",
    18: "Camden and Marylebone — Literature, Art and Culture",
    19: "Southwark — Contemporary Art, Fashion and Printing",
    20: "Greenwich — Astronomy, Maritime and Ancient History",
    21: "Kensington — Science, Royalty and Art",
    22: "City, Shoreditch and Bankside — History, Identity and Culture",
    23: "Hampton Court and Kingston — Royal History and Archaeology",
    24: "Haringey — Religion, Family and Green Spaces",
    25: "Wandsworth — African Culture, Food and Local Heritage",
    26: "Hampstead and Highgate — Cemetery Heritage and Landscapes",
    27: "Bromley — Roman Archaeology and Historic Estates",
}


def load_attractions():
    with open(DATA_FILE, "r", encoding="utf-8") as file:
        attractions = json.load(file)

    if not isinstance(attractions, list):
        raise ValueError("Attraction data must contain a JSON list")
    
    return pd.DataFrame(attractions)

def prepare_coordinates(df):
    # add the x and y cordinates and also remove duplicates
    # remove duplicates based on name, keeping the first occurrence
    df = df.drop_duplicates(subset="name", keep="first")
    attractions_gdf = gpd.GeoDataFrame(
        df.copy(),
        geometry=gpd.points_from_xy(
            df["longitude"],
            df["latitude"],
        ),
        crs="EPSG:4326",
    )

    # Convert latitude/longitude into British National Grid coordinates
    attractions_gdf = attractions_gdf.to_crs("EPSG:27700")

    attractions_gdf["x"] = attractions_gdf.geometry.x
    attractions_gdf["y"] = attractions_gdf.geometry.y
    return attractions_gdf

def load_london_boroughs():
    london_boroughs = gpd.read_file(BOROUGHS_FILE)
    return london_boroughs.to_crs("EPSG:27700")

def filter_to_greater_london(london_boroughs, attractions_gdf):
    greater_london_boundary = london_boroughs.geometry.union_all()
    inside_london = attractions_gdf.geometry.apply(
        greater_london_boundary.covers
    )

    london_gdf = attractions_gdf.loc[inside_london].copy().reset_index(drop=True)
    return london_gdf

def attach_boroughs(attractions_gdf, london_boroughs):
    attractions_gdf = gpd.sjoin(
        attractions_gdf,
        london_boroughs[["name", "geometry"]],
        how="left",
        predicate="within"
    )
    # fix the name left and name right columns after the spatial join
    attractions_gdf = attractions_gdf.rename(columns={
        "name_left": "name",
        "name_right": "borough_name"
    })
    # also get rid of the index_right column that was created during the spatial join
    attractions_gdf = attractions_gdf.drop(columns=["index_right"])

    if attractions_gdf["borough_name"].isna().any():
        missing_count = (
            attractions_gdf["borough_name"]
            .isna()
            .sum()
        )

        raise ValueError(
            f"{missing_count} attractions have no borough"
        )
    
    return attractions_gdf

def assign_clusters(attractions_gdf):
    clusters_model = KMeans (
        n_clusters=NUMBER_OF_CLUSTERS,
        init="k-means++",
        random_state=RANDOM_STATE,
        n_init=20,
    )

    attractions_gdf["cluster_id"] = clusters_model.fit_predict(
        attractions_gdf[["x", "y"]]
    )
    return attractions_gdf

def add_cluster_labels(attractions_clustered_gdf):
    attractions_clustered_gdf["cluster_label"] = (
        attractions_clustered_gdf["cluster_id"]
        .map(CLUSTER_LABELS)
    )

    missing_labels = (
        attractions_clustered_gdf["cluster_label"]
        .isna()
        .sum()
    )

    if missing_labels:
        raise ValueError(
            f"{missing_labels} attractions have no cluster label"
        )

    return attractions_clustered_gdf

def save_clustered_attractions(attractions_clustered_gdf):
    columns_to_remove = [
        "geometry",
        "x",
        "y",
        "nlp_text",
    ]

    output_df = attractions_clustered_gdf.drop(
        columns=columns_to_remove,
        errors="ignore",
    )

    temporary_file = OUTPUT_FILE.with_suffix(
        OUTPUT_FILE.suffix + ".tmp"
    )

    output_df.to_json(
        temporary_file,
        orient="records",
        indent=2,
        force_ascii=False,
    )

    temporary_file.replace(OUTPUT_FILE)

def main():
    # load the attractions data
    attractions_df = load_attractions()
    print(f"Loaded attractions: {len(attractions_df)}")

    # prepare the coordinates and remove duplicates
    attractions_gdf = prepare_coordinates(attractions_df)
    print(
        "After duplicate removal:",
        len(attractions_gdf),
    )

    # load the London boroughs data
    london_boroughs = load_london_boroughs()
    print(f"Loaded London boroughs: {len(london_boroughs)}")

    # filter the attractions to only those within Greater London
    london_gdf = filter_to_greater_london(london_boroughs, attractions_gdf)
    print(
        "Inside Greater London:",
        len(london_gdf),
    )

    # attach the borough names to the attractions
    london_gdf = attach_boroughs(london_gdf, london_boroughs)

    # assign clusters to the attractions
    attractions_clustered_gdf = assign_clusters(london_gdf)
    print(
        f"Assigned {attractions_clustered_gdf['cluster_id'].nunique()} clusters"
    )

    # add cluster labels to the attractions
    attractions_clustered_gdf = add_cluster_labels(attractions_clustered_gdf)

    # save the clustered attractions to a JSON file
    save_clustered_attractions(attractions_clustered_gdf)
    print(f"Saved clustered attractions to {OUTPUT_FILE}")

if __name__ == "__main__":
    main()