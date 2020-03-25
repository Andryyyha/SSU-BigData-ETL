
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.kinesis.common.KinesisClientUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;


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
        return data;
    }

    public static void writeData(List<String> data) {
        KinesisAsyncClient kinesisClient = KinesisClientUtil
                .createKinesisAsyncClient(KinesisAsyncClient
                        .builder()
                        .region(Region.US_EAST_2));

        for (int i = 0; i < data.size(); i++) {
            String current = data.get(i);
            byte[] bytes = current.getBytes();
            if (bytes == null) {
                LOG.warn("Could not get bytes for record");
                System.out.println("Could not get bytes for record");
                return;
            }

            LOG.info("Putting record: " + current);
            System.out.println("Could not get bytes for record");
            PutRecordRequest request = PutRecordRequest.builder()
                    .partitionKey("air") // We use the ticker symbol as the partition key, explained in the Supplemental Information section below.
                    .streamName("air_pollution")
                    .data(SdkBytes.fromByteArray(bytes))
                    .build();
            try {
                kinesisClient.putRecord(request).get();
            } catch (InterruptedException e) {
                LOG.info("Interrupted, assuming shutdown.");
                System.out.println("Interrupted, assuming shutdown.");
            } catch (ExecutionException e) {
                LOG.error("Exception while sending data to Kinesis. Will try again next cycle.", e);
                System.out.println("Exception while sending data to Kinesis. Will try again next cycle.");
            }
//        KinesisProducer kinesis = new KinesisProducer();
//        for (int i = 0; i < data.size(); ++i) {
//            ByteBuffer row = null;
//            try {
//                row = ByteBuffer.wrap(data.get(i).getBytes("UTF-8"));
//            } catch (UnsupportedEncodingException ex) {
//                ex.printStackTrace();
//            }
//            kinesis.addUserRecord("air_pollution", "air", row);
//        }
        }
    }
}
