package exercises

@Grab('org.slf4j:slf4j-simple:1.7.30')
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

Logger log = LoggerFactory.getLogger(this.class)

//To-do: Change the location of json to police_crime_date.json before submitting assignment
def jsonFile = new File('src/main/resources/testing.json')

def jsonSlurper = new JsonSlurper()

def crime_data_map

try {
	crime_data_map = jsonSlurper.parse(jsonFile)
} catch (Exception e) {
    log.error("Error parsing JSON", e)
    return
}

log.info("Converted Map from file: ${crime_data_map}")


def locationsToCheck = [
	[52.632064, -1.136287], //IQ Accommodation
	[52.626136, -1.142365], //The Summit
	[52.619579, -1.135082], //Freeman's common
	[52.609383, -1.090573], // The Village
	[52.636287, -1.143154], // Merlin Heights
	[52.631924, -1.129743] // Home for students dover street apartments
]

def allCrimes = []

locationsToCheck.each { targetLocation ->
	double targetLatitude = targetLocation[0]
	double targetLongitude = targetLocation[1]

	def crimesWithin1Km = crime_data_map['leicestershire-street'].findAll { crime ->
		crime?.location?.geo?.lat && crime?.location?.geo?.lng && isWithin1Km(targetLatitude, targetLongitude, crime)
	}
	
	crimesWithin1Km.each { crime ->
		def crimeDetails = [
			lat: extractFirstElementOrValue(crime.location.geo.lat),
			lng: extractFirstElementOrValue(crime.location.geo.lng),
			location: crime?.location?.address ?: null,
			crime_type: crime?.crime_type ?: 'Unknown',
			date: crime?.date ?: 'Unknown'
		]
		// Add unique crime by comparing location, latitude, longitude, crime_type and date
		if (!allCrimes.any { existingCrime -> isDuplicate(existingCrime, crimeDetails)}) {
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

//To filter the location, crime_type and last_outcome_category
def filteredCrimes = allCrimes.collect { crime ->
	[
		location: crime?.location,
		crime_type: crime?.crime_type,
		last_outcome_category: crime?.last_outcome_category
	]
}

//To print the filteredCrimes after converting to Json
def jsonOutput = JsonOutput.toJson(filteredCrimes)
println "\nAll Filtered Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutput)}"

//To check whether the crime location is near to listed student accommodation logic or not
boolean isWithin1Km(double targetLatitude, double targetLongitude, crime) {
	double crimeLocationLatitude = extractFirstElementOrValue(crime.location.geo.lat)
	double crimeLocationLongitude = extractFirstElementOrValue(crime.location.geo.lng)
	double distance = calculateDistance(targetLatitude, targetLongitude, crimeLocationLatitude, crimeLocationLongitude)
	return distance <= 1.0
}

//Distance on a sphere: The Haversine Formula referred https://community.esri.com/t5/coordinate-reference-systems-blog/distance-on-a-sphere-the-haversine-formula/ba-p/902128 and https://www.movable-type.co.uk/scripts/latlong.html
double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
	def R = 6371
	def dLat = Math.toRadians(lat2 - lat1)
	def dLon = Math.toRadians(lon2 - lon1)
	def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
			Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
			Math.sin(dLon / 2) * Math.sin(dLon / 2)
	def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
	return R * c // Distance in kilometers
}

double extractFirstElementOrValue(def value) {
	return (value instanceof List) ? value[0] : value
}

// To check if the newCrime from other student accommodation location has already been identified and checked to avoid duplication
boolean isDuplicate(existingCrime, newCrime) {
	return existingCrime.location == newCrime.location &&
		   existingCrime.lat == newCrime.lat &&
		   existingCrime.lng == newCrime.lng &&
		   existingCrime.crime_type == newCrime.crime_type &&
		   existingCrime.date == newCrime.date
}