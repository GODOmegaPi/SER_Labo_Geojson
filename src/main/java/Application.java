import java.io.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Application {
    static final String FILE = "data/countries.geojson";

    public static void main(String[] args) {
        JSONParser jsonParser = new JSONParser();
        SAXBuilder builder = new SAXBuilder();

        try (FileReader reader = new FileReader(FILE)) {
            // KML
            Document document = new Document();
            Element kml = new Element("kml");//.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");

            // JSON
            Object obj = jsonParser.parse(reader);
            JSONObject geojson = (JSONObject) obj;

            Element doc = new Element("Document");

            JSONArray features = (JSONArray) geojson.get("features");

            features.forEach(feature -> {
                Element placemark = parseFeaturesArray((JSONObject)feature);
                doc.addContent(placemark);
            });

            kml.addContent(doc);

            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat().setEncoding("UTF-8"));
            xmlOutputter.output(kml, new FileWriter("data/countries.kml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element parseFeaturesArray(JSONObject feature) {
        // KML
        Element placemark = new Element("Placemark");
        Element extendedData = new Element("ExtendedData");

        // JSON
        JSONObject properties = (JSONObject) feature.get("properties");
        JSONObject geometry = (JSONObject) feature.get("geometry");

        // Adding fullname of the land
        Element data = new Element("Data").setAttribute("name", "ADMIN");
        data.addContent(new Element("value").setText(properties.get("ADMIN").toString()));
        extendedData.addContent(data);

        // Adding ISO_A3 name of the land
        data = new Element("Data").setAttribute("name", "ISO_A3");
        data.addContent(new Element("value").setText(properties.get("ISO_A3").toString()));
        extendedData.addContent(data);

        placemark.addContent(extendedData);

        System.out.println("(" + properties.get("ISO_A3") + ") " + properties.get("ADMIN"));
        if(geometry.get("type").equals("Polygon")){
            JSONArray coordinates = (JSONArray)geometry.get("coordinates");
            System.out.println("\t  - " + ((JSONArray)coordinates.get(0)).size() + " coordinates");

            Element polygon = new Element("Polygon");
            Element outerBoundaryIs = new Element("outerBoundaryIs");
            Element linearRing = new Element("LinearRing");

            Element coords = new Element("coordinates");
            ((JSONArray)coordinates.get(0)).forEach(coordinate -> {
                coords.setText(coords.getText() + ((JSONArray)coordinate).get(0) + "," + ((JSONArray)coordinate).get(1) + " ");
            });
            coords.setText(coords.getText().substring(0, coords.getText().length() - 1));

            placemark.addContent(polygon.addContent(outerBoundaryIs.addContent(linearRing.addContent(coords))));
        } else {
            JSONArray coordinates = (JSONArray)geometry.get("coordinates");
            Element multiGeometry = new Element("MultiGeometry");
            coordinates.forEach((arrays) -> {
                System.out.println("\t  - " + ((JSONArray)((JSONArray)arrays).get(0)).size() + " coordinates");

                Element polygon = new Element("Polygon");
                Element outerBoundaryIs = new Element("outerBoundaryIs");
                Element linearRing = new Element("LinearRing");

                Element coords = new Element("coordinates");
                ((JSONArray)((JSONArray)arrays).get(0)).forEach(coordinate -> {
                    coords.setText(coords.getText() + ((JSONArray)coordinate).get(0) + "," + ((JSONArray)coordinate).get(1) + " ");
                });
                coords.setText(coords.getText().substring(0, coords.getText().length() - 1));

                multiGeometry.addContent(polygon.addContent(outerBoundaryIs.addContent(linearRing.addContent(coords))));
            });

            placemark.addContent(multiGeometry);
        }

        return placemark;
    }
}
