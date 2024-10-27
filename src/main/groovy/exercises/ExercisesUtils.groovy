package exercises

class ExercisesUtils {

    final static List<List<Double>> STUDENTS_ACCOMMODATIONS_LOCATIONS_TO_CHECK = [
            [52.632064, -1.136287], // IQ Accommodation
            [52.626136, -1.142365], // The Summit
            [52.619579, -1.135082], // Freeman's common
            [52.609383, -1.090573], // The Village
            [52.636287, -1.143154], // Merlin Heights
            [52.631924, -1.129743] // Home for students dover street apartments
    ]

    //To check whether the crime location is near to listed student accommodation logic or not
    static boolean isWithin1Km(double targetLatitude, double targetLongitude, crime) {
        double crimeLocationLatitude = extractFirstElementOrValue(crime.location.geo.lat)
        double crimeLocationLongitude = extractFirstElementOrValue(crime.location.geo.lng)
        double distance = calculateDistance(targetLatitude, targetLongitude, crimeLocationLatitude, crimeLocationLongitude)
        return distance <= 1.0
    }

    static double extractFirstElementOrValue(def value) {
        return (value instanceof List) ? value[0] : value
    }

    //Distance on a sphere: The Haversine Formula referred https://community.esri.com/t5/coordinate-reference-systems-blog/distance-on-a-sphere-the-haversine-formula/ba-p/902128 and https://www.movable-type.co.uk/scripts/latlong.html
    static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        def R = 6371
        def dLat = Math.toRadians(lat2 - lat1)
        def dLon = Math.toRadians(lon2 - lon1)
        def a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        def c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c // Distance in kilometers
    }

    // To check if the newCrime from other student accommodation location has already been identified and checked to avoid duplication
    static boolean isDuplicate(existingCrime, newCrime) {
        return existingCrime.location == newCrime.location &&
                existingCrime.lat == newCrime.lat &&
                existingCrime.lng == newCrime.lng &&
                existingCrime.crime_type == newCrime.crime_type &&
                existingCrime.date == newCrime.date
    }

    // Collect all crimes happening w.r.t location, crime_type, last_outcome_category and date
    static List<Map<String, Object>> filterCrimes(List<Map<String, Object>> allCrimes) {
        return allCrimes.collect { crime ->
            [
                    location: crime?.location,
                    crime_type: crime?.crime_type,
                    last_outcome_category: crime?.last_outcome_category,
                    date: crime?.date
            ]
        }
    }
}
