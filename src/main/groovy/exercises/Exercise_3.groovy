package exercises


@Grab('org.slf4j:slf4j-simple:1.7.30')
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import static com.mongodb.client.model.Accumulators.*
import static com.mongodb.client.model.Aggregates.*
import static com.mongodb.client.model.Filters.*
import static com.mongodb.client.model.Sorts.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import static com.mongodb.client.model.Projections.*

import org.bson.Document

import com.mongodb.client.MongoClients

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.BucketAutoOptions
import com.mongodb.client.model.Facet
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.mongodb.client.model.geojson.Polygon


Logger log = LoggerFactory.getLogger(this.class)

// Load credentials from src/main/resources/mongodb.properties
def properties = new Properties()
def propertiesFile = new File('src/main/resources/mongodb.properties')
propertiesFile.withInputStream {
	properties.load(it)
}

// Create MongoDB connection
def mongoClient = MongoClients.create("mongodb+srv://${properties.USN}:${properties.PWD}@${properties.CLUSTER}.${properties.SERVER}.mongodb.net/${properties.DB}?retryWrites=true&w=majority")
def db = mongoClient.getDatabase(properties.DB)

// Load data into mongoDB
def loadData = { collection, filePath ->
	collection.deleteMany(new Document()) // reset collection
	def jsonFile = new File(filePath)
	def jsonSlurper = new JsonSlurper()
	
	try {
		def crimeDataMap = jsonSlurper.parse(jsonFile)
		def insertDocuments = []
		
		log.info("Creating docs...")
		long startDocCreateTime = System.currentTimeMillis()
		crimeDataMap.each { street, crimes ->
			crimes.each { crime ->
				def doc = Document.parse(JsonOutput.toJson(crime))
				doc.put("street", street) // Insert area as a separate field
				insertDocuments << doc
			}
		}
		log.info("Documents created in ${(System.currentTimeMillis() - startDocCreateTime) / 1000.0} seconds")
		
		if (!insertDocuments.isEmpty()) {
			log.info("Loading docs into mongodb... please wait.")
			long startDBInsertTime = System.currentTimeMillis()
			
			collection.insertMany(insertDocuments)
			log.info("${insertDocuments.size()} documents loaded in ${(System.currentTimeMillis() - startDBInsertTime) / 1000.0} seconds.")
		} else {
			log.info("No Documents to insert..")
		}
		
	} catch (Exception e) {
		log.error("Error loading crime data: ", e)
		// System/Script exit here: do not proceed...
	}
}


def col = db.getCollection("police_crime_data")

// DO NOT UNCOMMENT!! (Only required once): Inserts data into collection
//loadData(col, "src/main/resources/police_crime_data.json");

//function to print pipeline results
def printResult(exercise, col, pipeline,querydefinition) {
	def result = col.aggregate(pipeline).into([])
	println("----------------------")
	println("EXERCISE ${exercise}: ${querydefinition}")
	result.each { println it }
}

// Calculating the date 4 months ago from today
def numberOfMonths = 4
def dateFormat = DateTimeFormatter.ofPattern("yyyy-MM")
def fourMonthsAgo = LocalDate.now().minusMonths(numberOfMonths).format(dateFormat)


//Pipeline to get the data of last four months
def pipeline_3 = [  
	match(gte("date", fourMonthsAgo))		 		
]

//printing the result
printResult(1, col, pipeline_1, "Selecting data of last four months")

//PROJECTION (latitude,longitude,crime_type,last_outcome_category)

def pipeline_2 = [
	project(new Document()
		.append("lat", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 1]))
		.append("lng", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 0]))
		.append("crime_type", 1)
		.append("last_outcome_category", 1)
	)
]
 
printResult(2, col, pipeline_2, "Project latitue, longtitude, crime_type and last_outcome_category_of_crimes")


//Filtering Query
def locationsToCheck = ExercisesUtils.STUDENTS_ACCOMMODATIONS_LOCATIONS_TO_CHECK

