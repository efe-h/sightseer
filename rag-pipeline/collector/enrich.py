import json
import requests
from pathlib import Path


OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL = "qwen3:14b"

INPUT_FILE = Path("../data/london_attractions.json")
OUTPUT_FILE = Path("../data/london_full_attractions.json")
FAILED_FILE = Path("../data/london_failed_attractions.json")

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

RESPONSE_SCHEMA = {
    "type": "object",
    "required": [
        "summary",
        "themes",
        "interest_scores",
        "recommended_visit_time",
        "estimated_visit_mins",
        "indoor",
        "family_friendly",
        "price_level",
    ],
    "properties": {
        "summary": {
            "type": "string"
        },
        "themes": {
            "type": "array",
            "items": {
                "type": "string"
            }
        },
        "interest_scores": {
            "type": "object",
            "required": INTERESTS,
            "properties": {
                interest: {
                    "type": "integer",
                    "minimum": 1,
                    "maximum": 5
                }
                for interest in INTERESTS
            }
        },
        "recommended_visit_time": {
            "type": "string",
            "enum": [
                "morning",
                "afternoon",
                "evening",
                "anytime"
            ]
        },
        "estimated_visit_mins": {
            "type": "integer",
            "minimum": 15
        },
        "indoor": {
            "type": "boolean"
        },
        "family_friendly": {
            "type": "boolean"
        },
        "price_level": {
            "type": "string",
            "enum": [
                "free",
                "£",
                "££",
                "£££"
            ]
        }
    }
}

def create_prompt(attraction):
    return f"""
You are enriching a dataset of tourist attractions in London.

Using the provided attraction information and your existing knowledge of the
attraction, produce the following information.

1. SUMMARY

Write a concise, factual, tourist-friendly summary of approximately 60-80 words.

2. THEMES

Identify 2-5 themes that describe what the attraction is mainly about.

Examples:
- history
- ancient history
- art
- science
- architecture
- nature
- culture
- royalty
- religion
- military history
- literature
- technology

Use short, meaningful labels.

3. INTEREST SCORES

Give a score from 1 to 5 for EACH of these interests:

- history
- art
- architecture
- nature
- science
- food
- entertainment
- shopping
- views
- family

The score represents how strongly the attraction itself appeals to a visitor
interested in that category.

Scoring:
1 = Very little relevance
2 = Low relevance
3 = Moderate relevance
4 = High relevance
5 = Very high relevance

Be consistent in how you score different attractions.

4. RECOMMENDED VISIT TIME

Choose the most suitable time of day for visiting that is the most enjoyable and suitable for a visitor:

- morning
- afternoon
- evening
- anytime

Use "anytime" when there is no strong reason to prefer a particular time.

5. ESTIMATED VISIT DURATION

Estimate how many minutes a typical tourist would reasonably spend visiting
the attraction itself.

Return an integer number of minutes.

6. INDOOR

Return true if the attraction is primarily an indoor attraction.

Return false if it is primarily outdoors.

7. FAMILY FRIENDLY

Return true if the attraction is generally suitable for families with children.

8. PRICE LEVEL

Estimate the typical admission cost for the attraction itself.

Use exactly one of:

- "free" = normally no admission charge
- "£" = inexpensive
- "££" = moderately expensive
- "£££" = expensive

Do not consider the cost of nearby restaurants, shops, transport or other
businesses.

Use "free" when the attraction normally has free admission, even if optional
exhibitions or experiences may require payment.

IMPORTANT:
- Score the attraction itself, not nearby attractions.
- Do not invent unrelated information.
- Be consistent between attractions.
- Return ALL required fields.
- Never return an empty JSON object.
- Return ONLY valid JSON.
- Do not include markdown or explanatory text.

Return exactly this structure:

{{
    "summary": "...",
    "themes": [
        "...",
        "..."
    ],
    "interest_scores": {{
        "history": 1,
        "art": 1,
        "architecture": 1,
        "nature": 1,
        "science": 1,
        "food": 1,
        "entertainment": 1,
        "shopping": 1,
        "views": 1,
        "family": 1
    }},
    "recommended_visit_time": "anytime",
    "estimated_visit_mins": 60,
    "indoor": true,
    "family_friendly": true,
    "price_level": "free"
}}

Attraction information:

{json.dumps(attraction, indent=2)}
"""

def load_json_list(file_path):
    """Load a JSON list, returning an empty list if the file does not exist."""
    if not file_path.exists():
        return []

    with open(file_path, "r", encoding="utf-8") as file:
        data = json.load(file)

    if not isinstance(data, list):
        raise ValueError(f"{file_path} must contain a JSON list")

    return data


def save_json_list(file_path, data):
    """Save safely using a temporary file before replacing the output."""
    temporary_file = file_path.with_suffix(file_path.suffix + ".tmp")

    with open(temporary_file, "w", encoding="utf-8") as file:
        json.dump(
            data,
            file,
            indent=2,
            ensure_ascii=False,
        )

    temporary_file.replace(file_path)

