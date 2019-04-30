import java.io.*;

import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Application {
    static final String FILE = "data/countries.geojson";

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();

        try (FileReader reader = new FileReader(FILE)) {
            Object obj = jsonParser.parse(reader);

            JSONObject geojson = (JSONObject) obj;

            JSONArray features = (JSONArray) geojson.get("features");

            features.forEach(feature->parseFeaturesArray((JSONObject)feature));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseFeaturesArray(JSONObject feature) {
        JSONObject properties = (JSONObject) feature.get("properties");
        JSONObject geometry = (JSONObject) feature.get("geometry");

        System.out.println("(" + properties.get("ISO_A3") + ") " + properties.get("ADMIN"));
        if(geometry.get("type").equals("Polygon")){
            JSONArray coordinates = (JSONArray)geometry.get("coordinates");
            System.out.println("\t  - " + ((JSONArray)coordinates.get(0)).size() + " coordinates");
        } else {
            JSONArray coordinates = (JSONArray)geometry.get("coordinates");
            coordinates.forEach((arrays) -> System.out.println("\t  - " + ((JSONArray)((JSONArray)arrays).get(0)).size() + " coordinates"));
        }
    }
}
