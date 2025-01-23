## Exercise 3 – MongoDB Script Implementation in Groovy

In this a Groovy script is implemented to analyze crime data stored in MongoDB. The script performs various operations, including data loading, querying for recent data, projection of specific fields, filtering by proximity to student accommodations, and grouping for summarized reports.

**Prerequisites**
- MongoDB Atlas cluster (or a MongoDB instance)
- Groovy for scripting
- JSON file containing crime data to load
- A properties file (mongodb.properties) with MongoDB credentials
---

### Mongo Db Connectivity and Load file
The script begins by establishing a connection to MongoDB, loading credentials from a properties file, and using them to securely connect to the database. Data is then loaded into the specified MongoDB collection from a JSON file, ensuring a refreshed data set by deleting any existing documents in the collection.

**Key Code Snippet Highlights**

```groovy
// Load credentials from properties file
def properties = new Properties()
new File('src/main/resources/mongodb.properties').withInputStream { properties.load(it) }

// MongoDB connection setup
def mongoClient = MongoClients.create("mongodb+srv://${properties.USN}:${properties.PWD}@${properties.CLUSTER}.${properties.SERVER}.mongodb.net/${properties.DB}?retryWrites=true&w=majority")
def db = mongoClient.getDatabase(properties.DB)

// Data load function
def loadData = { collection, filePath ->
    collection.deleteMany(new Document()) // Reset collection
    def jsonFile = new File(filePath)
    def crimeDataMap = new JsonSlurper().parse(jsonFile)

    def insertDocuments = []
    crimeDataMap.each { street, crimes ->
        crimes.each { crime ->
            def doc = Document.parse(JsonOutput.toJson(crime))
            doc.put("street", street) // Add street field
            insertDocuments << doc
        }
    }

    collection.insertMany(insertDocuments) // Insert data into MongoDB
    println "${insertDocuments.size()} documents loaded."

    // Add exception handling and logging if needed for error management
}

```

**Outcome**: The collection is populated with documents from the provided JSON file.

---

### Selection Query
**Query Definition:** Selection of crimes in the last 4 months.​

The selection query extracts records of crimes reported within the last four months. The script calculates the date threshold by subtracting four months from the current date and uses this date as a filter criterion in the query.  The selected crimes are then displayed in JSON format, providing a structured overview of recent crimes.

**Key Code Snippet Highlights** & for **Outcome:** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/ESjYhXgkUVVGvJp7mlhnQ6wBxgaairRs-bII68Wn6ehqUQ?e=Bqlbqt)
```groovy
// Calculating the date 4 months ago from today
// Define a date threshold by calculating the date 4 months ago
def fourMonthsAgo = LocalDate.now().minusMonths(4).format(DateTimeFormatter.ofPattern("yyyy-MM"))
def pipeline_1 = [
    match(gte("date", fourMonthsAgo)) // Filter by date
]
// Implement printResult function for displaying results (e.g., JSON output)
}

def jsonOutputSelected = JsonOutput.toJson(selectedCrimes)
println "\nSelected Crimes in JSON format:\n${JsonOutput.prettyPrint(jsonOutputSelected)}"
```



---

### Projection Query
**Query Definition:** Select only the latitude, longitude, crime type and last outcome category.

This query performs a projection to retrieve only specific fields from each crime document. The fields selected include latitude, longitude, crime type, and the last known outcome category of each crime. Using a pipeline, the script structures the data to return these essential details for each crime, facilitating focused analysis.

**Key Code Snippet Highlights** & for **Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EcaG_gLCwHtDoapA-0_cbZUBlzXSPOuI1Az6grnJRN6QSg?e=AXepra)
```groovy
// Projection of Data
def pipeline_2 = [
    project(new Document()
        .append("lat", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 1])) // Get latitude
        .append("lng", new Document("\$arrayElemAt", ["\$location.geo.coordinates", 0])) // Get longitude
        .append("crime_type", 1) // Include crime type
        .append("last_outcome_category", 1) // Include last outcome category
    )
]

// Implement printResult function for displaying results in JSON format
```

---

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

In the filtering query, the script selects crimes that occurred within a 1-kilometer radius of pre-defined student accommodation locations. By using the $geoNear operator, the script filters the data based on geographic proximity. The projection further refines the data by selecting only location details, crime type, and the last outcome category. Unique crimes are then added to a list, ensuring no duplicate records are included in the final output.

**Key Code Snippet Highlights** & for **Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EfjssWtsncxGgbWuwCPyPpMBL5Uxol8KFPB6ldR4_-eAcQ?e=FFAez7)
```groovy
//col.createIndex(new Document("location.geo", "2dsphere"))
// Iterate over list of student accommodation locations
locationsToCheck.each { targetLocation ->
    def pipeline_3 = [
        new Document("\$geoNear", new Document()
            .append("near", new Document("type", "Point").append("coordinates", [targetLocation[1], targetLocation[0]])) // Set target location
            .append("distanceField", "distance") // Name field for calculated distance
            .append("maxDistance", 1000) // Set max distance to 1km
            .append("spherical", true) // Spherical geometry for calculation
        ),
        project(fields(include("location", "crime_type", "last_outcome_category"), // Include essential fields
            computed("lat", "\$location.geo.lat"), // Compute latitude
            computed("lng", "\$location.geo.lng")  // Compute longitude
        ))
    ]
    // Aggregate results and check for unique crimes
    // Implement logic to store unique records and avoid duplicates using Hashset
}
```

---

### Grouping and Combination Query
**Query Definition:** With the data filtered, the sum of total cases grouped by location and crime type​.​

This query combines filtering and grouping operations. After filtering crimes within a 1-kilometer radius of specified locations, the script groups the results by location and crime type to provide a count of total cases for each category. This grouping gives a summarized view of crime frequency and types for each area, enabling more focused reporting.

**Key Code Snippet Highlights** & for **Outcome** [Click here](https://uniofleicester-my.sharepoint.com/:i:/g/personal/pm455_student_le_ac_uk/EZ1nfx8GrsZOnuXhuuHp0fYBsxXVhWk6YEImZh30tPsKyQ?e=rftY0h)
```groovy
// Combination & Grouping of Data
// Iterate over list of student accommodation locations
locationsToCheck.each { targetLocation ->
    def pipeline_3 = [
        new Document("\$geoNear", new Document()
            .append("near", new Document("type", "Point").append("coordinates", [targetLocation[1], targetLocation[0]])) // Set target location
            .append("distanceField", "distance") // Name field for calculated distance
            .append("maxDistance", 1000) // Set max distance to 1km
            .append("spherical", true) // Spherical geometry for calculation
        ),
        project(fields(include("location", "crime_type", "last_outcome_category"), // Include essential fields
            computed("lat", "\$location.geo.lat"), // Compute latitude
            computed("lng", "\$location.geo.lng")  // Compute longitude
        ))
	group(
		new Document("location", "\$location.address")
			.append("crime_type", "\$crime_type"),
		sum("crime_count", 1)
	)
    ]
    // Aggregate results and check for unique crimes
    // Implement logic to store unique records and avoid duplicates using HashMap
}
```

---
