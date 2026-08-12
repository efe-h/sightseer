from dataclasses import dataclass

@dataclass
class WikipediaAttraction:
    wikidata_id: str
    name: str
    wikidata_category: str
    description: str
    latitude: float
    longitude: float
    # no category yet, this will be done later in the pipeline using an LLM
    image_url: str | None
    sitelinks: int | None

    wikipedia_title: str | None
    wikipedia_summary: str | None