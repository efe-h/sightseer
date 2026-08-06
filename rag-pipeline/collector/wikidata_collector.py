import json
import time
from pathlib import Path

import requests

from models.raw_attraction import RawAttraction


class WikidataCollector:

    BAD_WORDS = {
        "former",
        "closed",
        "defunct",
        "demolished",
        "abandoned",
        "proposed",
        "archive",
        "archives",
        "headquarters",
        "research centre",
        "research center",
    }


    def __init__(self):

        self.output_path = Path(
            "data/london_attractions.json"
        )

        self.endpoint = (
            "https://query.wikidata.org/sparql"
        )


    def get_london_attractions(self):

        categories = {
            "museum": 100,
            "art_museum": 100,
            "castle": 50,
            "palace": 50,
            "archaeological_site": 50,
            "monument": 100,
            "historical_landmark": 100,
            "garden": 100,
            "park": 100,
            "market": 100,
        }


        all_attractions = []


        for category, limit in categories.items():

            print(f"Collecting {category}...")

            # avoid hammering Wikidata
            time.sleep(3)


            try:

                attractions = self.get_attractions(
                    category,
                    limit
                )


                print(
                    f"Found {len(attractions)} {category}"
                )


                all_attractions.extend(
                    attractions
                )


            except Exception as e:

                print(
                    f"Failed collecting {category}: {e}"
                )



        print(
            f"\nRaw attractions: {len(all_attractions)}"
        )



        all_attractions = self.deduplicate(
            all_attractions
        )


        print(
            f"After deduplication: {len(all_attractions)}"
        )



        # remove invalid entries only
        all_attractions = [
            attraction
            for attraction in all_attractions
            if self.is_valid(attraction)
        ]


        print(
            f"After quality filter: {len(all_attractions)}"
        )


        self.save_attractions(
            all_attractions
        )


        return all_attractions



    def get_attractions(self, category, limit):

        query = f"""

        SELECT DISTINCT
            ?item
            ?itemLabel
            ?coord
            ?image
            ?description
            ?sitelinks

        WHERE {{

            ?item wdt:P31 wd:{self.get_qid(category)};
                wdt:P625 ?coord.

            # Exclude closed / demolished attractions
            FILTER NOT EXISTS {{ ?item wdt:P576 ?endDate. }}

            # Exclude dissolved organisations
            FILTER NOT EXISTS {{ ?item wdt:P3999 ?dissolvedDate. }}

            OPTIONAL {{
                ?item wdt:P18 ?image.
            }}

            OPTIONAL {{
                ?item schema:description ?description.
                FILTER(LANG(?description) = "en")
            }}

            OPTIONAL {{
                ?item wikibase:sitelinks ?sitelinks.
            }}

            FILTER(

                geof:longitude(?coord) > -0.55 &&
                geof:longitude(?coord) < 0.30 &&
                geof:latitude(?coord) > 51.28 &&
                geof:latitude(?coord) < 51.70

            )

            SERVICE wikibase:label {{
                bd:serviceParam wikibase:language "en".
            }}

        }}

        ORDER BY DESC(?sitelinks)

        LIMIT {limit}

        """


        response = requests.get(

            self.endpoint,

            params={
                "query": query,
                "format": "json"
            },

            headers={
                "User-Agent": "Sightseer/1.0"
            },

            timeout=30
        )


        response.raise_for_status()


        results = response.json()


        attractions = []


        for result in results["results"]["bindings"]:


            coord = (
                result["coord"]["value"]
                .replace("Point(", "")
                .replace(")", "")
            )


            longitude, latitude = map(
                float,
                coord.split()
            )


            attractions.append(

                RawAttraction(

                    wikidata_id=
                    result["item"]["value"]
                    .split("/")[-1],


                    name=
                    result["itemLabel"]["value"],


                    category=
                    category,


                    description=
                    result.get(
                        "description",
                        {}
                    )
                    .get(
                        "value",
                        ""
                    ),


                    latitude=
                    latitude,


                    longitude=
                    longitude,


                    image_url=
                    result.get(
                        "image",
                        {}
                    )
                    .get(
                        "value",
                        None
                    ),


                    sitelinks=int(
                        result.get(
                            "sitelinks",
                            {}
                        ).get(
                            "value",
                            0
                        )
                    )

                )

            )


        return attractions



    def get_qid(self, category):

        mapping = {

            "museum": "Q33506",

            "art_museum": "Q207694",

            "castle": "Q23413",

            "palace": "Q16560",

            "archaeological_site": "Q839954",

            "monument": "Q4989906",

            "historical_landmark": "Q14419701",

            "garden": "Q1107656",

            "park": "Q22698",

            "market": "Q3300198"

        }


        return mapping[category]



    def deduplicate(self, attractions):

        seen = {}


        for attraction in attractions:

            if attraction.wikidata_id not in seen:

                seen[
                    attraction.wikidata_id
                ] = attraction


        return list(
            seen.values()
        )

    def is_valid(self, attraction: RawAttraction) -> bool:

        if not attraction.name:
            return False

        if attraction.latitude is None or attraction.longitude is None:
            return False

        description = attraction.description.lower()

        if any(word in description for word in self.BAD_WORDS):
            return False

        return True

    def save_attractions(self, attractions):

        self.output_path.parent.mkdir(
            exist_ok=True
        )


        data = [

            {

                "wikidata_id":
                    attraction.wikidata_id,

                "name":
                    attraction.name,

                "category":
                    attraction.category,

                "description":
                    attraction.description,

                "latitude":
                    attraction.latitude,

                "longitude":
                    attraction.longitude,

                "image_url":
                    attraction.image_url,

                "sitelinks":
                    attraction.sitelinks

            }

            for attraction in attractions

        ]


        with open(

            self.output_path,

            "w",

            encoding="utf-8"

        ) as file:


            json.dump(

                data,

                file,

                indent=4,

                ensure_ascii=False

            )


        print(
            f"Saved {len(data)} attractions to {self.output_path}"
        )