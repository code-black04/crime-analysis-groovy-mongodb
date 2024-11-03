package exercises

@Grab('org.slf4j:slf4j-simple:1.7.30')
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import java.time.LocalDate
import java.time.format.DateTimeFormatter

Logger log = LoggerFactory.getLogger(this.class)

// Util func to measure time and memory
def measureExecutionTimeAndMemory(Logger log, String operationName, Closure operation) {
	//	Check mem ref: https://gist.github.com/jhoblitt/21f46d853fee9b70be8c2e5873d2e621
	def startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
	def startTime = System.currentTimeMillis()
	
	// Execute...
	def result = operation()
	
	def endTime = System.currentTimeMillis()
	def endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
	
	log.info("$operationName Execution Time: ${(endTime - startTime)} ms")
	log.info("$operationName Memory Usage: ${(endMemory - startMemory) / (1024 * 1024)} MB")
	
	return result
}

def jsonFile = new File('src/main/resources/police_crime_data.json')

def jsonSlurper = new JsonSlurper()

def crime_data_map

try {
	crime_data_map = jsonSlurper.parse(jsonFile)
} catch (Exception e) {
    log.error("Error parsing JSON", e)
    return
}


//log.info("Converted Map from file: ${crime_data_map}")

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


//To filter the crime data happened within 1km of radius of Students Accommodations
def filteredCrimes = measureExecutionTimeAndMemory(log, "Data Filtering") {
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
	
	return ExercisesUtils.filterCrimes(allCrimes)
}

//To print the filteredCrimes after converting to Json
def jsonOutput = JsonOutput.toJson(filteredCrimes)
println "\nAll Filtered Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutput)}"


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
