package proyecto.model;

import java.util.ArrayList;
import java.util.List;

public class DataTemp {
    public String name;
    public List<Series> series;

    public DataTemp(String data) {
        name = "Temperatura";
        series = new ArrayList<Series>();
        String[] dataArray = data.split(",");
        int y = 2000;
        for (int i = 100; i < 202; i++) {
            series.add(new Series(Integer.toString(y), Float.parseFloat(dataArray[i])));
            y++;
        }
        ;
    }

    public DataTemp(double[] data) {

        name = "Temperatura";
        series = new ArrayList<Series>();
        int y = 2000;
        for (int i = 0; i < data.length - 1; i++) {
            series.add(new Series(Integer.toString(y), (float) data[i]));
            y++;
        }
        ;
    }

}
