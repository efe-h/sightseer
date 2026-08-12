import json
import requests
from pathlib import Path


OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL = "qwen3:14b"

INPUT_FILE = Path("../data/london_attractions.json")
OUTPUT_FILE = Path("../data/london_full_attractions.json")

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
    with open(INPUT_FILE, "r", encoding="utf-8") as file:
        attractions = json.load(file)

    enriched_attractions = []

    # TESTING: only process the first 20 attractions
    for i, attraction in enumerate(attractions[:20], start=1):
        print(
            f"\nEnriching {i}/{min(20, len(attractions))}: "
            f"{attraction['name']}"
        )

        try:
            enrichment = enrich_attraction(attraction)

            attraction["summary"] = enrichment["summary"]
            attraction["themes"] = enrichment["themes"]
            attraction["interest_scores"] = enrichment["interest_scores"]
            attraction["recommended_visit_time"] = enrichment["recommended_visit_time"]
            attraction["estimated_visit_mins"] = enrichment["estimated_visit_mins"]
            attraction["indoor"] = enrichment["indoor"]
            attraction["family_friendly"] = enrichment["family_friendly"]
            attraction["price_level"] = enrichment["price_level"]

            enriched_attractions.append(attraction)

            # Save immediately
            with open(OUTPUT_FILE, "w", encoding="utf-8") as file:
                json.dump(
                    enriched_attractions,
                    file,
                    indent=2,
                    ensure_ascii=False,
                )

            print("✓ Done")

        except Exception as e:
            print(f"✗ Failed after retries: {e}")


if __name__ == "__main__":
    main()