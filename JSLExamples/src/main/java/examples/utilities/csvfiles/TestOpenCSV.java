package examples.utilities.csvfiles;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class TestOpenCSV {

    public static void main(String[] args) throws FileNotFoundException {

        //the IDE warnings (yellow) can be ignored since we know the type
        List<Data> dataList = new CsvToBeanBuilder(new FileReader("examples/data.csv"))
                .withType(Data.class).build().parse();

        List<Double> x = new ArrayList<>();
        x.add(1.0);

        for(double b: x){
            System.out.println(b);
        }
        Data data = dataList.get(0);
        System.out.println(data.getX());
        System.out.println(data);

        for(Data d: dataList){
            System.out.println(d);
        }
    }
}