def enrich_attraction(attraction, max_retries=3):
    prompt = create_prompt(attraction)

    for attempt in range(1, max_retries + 1):
        try:
            response = requests.post(
                OLLAMA_URL,
                json={
                    "model": MODEL,
                    "prompt": prompt,
                    "stream": False,
                    "format": RESPONSE_SCHEMA,
                    "options": {
                        "temperature": 0
                    },
                },
                timeout=300,
            )

            response.raise_for_status()

            result = response.json()
            raw_response = result.get("response", "").strip()

            print("\nRAW MODEL RESPONSE:")
            print(raw_response)

            if not raw_response:
                raise ValueError("Ollama returned an empty response")

            enrichment = json.loads(raw_response)

            # Validate summary
            summary = enrichment.get("summary")

            if not isinstance(summary, str) or not summary.strip():
                raise ValueError("Missing or invalid summary")


            # Validate themes
            themes = enrichment.get("themes")

            if not isinstance(themes, list) or not themes:
                raise ValueError("Missing or invalid themes")

            if not all(isinstance(theme, str) and theme.strip() for theme in themes):
                raise ValueError("Invalid theme")


            # Validate interest scores
            scores = enrichment.get("interest_scores")

            if not isinstance(scores, dict):
                raise ValueError("Missing or invalid interest_scores")

            for interest in INTERESTS:
                if interest not in scores:
                    raise ValueError(
                        f"Missing score for {interest}"
                    )

                score = scores[interest]

                if not isinstance(score, int) or not 1 <= score <= 5:
                    raise ValueError(
                        f"Invalid score for {interest}: {score}"
                    )


            # Validate visit time
            valid_visit_times = {
                "morning",
                "afternoon",
                "evening",
                "anytime",
            }

            if enrichment.get("recommended_visit_time") not in valid_visit_times:
                raise ValueError("Invalid recommended_visit_time")


            # Validate visit duration
            visit_mins = enrichment.get("estimated_visit_mins")

            if not isinstance(visit_mins, int) or visit_mins < 15:
                raise ValueError("Invalid estimated_visit_mins")


            # Validate booleans
            if not isinstance(enrichment.get("indoor"), bool):
                raise ValueError("Invalid indoor")

            if not isinstance(enrichment.get("family_friendly"), bool):
                raise ValueError("Invalid family_friendly")


            # Validate price
            if enrichment.get("price_level") not in {
                "free",
                "£",
                "££",
                "£££",
            }:
                raise ValueError("Invalid price_level")

            return enrichment

        except Exception as e:
            print(
                f"Attempt {attempt}/{max_retries} failed: {e}"
            )

            if attempt < max_retries:
                print("Retrying...")
            else:
                raise


def main():
    attractions = load_json_list(INPUT_FILE)

    # Resume from results saved by an earlier run
    enriched_attractions = load_json_list(OUTPUT_FILE)
    failed_attractions = load_json_list(FAILED_FILE)

    processed_ids = {
        attraction["wikidata_id"]
        for attraction in enriched_attractions
        if attraction.get("wikidata_id")
    }

    failed_ids = {
        attraction["wikidata_id"]
        for attraction in failed_attractions
        if attraction.get("wikidata_id")
    }

    print(
        f"Loaded {len(enriched_attractions)} previously enriched attractions"
    )

    # TESTING FINISHED: running on all attractions
    attractions_to_process = attractions

    for i, attraction in enumerate(attractions_to_process, start=1):
        attraction_id = attraction.get("wikidata_id")
        attraction_name = attraction.get("name", "Unknown attraction")

        print(
            f"\nEnriching {i}/{len(attractions_to_process)}: "
            f"{attraction_name}"
        )

        if not attraction_id:
            print("✗ Skipped: missing wikidata_id")
            continue

        if attraction_id in processed_ids:
            print("↷ Already enriched — skipping")
            continue

        try:
            enrichment = enrich_attraction(attraction)

            # Make a new dictionary rather than changing the raw attraction
            enriched_attraction = {
                **attraction,
                **enrichment,
            }

            enriched_attractions.append(enriched_attraction)
            processed_ids.add(attraction_id)
            # Remove the attraction from the failure list if it previously failed
            if attraction_id in failed_ids:
                failed_attractions = [
                    failed
                    for failed in failed_attractions
                    if failed.get("wikidata_id") != attraction_id
                ]

                failed_ids.remove(attraction_id)
                save_json_list(
                    FAILED_FILE,
                    failed_attractions,
                )

            save_json_list(
                OUTPUT_FILE,
                enriched_attractions,
            )

            print("✓ Done")

        except Exception as e:
            print(f"✗ Failed after retries: {e}")

            if attraction_id not in failed_ids:
                failed_attractions.append({
                    "wikidata_id": attraction_id,
                    "name": attraction_name,
                    "error": str(e),
                })

                failed_ids.add(attraction_id)
                save_json_list(
                    FAILED_FILE,
                    failed_attractions,
                )


if __name__ == "__main__":
    main()