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
    "required": ["description", "scores"],
    "properties": {
        "description": {
            "type": "string"
        },
        "scores": {
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
        }
    }
}

def create_prompt(attraction):
    return f"""
You are enriching a dataset of tourist attractions in London.

Using the provided attraction information and your existing knowledge of the
attraction, produce:

1. A concise, factual, tourist-friendly description of approximately 60-80 words.

2. A score from 1 to 5 for EACH of these interests:
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

The score represents how strongly the attraction appeals to a visitor interested
in that category.

Scoring:
1 = Very little relevance
2 = Low relevance
3 = Moderate relevance
4 = High relevance
5 = Very high relevance

You may use your existing knowledge of the attraction in addition to the
provided information.

Score the attraction itself, not nearby attractions, restaurants, shops or
other businesses.

Be consistent in how you score different attractions.

Return ONLY valid JSON in exactly this format:

{{
    "description": "...",
    "scores": {{
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
    }}
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

            # Validate description
            if not isinstance(enrichment.get("description"), str):
                raise ValueError("Missing or invalid description")

            # Validate scores
            scores = enrichment.get("scores")

            if not isinstance(scores, dict):
                raise ValueError("Missing or invalid scores")

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

    # TESTING: only process the first 3 attractions
    for i, attraction in enumerate(attractions[:3], start=1):
        print(
            f"\nEnriching {i}/{min(3, len(attractions))}: "
            f"{attraction['name']}"
        )

        try:
            enrichment = enrich_attraction(attraction)

            attraction["llm_description"] = enrichment["description"]
            attraction["scores"] = enrichment["scores"]

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