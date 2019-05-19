import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.io.FileWriter;

public class Parser {
    private String inputFile;
    private String outputFile;

    public Parser(String inputFile, String outputFile){
        this.inputFile = inputFile;
        this.outputFile = outputFile;
    }

    public void parse(){
        JSONParser jsonParser = new JSONParser();
        SAXBuilder builder = new SAXBuilder();

        try (FileReader reader = new FileReader(this.inputFile)) {
            // KML
            final Document document = new Document();
            final Element kml = new Element("kml");
            kml.setNamespace(Namespace.getNamespace("http://www.opengis.net/kml/2.2"));

            // JSON
            final Object obj = jsonParser.parse(reader);
            final JSONObject geojson = (JSONObject) obj;

            final Element doc = new Element("Document");
            final Element style = getStyle();
            doc.addContent(style);

            final JSONArray features = (JSONArray)geojson.get("features");

            features.forEach(feature -> {
                Element placemark = parseFeaturesArray((JSONObject)feature);
                doc.addContent(placemark);
            });

            kml.addContent(doc);
            save(kml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element getStyle() {
        final Element style = new Element("Style");
        style.setAttribute("id", "borderColor");

        final Element polyStyle = new Element("PolyStyle");
        polyStyle.addContent(new Element("fill").setText("0"));
        polyStyle.addContent(new Element("outline").setText("1"));

        style.addContent(polyStyle);

        return style;
    }

    private void save(Element root) {
        try {
            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.setFormat(Format.getPrettyFormat().setEncoding("UTF-8"));
            xmlOutputter.output(root, new FileWriter(this.outputFile));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Element parseFeaturesArray(JSONObject feature) {
        // KML
        final Element placemark = new Element("Placemark");
        placemark.addContent(new Element("styleUrl").setText("#borderColor"));
        final Element extendedData = new Element("ExtendedData");

        // JSON
        final JSONObject properties = (JSONObject) feature.get("properties");
        final JSONObject geometry = (JSONObject) feature.get("geometry");

        placemark.addContent(new Element("name").setText(properties.get("ADMIN").toString()));

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
        final JSONArray coordinates = (JSONArray)geometry.get("coordinates");
        if(geometry.get("type").equals("Polygon")){
            final Element polygon = new Element("Polygon");
            parsePolygon(coordinates, polygon);
            placemark.addContent(polygon);
        } else {
            final Element multiGeometry = new Element("MultiGeometry");
            for(int i = 0; i < coordinates.size(); ++i){
                Element polygon = new Element("Polygon");
                parsePolygon((JSONArray) coordinates.get(i), polygon);
                multiGeometry.addContent(polygon);
            }
            placemark.addContent(multiGeometry);
        }

        return placemark;
    }

    private static void parsePolygon(JSONArray coordinates, Element polygon) {
        int nbCoords = 0;
        for(int i = 0; i < coordinates.size(); ++i){
            nbCoords += ((JSONArray)coordinates.get(i)).size();

            final Element linearRing = new Element("LinearRing");
            final Element coords = new Element("coordinates");
            if(i == 0){
                final Element outerBoundaryIs = new Element("outerBoundaryIs");

                ((JSONArray)coordinates.get(0)).forEach(coordinate -> {
                    coords.setText(coords.getText() + ((JSONArray)coordinate).get(0) + "," + ((JSONArray)coordinate).get(1) + " ");
                });
                coords.setText(coords.getText().substring(0, coords.getText().length() - 1));
                polygon.addContent(outerBoundaryIs.addContent(linearRing.addContent(coords)));
            } else{
                final Element innerBoundaryIs = new Element("innerBoundaryIs");

                ((JSONArray)coordinates.get(i)).forEach(coordinate -> {
                    coords.setText(coords.getText() + ((JSONArray)coordinate).get(0) + "," + ((JSONArray)coordinate).get(1) + " ");
                });
                coords.setText(coords.getText().substring(0, coords.getText().length() - 1));
                polygon.addContent(innerBoundaryIs.addContent(linearRing.addContent(coords)));
            }
        }

        System.out.println("\t  - " + nbCoords + " coordinates");
    }
}
