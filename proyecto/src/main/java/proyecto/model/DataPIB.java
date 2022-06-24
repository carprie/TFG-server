package proyecto.model;

import java.util.ArrayList;
import java.util.List;

public class DataPIB {

    public String name;
    public List<Series> series;

    public DataPIB(String data) {
        name = "PIB";
        series = new ArrayList<Series>();
        String[] dataArray = data.split(",");
        int y = 2000;
        for (int i = 206; i < dataArray.length - 5; i++) {
            series.add(new Series(Integer.toString(y), Float.parseFloat(dataArray[i])));
            y++;
        }
        ;
    }

    public DataPIB(double[] data) {
        name = "PIB";
        series = new ArrayList<Series>();
        int y = 2000;
        for (int i = 0; i < data.length - 1; i++) {
            series.add(new Series(Integer.toString(y), (float) data[i]));
            y++;
        }
        ;
    }
}
