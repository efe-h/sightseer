from collector.wikidata_collector import WikidataCollector


def main():

    collector = WikidataCollector()


    attractions = collector.get_london_attractions()


    print("\nCollection complete!")
    print(
        f"Final attractions: {len(attractions)}"
    )


if __name__ == "__main__":
    main()