def allCrimes = []

//DO NOT UNCOMMENT!! (Only required once): To give "Point" type to geo.lat and geo.lng
//To perform indexing on location.geo
/*col.createIndex(new Document("location.geo", "2dsphere"))
def batchSize = 100
def lastId = null
def documents = []

while (true) {
	def query = lastId ? new Document("_id", new Document("\$gt", lastId)) : new Document()
	documents = col.find(query).sort(new Document("_id", 1)).limit(batchSize).into([])

	if (documents.isEmpty()) break

	documents.each { doc ->
		def lat = doc.get("location")?.get("geo")?.get("lat")
		def lng = doc.get("location")?.get("geo")?.get("lng")

		if (lat && lng) {
			def geoJsonPoint = new Document("type", "Point").append("coordinates", [lng, lat])
			col.updateOne(
					new Document("_id", doc.get("_id")),
					new Document("\$set", new Document("location.geo", geoJsonPoint))
			)
			log.info("Updated document with _id: ${doc.get("_id")}")
		}
		lastId = doc.get("_id")
	}
}

log.info("Process completed to have geo object of Type as Point")*/

//To have set of unique crimes happening
def uniqueCrimes = new HashSet<>()

locationsToCheck.each { targetLocation ->
	double targetLatitude = targetLocation[0]
	double targetLongitude = targetLocation[1]

	def pipeline_3 = [
			new Document("\$geoNear", new Document()
					.append("near", new Document("type", "Point")
							.append("coordinates", [targetLongitude, targetLatitude]))
					.append("distanceField", "distance")
					.append("maxDistance", 1000)
					.append("spherical", true)
			),
			project(fields(include("location", "crime_type", "date", "last_outcome_category"),
					computed("lat", "\$location.geo.lat"),
					computed("lng", "\$location.geo.lng"))),
			group(
					new Document("location", "\$location.address")
							.append("lat", "\$lat")
							.append("lng", "\$lng")
							.append("crime_type", "\$crime_type")
							.append("date", "\$date"),
					first("location", "\$location.address"),
					first("lat", "\$lat"),
					first("lng", "\$lng"),
					first("crime_type", "\$crime_type"),
					first("date", "\$date"),
					first("last_outcome_category", "\$last_outcome_category")
			)
	]

	def result = col.aggregate(pipeline_3).into([])
	// Add only unique crimes to allCrimes
	result.each { doc ->
		def uniqueKey = "${doc._id}"
		if (!uniqueCrimes.contains(uniqueKey)) {
			uniqueCrimes.add(uniqueKey)
			// Add to allCrimes if unique
			allCrimes << doc
		}
	}
}
println("\nFilter Query: Find all the crimes happened within 1 km radius of Students Accommodations\n")
uniqueCrimes.each { println it }

def uniqueCrimesDataCombination = new HashMap()

locationsToCheck.each { targetLocation ->
	double targetLatitude = targetLocation[0]
	double targetLongitude = targetLocation[1]

	def pipeline_4 = [
	    new Document("\$geoNear", new Document()
	            .append("near", new Document("type", "Point")
	                    .append("coordinates", [targetLongitude, targetLatitude]))
	            .append("distanceField", "distance")
	            .append("maxDistance", 1000)
	            .append("spherical", true)
	    ),
	    project(fields(include("location", "crime_type", "date", "last_outcome_category", "crime_count"),
	            computed("lat", "\$location.geo.lat"),
	            computed("lng", "\$location.geo.lng")
				)),
	    group(
			new Document("location", "\$location.address")
				.append("crime_type", "\$crime_type"),
			sum("crime_count", 1)
		)
	]

	def result = col.aggregate(pipeline_4).into([])
	 printResult(4, col, pipeline_4,"Testing")
	// Add only unique crimes to allCrimes
	result.each { doc ->
		uniqueCrimesDataCombination.put(doc._id, doc.crime_count)
	}
}
println("\nData combination\n")
uniqueCrimesDataCombination.each { println it }
