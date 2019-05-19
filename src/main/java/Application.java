
public class Application {
    static final String INPUT = "data/countries.geojson";
    static final String OUTPUT = "data/countries.kml";

    public static void main(String[] args) {
        Parser p = new Parser(INPUT, OUTPUT);
        p.parse();
    }
}
