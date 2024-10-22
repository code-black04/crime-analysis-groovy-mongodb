import os
import fire
import pandas as pd
import json
from tqdm import tqdm
from pathlib import Path
from datetime import datetime

# CONFIG/Constants
INPUT_FOLDER = "./police_data"
OUTPUT_FILE = "./police_crime_data.json"
DATE_START = "2024-03"
DATE_END = "2024-08"


class PoliceDataConverter:
    def _street_from_filename(self, filename):
        return "-".join(filename.stem.split("-")[2:])

    def _is_in_date_range(self, folder_date, date_start, date_end):
        if date_start and folder_date < date_start:
            return False
        if date_end and folder_date > date_end:
            return False
        return True

    def to_json(
        self,
        input_folder: str = INPUT_FOLDER,
        output_file: str = OUTPUT_FILE,
        date_start: str = DATE_START,
        date_end: str = DATE_END,
    ):
        input_folder = Path(input_folder)
        result = {}

        date_start = datetime.strptime(date_start, "%Y-%m")
        date_end = datetime.strptime(date_end, "%Y-%m")

        # Get csv files
        for folder in tqdm(sorted(input_folder.iterdir()), desc="Processing folders"):
            if folder.is_dir():
                folder_date = datetime.strptime(folder.name, "%Y-%m")

                if self._is_in_date_range(folder_date, date_start, date_end):
                    csv_files = list(folder.glob("*.csv"))

                    for csv_file in csv_files:
                        self._process_csv(csv_file, result)

        # Save data
        with open(output_file, "w") as f_out:
            json.dump(result, f_out, indent=4)

    def _process_csv(self, csv_file, result):
        df = pd.read_csv(csv_file)

        street = self._street_from_filename(csv_file)
        if street not in result:
            result[street] = []

        for i, row in df.iterrows():
            data = {
                key: value
                for key, value in {
                    "crime_id": row.get("Crime ID"),
                    "date": row.get("Month"),
                    "reported_by": row.get("Reported by"),
                    "falls_within": row.get("Falls within"),
                    "location": {
                        "address": row.get("Location"),
                        "geo": {
                            "lat": row.get("Latitude"),
                            "lng": row.get("Longitude"),
                        },
                    },
                    "LSOA": {
                        "code": row.get("LSOA code"),
                        "name": row.get("LSOA name"),
                    },
                    "crime_type": row.get("Crime type"),
                    "last_outcome_category": row.get("Last outcome category"),
                }.items()
                if pd.notna(value)
            }
            # Nan data clean up for nested values
            if "location" in data:
                geo_data = data["location"].get("geo")
                if geo_data and not any(pd.notna(v) for v in geo_data.values()):
                    del data["location"]["geo"]

                if not any(pd.notna(v) for v in data["location"].values()):
                    del data["location"]

            if "LSOA" in data and not any(pd.notna(v) for v in data["LSOA"].values()):
                del data["LSOA"]

            result[street].append(data)


if __name__ == "__main__":
    fire.Fire(PoliceDataConverter)
