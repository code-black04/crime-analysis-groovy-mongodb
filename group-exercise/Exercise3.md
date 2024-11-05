## Exercise 3 – MongoDB Script Implementation in Groovy

---

### Cloud Connectivity and Load file

**Code**

```groovy
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
```

---

### Selection Query
Query Definition: Selection of crimes in the last 4 months.​

**Code**
```groovy
// Calculating the date 4 months ago from today
def numberOfMonths = 4
def dateFormat = DateTimeFormatter.ofPattern("yyyy-MM")
def fourMonthsAgo = LocalDate.now().minusMonths(numberOfMonths).format(dateFormat)

//Pipeline to get the data of last four months
def pipeline_1 = [  
	match(gte("date", fourMonthsAgo)),
//	match(new Document()),	// Select all data	
	skip((pageNumber - 1) * pageSize),
	limit(pageSize) 		
]
//printing the result
printResult(1, col, pipeline_1, "Selecting data of last four months")
}

def jsonOutputSelected = JsonOutput.toJson(selectedCrimes)
println "\nSelected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputSelected)}"
```

**Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/ESjYhXgkUVVGvJp7mlhnQ6wBxgaairRs-bII68Wn6ehqUQ?e=Bqlbqt)

---

### Projection Query
Query Definition: Select only the latitude, longitude, crime type and last outcome category.

**Code**
```groovy
// Projection of Data
def pipeline_2 = [
	project(new Document()
		.append("lat", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 1]))
		.append("lng", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 0]))
		.append("crime_type", 1)
		.append("last_outcome_category", 1)
	),
	skip((pageNumber - 1) * pageSize),
	limit(pageSize)
]
printResult(2, col, pipeline_2, "Project latitude, longitude, crime_type and last_outcome_category_of_crimes")
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EcaG_gLCwHtDoapA-0_cbZUBlzXSPOuI1Az6grnJRN6QSg?e=AXepra)

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
//col.createIndex(new Document("location.geo", "2dsphere"))
//To filter the crime data happened within 1km of radius of each Students Accommodations
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
								.append("last_outcome_category", "\$last_outcome_category")
								.append("date", "\$date"),
						first("location", "\$location.address"),
						first("lat", "\$lat"),
						first("lng", "\$lng"),
						first("crime_type", "\$crime_type"),
						first("date", "\$date"),
						first("last_outcome_category", "\$last_outcome_category")
				),
				skip((pageNumber - 1) * pageSize),
				limit(pageSize),
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
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EfjssWtsncxGgbWuwCPyPpMBL5Uxol8KFPB6ldR4_-eAcQ?e=FFAez7)

---

### Grouping and Combination Query
Query Definition: With the data filtered, the sum of total cases grouped by location and crime type​.​

**Code**
```groovy
// Combination & Grouping of Data
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
								.append("last_outcome_category", "\$last_outcome_category")
								.append("date", "\$date"),
						first("location", "\$location.address"),
						first("lat", "\$lat"),
						first("lng", "\$lng"),
						first("crime_type", "\$crime_type"),
						first("date", "\$date"),
						first("last_outcome_category", "\$last_outcome_category")
				),
				skip((pageNumber - 1) * pageSize),// For Pagination
				limit(pageSize),
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
```

**Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EZ1nfx8GrsZOnuXhuuHp0fYBsxXVhWk6YEImZh30tPsKyQ?e=rftY0h)

---
