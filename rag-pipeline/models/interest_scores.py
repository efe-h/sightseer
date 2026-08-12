from dataclasses import dataclass

@dataclass
class InterestScores:
    history: int
    art: int
    architecture: int
    nature: int
    science: int
    food: int
    entertainment: int
    shopping: int
    views: int
    family: int