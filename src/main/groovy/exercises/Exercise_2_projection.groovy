package exercises

import groovy.json.JsonSlurper
import groovy.json.JsonOutput


def parser = new JsonSlurper()


def file = new File('src/main/resources/police_crime_data.json')
crime_data_map = parser.parse(file)

def crimesList = crime_data_map['leicestershire-street'] ?: []
def projectedCrimes = crimesList.collect { crime ->
	if (crime?.location?.geo) {
		[
			lat: extractFirstElementOrValue(crime.location.geo.lat),
			lng: extractFirstElementOrValue(crime.location.geo.lng),
			crime_type: crime?.crime_type,
			last_outcome_category: crime?.last_outcome_category
		]
	} else {
		print("Crime object missing location or geo data: $crime")
		null
	}
}.findAll { it != null }

// To print the projectedCrimes after converting to Json
def jsonOutput = JsonOutput.toJson(projectedCrimes)
println "\nProjected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutput)}"

double extractFirstElementOrValue(def value) {
	return (value instanceof List) ? value[0] : value
}