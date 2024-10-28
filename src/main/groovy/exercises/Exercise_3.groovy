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
import static com.mongodb.client.model.Projections.*

import org.bson.Document

import com.mongodb.client.MongoClients

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.BucketAutoOptions
import com.mongodb.client.model.Facet

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
def printResult(exercise, col, pipeline) {
	def result = col.aggregate(pipeline).into([])
	println("----------------------")
	println("EXERCISE ${exercise}: Selecting data of last four months")
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
printResult(3, col, pipeline_3)

