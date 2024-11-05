## Exercise 2 – Groovy Script Implementation 

This task involves evaluating the crime information using Groovy. The objective is to analyze a dataset of crime incidents and utilize various methods to extract valuable information. The examination is divided into four stages:

 - Selection Query: Select crimes that have occurred in the last 4 months.
 - Projection Query: Extract specific details such as crime type, location (latitude and longitude), and the last outcome category.
 - Filtering Query: Focus on crimes that happened within a 1km radius of student accommodations.
 - Grouping and Combination Query: Group the filtered crimes by location and crime type, then count how many times each crime occurred in each location.
 
---

### Selection Query
**Query Definition:** Selection of crimes in the last 4 months.​

This query filters the crime data to include only incidents that occurred within the last four months. It calculates a reference date by subtracting four months from the current date. Then, it processes the Leicestershire street crime data, selects non-null records that occurred after this reference date, and sorts the records in descending order, with the most recent crimes appearing first.

**Key Code Snippet Highlighths** & **Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EbEjMaUy0bFAlatqIxm94GIBTzpOabQ-WkZVkTzvqfsFRw?e=3gEjaZ)
```groovy
// Selection of Data
def NUM_MONTHS = 4
def formatter = DateTimeFormatter.ofPattern("yyyy-MM")
def monthsAgo = LocalDate.now().minusMonths(NUM_MONTHS).format(formatter)
def selectedCrimes = measureExecutionTimeAndMemory(log, "Data Selection") {
	crime_data_map['leicestershire-street']
		.collect { it }
		.findAll { it != null && it.date >= monthsAgo }
//		.findAll { it != null }  // For time execution using full selection of data...
		.sort { a, b -> b.date <=> a.date }
}


// prints Selected Crimes in JSON format
```

**Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EbEjMaUy0bFAlatqIxm94GIBTzpOabQ-WkZVkTzvqfsFRw?e=3gEjaZ)

---

### Projection Query
**Query Definition**: Select only the latitude, longitude, crime type and last outcome category.

In this query ,we focus on the most important details: crime type, latitude, longitude, and the last outcome category. This reduces the dataset size, making it easier to analyze the key information. Crimes without geographic data are excluded, as location is essential for mapping and further analysis.


**Key Code Snippet Highlighths** & **Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EfrmsYiWy2hFljqQTBukD6EBN93UUugDUm5L55Q_z5PcJg?e=2svXvJ)
```groovy
// Projection of Data
def projectedCrimes = measureExecutionTimeAndMemory(log, "Data Projection") {
	crime_data_map['leicestershire-street'].collect { crime ->
		if (crime?.location?.geo) {
			[
				lat: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lat),
				lng: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lng),
				crime_type: crime?.crime_type,
				last_outcome_category: crime?.last_outcome_category
			]
		} else {
	//		print("Crime object missing location or geo data: $crime")
			null
		}
	}.findAll { it != null }
}

// To print the projectedCrimes after converting to Json

```
**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EfrmsYiWy2hFljqQTBukD6EBN93UUugDUm5L55Q_z5PcJg?e=2svXvJ)

**Note**: Students Accommodations considered in filtering, grouping and combination query

```groovy
    final static List<List<Double>> STUDENTS_ACCOMMODATIONS_LOCATIONS_TO_CHECK = [
            [52.632064, -1.136287], // IQ Accommodation
            [52.626136, -1.142365], // The Summit
            [52.619579, -1.135082], // Freeman's common
            [52.609383, -1.090573], // The Village
            [52.636287, -1.143154], // Merlin Heights
            [52.631924, -1.129743] // Home for students dover street apartments
    ]
```

---

### Filtering Query
**Query Definition:** Select only the location, crime type and last outcome category of crimes that happened in a radius of 1 km from Students accommodations​.

This query filters the dataset to include only crimes that occurred within 1km of student accommodations. This helps us understand crime patterns near student areas.
The Haversine formula is ben used inorder to calculate the distance between crime locations and the accommodation points. For each location, we extract its coordinates and check if each crime is within the 1km radius. Only crimes with valid coordinates within this distance are kept. We then gather essential details like latitude, longitude, crime type, and date, filling in missing information with default values and ensuring there are no duplicates before adding the records.

**Key Code Snippet Highlighths** & **Outcome:**[Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EUyu5S9eXEtPhdC91WZF4k0BHYE-VfyEpZ5PGWxXjDrDIQ?e=YVqvE6)


```groovy
//To filter the crime data happened within 1km of radius of each Students Accommodations
def filteredCrimes = measureExecutionTimeAndMemory(log, "Data Filtering") {
	def locationsToCheck = ExercisesUtils.STUDENTS_ACCOMMODATIONS_LOCATIONS_TO_CHECK
	def allCrimes = []
	
	locationsToCheck.each { targetLocation ->
		double targetLatitude = targetLocation[0]
		double targetLongitude = targetLocation[1]

    //isWithinKm extracts the latitide and longitude and calculate the distance b/w two coordinates using Haversine Formula
		def crimesWithin1Km = crime_data_map['leicestershire-street'].findAll { crime ->
			crime?.location?.geo?.lat && crime?.location?.geo?.lng && ExercisesUtils.isWithin1Km(targetLatitude, targetLongitude, crime) 
		}
	
		crimesWithin1Km.each { crime ->
			def crimeDetails = [
					lat: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lat),
					lng: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lng),
					location: crime?.location?.address ?: null,
					crime_type: crime?.crime_type ?: 'Unknown',
					date: crime?.date ?: 'Unknown'
			]
			// Add unique crime by comparing location, latitude, longitude, crime_type and date
			if (!allCrimes.any { existingCrime -> ExercisesUtils.isDuplicate(existingCrime, crimeDetails)}) {
				allCrimes << [
						location: crime?.location?.address,
						lat: crimeDetails.lat,
						lng: crimeDetails.lng,
						crime_type: crimeDetails.crime_type,
						date: crimeDetails.date,
						last_outcome_category: crime?.last_outcome_category
				]
			}
		}
	}
	
	return ExercisesUtils.filterCrimes(allCrimes)
}

//To print the filteredCrimes after converting to Json

```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EUyu5S9eXEtPhdC91WZF4k0BHYE-VfyEpZ5PGWxXjDrDIQ?e=YVqvE6)

---

### Grouping and Combination Query
**Query Definition:** With the data filtered, the sum of total cases grouped by location and crime type​.​

Finally, in this query , we then group the filtered crimes by location and crime type. This helps us see how many incidents of each crime type occurred in each location, providing a clearer picture of crime patterns.
The organization of the data is done by by creating a list of crimes for each location. Within each location, we then group the crimes by type and count how many incidents of each type occurred. This gives us valuable insights into the most common crimes in specific areas and helps identify potential hotspots.

**Key Code Snippet Highlighths** & **Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/Ec1fQnPI3ZpBiQBDH---G58BEIurXOdJ3C0GemmYwCqEqg?e=ZsW4ky)
```groovy
// Combination & Grouping of Data
def groupedByLocation = measureExecutionTimeAndMemory(log, "Data Combination and Grouping") {
	filteredCrimes
		.groupBy { it.location }
		.collectEntries { location, crimes ->
			[
				location, 
				crimes
					.groupBy { it.crime_type }
					.collectEntries { crime_type, entries -> [crime_type, entries.size()]}
			]
		}
}

// print All Grouped Crimes by location and crime type in JSON format
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/Ec1fQnPI3ZpBiQBDH---G58BEIurXOdJ3C0GemmYwCqEqg?e=ZsW4ky)

---
