## Exercise 2 – Groovy Script Implementation 

---

### Selection Query
Query Definition: Selection of crimes in the last 4 months.​

**Code**
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

def jsonOutputSelected = JsonOutput.toJson(selectedCrimes)
println "\nSelected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputSelected)}"
```

**Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EbEjMaUy0bFAlatqIxm94GIBTzpOabQ-WkZVkTzvqfsFRw?e=3gEjaZ)

---

### Projection Query
Query Definition: Select only the latitude, longitude, crime type and last outcome category.

**Code**
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
def jsonOutputProjected = JsonOutput.toJson(projectedCrimes)
println "\nProjected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputProjected)}"
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
Query Definition: Select only the location, crime type and last outcome category of crimes that happened in a radius of 1 km from Students accommodations​.

**Code**
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
def jsonOutput = JsonOutput.toJson(filteredCrimes)
println "\nAll Filtered Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutput)}"
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EUyu5S9eXEtPhdC91WZF4k0BHYE-VfyEpZ5PGWxXjDrDIQ?e=YVqvE6)

---

### Grouping and Combination Query
Query Definition: With the data filtered, the sum of total cases grouped by location and crime type​.​

**Code**
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
def jsonOutputGrouped = JsonOutput.toJson(groupedByLocation)
println "\nAll Grouped Crimes by location and crime type in JSON format:\n${JsonOutput.prettyPrint(jsonOutputGrouped)}"
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/Ec1fQnPI3ZpBiQBDH---G58BEIurXOdJ3C0GemmYwCqEqg?e=ZsW4ky)

---
