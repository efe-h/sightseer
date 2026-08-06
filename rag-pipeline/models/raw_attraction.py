from dataclasses import dataclass

@dataclass
class RawAttraction:
    wikidata_id: str
    name: str
    category: str
    description: str
    latitude: float
    longitude: float
    # no category yet, this will be done later in the pipeline using an LLM
    image_url: str | None
    sitelinks: int | None