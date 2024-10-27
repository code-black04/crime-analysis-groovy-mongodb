package exercises

@Grab('org.slf4j:slf4j-simple:1.7.30')
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.time.LocalDate
import java.time.format.DateTimeFormatter

Logger log = LoggerFactory.getLogger(this.class)

//To-do: Change the location of json to police_crime_date.json before submitting assignment
def jsonFile = new File('src/main/resources/police_crime_data.json')

def jsonSlurper = new JsonSlurper()

def crime_data_map

try {
	crime_data_map = jsonSlurper.parse(jsonFile)
} catch (Exception e) {
    log.error("Error parsing JSON", e)
    return
}


log.info("Converted Map from file: ${crime_data_map}")

// Selection of Data

def NUM_MONTHS = 4
def formatter = DateTimeFormatter.ofPattern("yyyy-MM")
def monthsAgo = LocalDate.now().minusMonths(NUM_MONTHS).format(formatter)
def selectedCrimes = crime_data_map['leicestershire-street'].collect { it }.findAll { it != null && it.date >= monthsAgo }.sort { a, b -> b.date <=> a.date } 
def jsonOutputSelected = JsonOutput.toJson(selectedCrimes)
println "\nSelected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputSelected)}"


// Projection of Data

def projectedCrimes = crime_data_map['leicestershire-street'].collect { crime ->
	if (crime?.location?.geo) {
		[
			lat: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lat),
			lng: ExercisesUtils.extractFirstElementOrValue(crime.location.geo.lng),
			crime_type: crime?.crime_type,
			last_outcome_category: crime?.last_outcome_category
		]
	} else {
		print("Crime object missing location or geo data: $crime")
		null
	}
}.findAll { it != null }

// To print the projectedCrimes after converting to Json
def jsonOutputProjected = JsonOutput.toJson(projectedCrimes)
println "\nProjected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputProjected)}"


//To filter the crime data happened within 1km of radius of Students Accommodations
def locationsToCheck = ExercisesUtils.STUDENTS_ACCOMMODATIONS_LOCATIONS_TO_CHECK

def allCrimes = []

locationsToCheck.each { targetLocation ->
	double targetLatitude = targetLocation[0]
	double targetLongitude = targetLocation[1]

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

def filteredCrimes = ExercisesUtils.filterCrimes(allCrimes)

//To print the filteredCrimes after converting to Json
def jsonOutput = JsonOutput.toJson(filteredCrimes)
println "\nAll Filtered Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutput)}"


// Combination & Grouping of Data
def groupedByLocation = filteredCrimes.groupBy { it.location }
		.collectEntries { location, crimes ->
			[location, crimes.groupBy { it.crime_type }.collectEntries { crime_type, entries -> [crime_type, entries.size()]}]}
def jsonOutputGrouped = JsonOutput.toJson(groupedByLocation)
println "\nAll Grouped Crimes by location and crime type in JSON format:\n${JsonOutput.prettyPrint(jsonOutputGrouped)}"
