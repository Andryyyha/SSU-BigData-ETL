
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisAsyncClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class DataGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(DataGenerator.class);

    public static List<String> generateData(String path) {
        List<String> data = new ArrayList<>();
        Path pathToFile = Paths.get(path);
        try (BufferedReader br = Files.newBufferedReader(pathToFile)) {
            String line = br.readLine();
            while (line != null) {
                data.add(line);
                line = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        data.remove(0);
        return data;
    }

    private static void writeData(List<String> data, String key, String streamName) {
        AmazonKinesis kinesisClient = AmazonKinesisAsyncClientBuilder.defaultClient();

        for (int i = 0; i < data.size(); i += 500) {

            System.out.println("putting from: " + i);
            PutRecordsRequest request = new PutRecordsRequest().withStreamName(streamName);
            List<PutRecordsRequestEntry> entries = new ArrayList<>();
            for (int j = i; j < i + 500 && j < data.size(); j++) {
                String current = data.get(j);
                byte[] bytes = current.getBytes();
                entries.add(new PutRecordsRequestEntry().withData(ByteBuffer.wrap(bytes)).withPartitionKey(key + i));
            }

            request.setRecords(entries);
            kinesisClient.putRecords(request);
        }
    }

    public static void writeDataAir(List<String> data) {
        writeData(data, "air", "air_pollution");
    }

    public static void writeDataStations(List<String> data) {
        writeData(data, "station", "stations");
    }
}
