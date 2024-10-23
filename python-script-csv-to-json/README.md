# Data Processing

## Prerequisites

1. Python 3.8+
2. (Optional) Virtual enviornment

### Install  Dependencies

Install the required dependecies from the requirements.txt

```
pip install -r requirements.txt
```

## Usage

Run the script using:

```
python converter.py to_json
```

Optional parameters

* `--input_folder`: The folder containing the CSV files (default: `"./police_data"`).
* `--output_file`: The path for the generated JSON file (default: `"./police_crime_data.json"`).
* `--date_start`: The start of the date range to filter the CSV files (in `YYYY-MM` format).
* `--date_end`: The end of the date range to filter the CSV files (in `YYYY-MM` format).

Example:

```
python converter.py to_json --input_folder="./police_data" --output_file="./output.json" --date_start="2023-01" --date_end="2023-06"

```
