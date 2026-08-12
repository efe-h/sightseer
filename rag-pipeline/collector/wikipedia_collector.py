# # input the raw attarctions - london_attractions.json
# # wikipedia article texts - london_wikipedia_articles.json
# # next step is to create and use the LLM_enricher.py to enrich the raw attractions with summaries, themes, interest scores, and other fields. This will create a new file called london_full_attractions.json which will contain the enriched attractions.
# # then clustering EDA (K-means) to geographically cluster the attractions and add a cluster_id field to each attraction. This will create a new file called london_full_attractions_with_clusters.json which will contain the enriched attractions with cluster ids.


# # will not be using anymore
# # will use the london_attactions.json file as input to the LLM_enricher.py to create descriptions for each attraction
# # these descriptions will then be used by the LLM to create the interest scores as well. 

# import json
# import time
# from dataclasses import asdict
# from pathlib import Path




# import requests

# from models.raw_attraction import RawAttraction
# from models.wikipedia_attraction import WikipediaAttraction


# class WikipediaCollector:

#     def __init__(self):

#         self.input_path = Path(
#             "data/london_attractions.json"
#         )

#         self.output_path = Path(
#             "data/wikipedia_attractions_london.json"
#         )

#         self.wikidata_url = (
#             "https://www.wikidata.org/wiki/Special:EntityData"
#         )

#         self.wikipedia_summary_url = (
#             "https://en.wikipedia.org/api/rest_v1/page/summary"
#         )


#     def collect(self):

#         attractions = self.load_raw_attractions()

#         print(
#             f"Loaded {len(attractions)} attractions"
#         )

#         wikipedia_attractions = []

#         for attraction in attractions:

#             print(
#                 f"Collecting {attraction.name}..."
#             )

#             time.sleep(0.2)

#             try:

#                 wikipedia = self.get_wikipedia(
#                     attraction
#                 )

#                 wikipedia_attractions.append(
#                     wikipedia
#                 )

#             except Exception as e:

#                 print(
#                     f"Failed: {attraction.name} ({e})"
#                 )

#                 wikipedia_attractions.append(

#                     WikipediaAttraction(

#                         wikidata_id=
#                         attraction.wikidata_id,

#                         name=
#                         attraction.name,

#                         wikidata_category=
#                         attraction.category,

#                         description=
#                         attraction.description,

#                         latitude=
#                         attraction.latitude,

#                         longitude=
#                         attraction.longitude,

#                         image_url=
#                         attraction.image_url,

#                         sitelinks=
#                         attraction.sitelinks,

#                         wikipedia_title=None,

#                         wikipedia_summary=None

#                     )

#                 )

#         self.save(
#             wikipedia_attractions
#         )

#         return wikipedia_attractions


#     def get_wikipedia(
#         self,
#         attraction: RawAttraction
#     ) -> WikipediaAttraction:

#         entity_url = (

#             f"{self.wikidata_url}/"
#             f"{attraction.wikidata_id}.json"

#         )

#         response = requests.get(

#             entity_url,

#             headers={
#                 "User-Agent": "Sightseer/1.0"
#             },

#             timeout=30

#         )

#         response.raise_for_status()

#         entity = response.json()["entities"][
#             attraction.wikidata_id
#         ]

#         title = None

#         if (
#             "sitelinks" in entity
#             and "enwiki" in entity["sitelinks"]
#         ):

#             title = entity["sitelinks"][
#                 "enwiki"
#             ]["title"]

#         summary = None

#         if title:

#             response = requests.get(

#                 f"{self.wikipedia_summary_url}/{title}",

#                 headers={
#                     "User-Agent": "Sightseer/1.0"
#                 },

#                 timeout=30

#             )

#             if response.status_code == 200:

#                 summary = response.json().get(
#                     "extract"
#                 )

#         return WikipediaAttraction(

#             wikidata_id=
#             attraction.wikidata_id,

#             name=
#             attraction.name,

#             wikidata_category=
#             attraction.category,

#             description=
#             attraction.description,

#             latitude=
#             attraction.latitude,

#             longitude=
#             attraction.longitude,

#             image_url=
#             attraction.image_url,

#             sitelinks=
#             attraction.sitelinks,

#             wikipedia_title=
#             title,

#             wikipedia_summary=
#             summary

#         )


#     def load_raw_attractions(self):

#         with open(

#             self.input_path,

#             "r",

#             encoding="utf-8"

#         ) as file:

#             data = json.load(
#                 file
#             )

#         return [

#             RawAttraction(**item)

#             for item in data

#         ]


#     def save(
#         self,
#         attractions
#     ):

#         self.output_path.parent.mkdir(
#             exist_ok=True
#         )

#         with open(

#             self.output_path,

#             "w",

#             encoding="utf-8"

#         ) as file:

#             json.dump(

#                 [

#                     asdict(
#                         attraction
#                     )

#                     for attraction in attractions

#                 ],

#                 file,

#                 indent=4,

#                 ensure_ascii=False

#             )

#         print(

#             f"Saved {len(attractions)} attractions "
#             f"to {self.output_path}"

#         )