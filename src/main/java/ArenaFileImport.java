import com.opencsv.*;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

public class ArenaFileImport {

    public static void main(String[] args) {

    }

    /**
     *
     * @param inputFilePath the path to the tally based observations csv file
     * @param outputFilePath the path to the transformed output as a csv file
     */
    public static void importArenaTallyObservations(Path inputFilePath, Path outputFilePath) throws IOException, CsvValidationException {
        Objects.requireNonNull(inputFilePath, "The path to the input file was null!");
        Objects.requireNonNull(outputFilePath, "The path to the output file was null!");
        int r = 1; // the replication
        int i = 0; // counts observations within a replication

        FileWriter writer = new FileWriter(outputFilePath.toFile());
        CSVWriter write = new CSVWriter(writer);

        final CSVParser parser = new CSVParserBuilder()
                        .withSeparator(',')
                        .withQuoteChar('"')
                        .withIgnoreQuotations(true)
                        .build();

        final CSVReader reader = new CSVReaderBuilder(new FileReader(inputFilePath.toFile()))
                        .withSkipLines(7)
                        .withCSVParser(parser)
                        .build();

        //Read CSV line by line and use the string array as you want
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (nextLine[0].startsWith("-")) {
                i = 0;
                r = r + 1;
            }
            else {
                i = i + 1;
                String[] data = new String[4];
                data[0] = nextLine[0]; // time of the observation
                data[1] = nextLine[1]; // the observation
                data[2] = Integer.toString(i); // observation number within the replication
                data[3] = Integer.toString(r); // replication number
                write.writeNext(data);
            }
        }
        write.flush();
        write.close();
        writer.close();
        reader.close();
    }


}
