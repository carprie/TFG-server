package proyecto.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import proyecto.model.DataPIB;
import proyecto.model.DataTemp;
import proyecto.utils.FileUtils;

@RestController
public class Controller {

    static List<String> fileLines = getData();

    public static List<String> getData() {
        List<String> fileLinesData = null;
        try {
            fileLinesData = FileUtils.getFileLines();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLinesData;
    }

    @GetMapping("/datosPIB/{index}")
    public ArrayList<DataPIB> getdatosPIB(@PathVariable("index") Integer index) throws IOException {
        ArrayList<DataPIB> dataPIB = new ArrayList<DataPIB>();
        DataPIB dataPIB1 = new DataPIB(fileLines.get(index));
        dataPIB.add(dataPIB1);
        // System.out.println(fileLines.get(0).split(","));
        return dataPIB;
    }

    @GetMapping("/datosTemp/{index}")
    public ArrayList<DataTemp> getdatosTemp(@PathVariable("index") Integer index) throws IOException {
        ArrayList<DataTemp> dataTemp = new ArrayList<DataTemp>();
        DataTemp dataTemp1 = new DataTemp(fileLines.get(index));
        dataTemp.add(dataTemp1);
        // System.out.println(fileLines.get(0).split(","));
        return dataTemp;
    }

    @GetMapping("/datosExtrPIB/{index}")
    public ArrayList<DataPIB> getdatosExtrPIB(@PathVariable("index") String index) throws IOException {
        double[] indexValue = new double[12];
        index = index.replaceAll(":", ".");
        for (int i = 0; i < 12; i++) {
            indexValue[i] = Double.parseDouble(index.split("-")[i]);
        }
        if (getCount(indexValue) == 0) {
            int indexPIB = (int) getIndex(indexValue);
            ArrayList<DataPIB> dataPIB = new ArrayList<DataPIB>();
            DataPIB dataPIB1 = new DataPIB(fileLines.get(indexPIB));
            dataPIB.add(dataPIB1);
            return dataPIB;
        } else {
            double[] datPIB = dataExtrapolation(indexValue, getCount(indexValue))[0];
            ArrayList<DataPIB> dataPIB = new ArrayList<DataPIB>();
            DataPIB dataPIB1 = new DataPIB(datPIB);
            dataPIB.add(dataPIB1);
            return dataPIB;
        }
    }

    @GetMapping("/datosExtrTemp/{index}")
    public ArrayList<DataTemp> getdatosExtrTemp(@PathVariable("index") String index) throws IOException {
        double[] indexValue = new double[12];
        index = index.replaceAll(":", ".");
        for (int i = 0; i < 12; i++) {
            indexValue[i] = Double.parseDouble(index.split("-")[i]);
        }
        if (getCount(indexValue) == 0) {
            int indexTemp = (int) getIndex(indexValue);
            ArrayList<DataTemp> dataTemp = new ArrayList<DataTemp>();
            DataTemp dataTemp1 = new DataTemp(fileLines.get(indexTemp));
            dataTemp.add(dataTemp1);

            return dataTemp;

        } else {

            double[] datTemp = dataExtrapolation(indexValue, getCount(indexValue))[1];
            ArrayList<DataTemp> dataTemp = new ArrayList<DataTemp>();
            DataTemp dataTemp1 = new DataTemp(datTemp);
            dataTemp.add(dataTemp1);
            return dataTemp;

        }
    }

    public double[][] dataExtrapolation(double[] indexValue, double count) {
        double[] vUpper = new double[indexValue.length];
        double[] vLower = new double[indexValue.length];
        double[] extrPIB = new double[103];
        double[] extrTemp = new double[103];
        double[] indexCopia = indexValue;

        for (int i = 0; i < indexValue.length; i++) {
            double a = indexCopia[i];
            double b = indexCopia[i];
            vLower[i] = Math.floor(a);
            vUpper[i] = Math.ceil(b);

        }

        int indexUpper = (int) getIndex(vUpper);
        int indexLower = (int) getIndex(vLower);
        String[] dataUpper = fileLines.get(indexUpper).split(",");
        String[] dataLower = fileLines.get(indexLower).split(",");

        // double[] dataVUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 12, 99));
        double[] dataPIBUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 206, dataUpper.length - 1));
        double[] dataTempUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 100, 205));

        // double[] dataVLower = toArrayDouble(Arrays.copyOfRange(dataLower, 12, 99));
        double[] dataPIBLower = toArrayDouble(Arrays.copyOfRange(dataLower, 206, dataLower.length - 1));
        double[] dataTempLower = toArrayDouble(Arrays.copyOfRange(dataLower, 100, 205));

        double lambda = calculateLambda(vUpper, vLower, indexValue, count);
        for (int i = 0; i < dataPIBLower.length - 3; i++) {
            extrPIB[i] = extrapolation(dataPIBUpper[i], dataPIBLower[i], lambda);
            extrTemp[i] = extrapolation(dataTempUpper[i], dataTempLower[i], lambda);
        }
        double[][] r = new double[2][];
        r[0] = extrPIB;
        r[1] = extrTemp;
        return r;
    }

    private double extrapolation(double d, double e, double lambda) {
        double n = (Math.abs(d - e)) * lambda + Math.min(d, e);
        if (n < 0) {
            return 0;
        } else
            return n;
    }

    private double calculateLambda(double[] vUpper, double[] vLower, double[] indexValue, double count) {
        double lambda = 0;
        double vH = 0;

        for (int i = 0; i < indexValue.length; i++) {

            if (vLower != vUpper) {
                if (indexValue[i] % 1 != 0) {
                    vH = vH + (indexValue[i] - vLower[i]) / (vUpper[i] - vLower[i]);
                }
            }
        }

        double alpha = 1 / count;
        lambda = alpha * vH;
        return lambda;

    }

    private double[] toArrayDouble(String[] array) {
        double[] dArray = new double[array.length];
        for (int i = 0; i < array.length; i++) {
            dArray[i] = Float.parseFloat(array[i]);
        }
        return dArray;
    }

    public int getCount(double[] value) {
        int count = 0;
        for (int i = 0; i < value.length; i++) {
            if (value[i] % 1 != 0) {
                count = count + 1;
            }
        }
        return count;
    }

    public double getIndex(double[] indexValue) {
        double index = 0;

        for (int i = 0; i < indexValue.length; i++) {
            index = index + (Math.pow(5, i) * indexValue[i]);
        }
        // Correccion M9

        double factorM9 = Math.floor(index / 5) * 2;

        // Correccion M8
        double factorM8 = Math.floor(index / 25) * 3 * 3;

        // Correccion M7
        double factorM7 = Math.floor(index / 125) * 6 * 2;

        // Correccion M6
        double factorM6 = Math.floor(index / 625) * 18 * 3;

        // Correccion M5
        double factorM5 = Math.floor(index / 3125) * 36 * 2;

        // Correccion M4
        double factorM4 = Math.floor(index / 15625) * 108 * 2;

        // Correccion M3
        double factorM3 = Math.floor(index / 78125) * 324 * 2;

        // Correccion M2
        double factorM2 = Math.floor(index / 390625) * 972 * 3;

        // Correccion H2
        double factorH2 = Math.floor(index / 48828125) * 48600 * 2;

        // Correccion H1
        double factorH1 = Math.floor(index / 244140625) * 145800 * 2;

        /*
         * // Correccion M8
         * double factorM8 = Math.floor(index / 9);// * 3;
         * 
         * // Correccion M6
         * double factorM6 = Math.floor(index / 81);// * 18;
         * 
         * // Correccion M2
         * double factorM2 = Math.floor(index / 6561);// * 972;
         */

        index = index + 1 - factorM9 - factorM8 - factorM7 - factorM6 - factorM5 - factorM4 - factorM3 - factorM2
                - factorH2
                - factorH1;
        return index;
    }

}
