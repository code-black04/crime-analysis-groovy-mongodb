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
  - Type of Crime

Link to document: https://docs.google.com/document/d/1uKzc5ywcvgoFMXAJhTSx21n6-sOmbhmK4fAWK5J1HSk/edit?usp=sharing