import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        List<String> madridYears = new ArrayList<>();
        for (int i = 2001; i <= 2001; i++) {
            madridYears.add(String.valueOf(i));
        }
        for (String madridYear : madridYears) {
            String fileName = "air-quality-madrid/csvs_per_year/csvs_per_year/madrid_" + madridYear + ".csv";
            List<String> data = DataGenerator.generateData(fileName);
            DataGenerator.writeDataAir(data);
        }
        List<String> data = DataGenerator.generateData("air-quality-madrid/stations.csv");
        DataGenerator.writeDataStations(data);
    }
}
