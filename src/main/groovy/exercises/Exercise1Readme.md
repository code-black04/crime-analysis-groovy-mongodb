# Dataset Proposal
## Police UK Crime Dataset - Leicestershire (Jan 2024 - Aug 2024)

### Category | Details
--- | ---
**Dataset Source** | Police UK
**Dataset Scope** | Crimes reported in Leicestershire between January 2024 and August 2024
**Link** | [Police UK Dataset](https://data.police.uk/)
**Format** | CSV
**Number of Records** | 9,359

## Schema Overview

| Field         | Description                                             | Data Type |
|---------------|---------------------------------------------------------|-----------|
| **Crime ID**  | Unique identifier for each crime                        | String    |
| **Month**     | Month in which the crime occurred                       | String    |
| **Reported by**| Organization or entity reporting the crime             | String    |
| **Falls within**| Police force responsible for solving the crime        | String    |
| **Longitude** | Longitude coordinate of the crime location              | Float     |
| **Latitude**  | Latitude coordinate of the crime location               | Float     |
| **Location**  | Brief description of where the crime happened           | String    |
| **LSOA Code** | Lower Layer Super Output Area code (geographical area)  | String    |
| **LSOA Name** | Lower Layer Super Output Area name                      | String    |
| **Crime Type**| Type/category of the crime (e.g., burglary, assault)    | String    |
| **Last Outcome**| Latest resolution or status of the crime              | String    |

## Intended Queries

- Retrieve crimes that occurred near the university accommodations.
- Group crimes by:
  - Type of Crime (e.g., burglary, robbery, etc.)
  - Last Outcome Resolution (e.g., solved, under investigation, no further action)

## Explanation of Queries

- **Data Selection**: Select all the crimes in the dataset that happened in the last 4 months.
- **Data Projection**: Select only the latitude, longitude, crime type, and last outcome category.
- **Data Filtering**: Filter crimes within a 1 km radius from these locations:
  - 52.62557255880064, -1.1274648376762608 (IQ accommodation)
  - 52.6261360864929, -1.1423652790954384 (The Summit)
  - 52.61957967664474, -1.1350820606701382 (Freemen’s Common)
  - 52.60938312669258, -1.0905735877789204 (The Village)
  - 52.6362874346052, -1.143154206590345 (Merlin Heights)
  - 52.63192491561155, -1.1297436947865873 (Homes for Students Dover Street Apartments)
- **Data Combination**: Sum total cases grouped by location and crime type.


Link to document: https://docs.google.com/document/d/1uKzc5ywcvgoFMXAJhTSx21n6-sOmbhmK4fAWK5J1HSk/edit?usp=sharing