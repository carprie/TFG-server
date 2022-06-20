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

    @GetMapping("/datosRegPIB/{index}")
    public ArrayList<DataPIB> getdatosRegPIB(@PathVariable("index") String index) throws IOException {
        double[] indexValue = new double[12];
        index = index.replaceAll(":", ".");
        for (int i = 0; i < 12; i++) {
            indexValue[i] = Double.parseDouble(index.split("-")[i]);
            System.out.println(indexValue[i]);
        }
        if (getCount(indexValue) == 0) {
            int indexPIB = (int) getIndex(indexValue);
            ArrayList<DataPIB> dataPIB = new ArrayList<DataPIB>();
            DataPIB dataPIB1 = new DataPIB(fileLines.get(indexPIB));
            dataPIB.add(dataPIB1);
            return dataPIB;
        } else {
            double[] datPIB = dataRegression(indexValue, getCount(indexValue))[0];
            ArrayList<DataPIB> dataPIB = new ArrayList<DataPIB>();
            DataPIB dataPIB1 = new DataPIB(datPIB);
            dataPIB.add(dataPIB1);
            return dataPIB;
        }
    }

    @GetMapping("/datosRegTemp/{index}")
    public ArrayList<DataTemp> getdatosRegTemp(@PathVariable("index") String index) throws IOException {
        System.out.println("entra en el back");
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

            double[] datTemp = dataRegression(indexValue, getCount(indexValue))[1];
            System.out.println("datTemp" + datTemp);
            ArrayList<DataTemp> dataTemp = new ArrayList<DataTemp>();
            DataTemp dataTemp1 = new DataTemp(datTemp);
            dataTemp.add(dataTemp1);
            return dataTemp;

        }
    }

    public double[][] dataRegression(double[] indexValue, double count) {
        double[] vUpper = new double[indexValue.length];
        double[] vLower = new double[indexValue.length];
        double[] regPIB = new double[103];
        double[] regTemp = new double[103];
        double[] indexCopia = indexValue;

        for (int i = 0; i < indexValue.length; i++) {
            double a = indexCopia[i];
            double b = indexCopia[i];
            vLower[i] = Math.floor(a);
            vUpper[i] = Math.ceil(b);

        }

        int indexUpper = (int) getIndex(vUpper);
        int indexLower = (int) getIndex(vLower);
        System.out.println("index upp  " + indexUpper);
        System.out.println("index low  " + indexLower);
        String[] dataUpper = fileLines.get(indexUpper).split(",");
        String[] dataLower = fileLines.get(indexLower).split(",");

        double[] dataVUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 12, 99));
        double[] dataPIBUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 206, dataUpper.length - 1));
        double[] dataTempUpper = toArrayDouble(Arrays.copyOfRange(dataUpper, 100, 205));

        double[] dataVLower = toArrayDouble(Arrays.copyOfRange(dataLower, 12, 99));
        double[] dataPIBLower = toArrayDouble(Arrays.copyOfRange(dataLower, 206, dataLower.length - 1));
        double[] dataTempLower = toArrayDouble(Arrays.copyOfRange(dataLower, 100, 205));

        double lambda = calculateLambda(dataVUpper, dataVLower, indexValue, count);
        for (int i = 0; i < dataPIBLower.length - 3; i++) {
            regPIB[i] = regression(dataPIBUpper[i], dataPIBLower[i], lambda);
            regTemp[i] = regression(dataTempUpper[i], dataTempLower[i], lambda);
        }
        double[][] r = new double[2][];
        r[0] = regPIB;
        r[1] = regTemp;
        return r;
    }

    private double regression(double d, double e, double lambda) {
        double n = (Math.abs(d - e)) * lambda + Math.min(d, e);
        if (n < 0) {
            return 0;
        } else
            return n;
    }

    private double calculateLambda(double[] dataVUpper, double[] dataVLower, double[] indexValue, double count) {
        double lambda = 0;
        double vH1, vH2, vH3, vM1, vM2, vM3, vM4, vM5, vM6, vM7, vM8, vM9;
        vH1 = vH2 = vH3 = vM1 = vM2 = vM3 = vM4 = vM5 = vM6 = vM7 = vM8 = vM9 = 0;

        // H1
        if (indexValue[11] > 0 && indexValue[11] < 1) {
            for (int i = 0; i < 15; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vH1 = vH1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[11] + dataVLower[i];
                    vH1 = vH1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH1 = vH1 / 15;

        } else if (indexValue[11] > 1 && indexValue[11] < 2) {
            for (int i = 0; i < 15; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH1 = vH1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[11] - 1) + dataVLower[i];
                    vH1 = vH1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH1 = vH1 / 15;
        } else if (indexValue[11] == 0 || indexValue[11] == 1 || indexValue[11] == 2) {
            vH1 = 0;

        }

        // H2
        if (indexValue[10] > 0 && indexValue[11] < 1) {
            for (int i = 15; i < 31; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH2 = vH2 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[10] + dataVLower[i];
                    vH2 = vH2 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH2 = vH2 / 16;
        } else if (indexValue[10] > 1 && indexValue[10] < 2) {
            for (int i = 15; i < 31; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH2 = vH2 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[10] - 1) + dataVLower[i];
                    vH2 = vH2 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH2 = vH2 / 16;
        } else if (indexValue[10] == 0 || indexValue[10] == 1 || indexValue[10] == 2) {
            vH2 = 0;
        }

        // H3
        if (indexValue[9] > 0 && indexValue[9] < 1) {
            for (int i = 31; i < 34; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH3 = vH3 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[9] + dataVLower[i];
                    vH3 = vH3 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH3 = vH3 / 3;
        } else if (indexValue[9] > 1 && indexValue[9] < 2) {
            for (int i = 31; i < 34; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH3 = vH3 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[9] - 1) + dataVLower[i];
                    vH3 = vH3 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH3 = vH3 / 3;
        } else if (indexValue[9] > 2 && indexValue[9] < 3) {
            for (int i = 31; i < 34; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH3 = vH3 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[9] - 2) + dataVLower[i];
                    vH3 = vH3 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH3 = vH3 / 3;
        } else if (indexValue[9] > 3 && indexValue[9] < 4) {
            for (int i = 31; i < 34; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vH3 = vH3 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[9] - 3) + dataVLower[i];
                    vH3 = vH3 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vH3 = vH3 / 3;
        } else if (indexValue[9] == 0 || indexValue[9] == 1 || indexValue[9] == 2 || indexValue[9] == 3
                || indexValue[9] == 4) {
            vH3 = 0;
        }

        // M1
        if (indexValue[8] > 0 && indexValue[8] < 1) {
            for (int i = 34; i < 37; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vM1 = vM1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[8] + dataVLower[i];
                    vM1 = vM1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM1 = vM1 / 3;
        } else if (indexValue[8] > 1 && indexValue[8] < 2) {
            for (int i = 34; i < 37; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vM1 = vM1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[8] - 1) + dataVLower[i];
                    vM1 = vM1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM1 = vM1 / 3;
        } else if (indexValue[8] > 2 && indexValue[8] < 3) {
            for (int i = 34; i < 37; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vM1 = vM1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[8] - 2) + dataVLower[i];
                    vM1 = vM1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM1 = vM1 / 3;
        } else if (indexValue[8] > 3 && indexValue[8] < 4) {
            for (int i = 34; i < 37; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vM1 = vM1 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[8] - 3) + dataVLower[i];
                    vM1 = vM1 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM1 = vM1 / 3;
        } else if (indexValue[8] == 0 || indexValue[8] == 1 || indexValue[8] == 2 || indexValue[8] == 3
                || indexValue[8] == 4) {
            vM1 = 0;
        }

        // M2
        if (indexValue[7] > 0 && indexValue[7] < 1) {
            for (int i = 37; i < 41; i++) {

                if (dataVLower[i] == dataVUpper[i]) {
                    vM2 = vM2 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[7] + dataVLower[i];
                    vM2 = vM2 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM2 = vM2 / 4;

        } else if (indexValue[7] > 1 && indexValue[7] < 2) {
            for (int i = 37; i < 41; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM2 = vM2 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[7] - 1) + dataVLower[i];
                    vM2 = vM2 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM2 = vM2 / 4;
        } else if (indexValue[7] == 0 || indexValue[7] == 1 || indexValue[7] == 2) {
            vM2 = 0;
        }

        // M3
        if (indexValue[6] > 0 && indexValue[6] < 1) {
            if (dataVLower[41] == dataVUpper[41]) {
                vM3 = 0;
            } else {
                double v = (dataVUpper[41] - dataVLower[41]) * indexValue[6] + dataVLower[41];
                vM3 = ((v - dataVLower[3]) / (dataVUpper[3] - dataVLower[3]));
            }

        } else if (indexValue[6] > 1 && indexValue[6] < 2) {
            if (dataVLower[41] == dataVUpper[41]) {
                vM3 = vM3 + 0;
            } else {
                double v = (dataVUpper[41] - dataVLower[41]) * (indexValue[6] - 1) + dataVLower[41];
                vM3 = ((v - dataVLower[3]) / (dataVUpper[3] - dataVLower[3]));
            }
        } else if (indexValue[6] == 0 || indexValue[6] == 1 || indexValue[6] == 2) {
            vM3 = 0;
        }

        // M4
        if (indexValue[5] > 0 && indexValue[5] < 1) {

            for (int i = 42; i < 44; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM4 = vM4 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * indexValue[5] + dataVLower[i];
                    vM4 = vM4 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM4 = vM4 / 2;

        } else if (indexValue[5] > 1 && indexValue[5] < 2) {
            for (int i = 42; i < 44; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM4 = vM4 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[5] - 1) + dataVLower[i];
                    vM4 = vM4 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM4 = vM4 / 2;
        } else if (indexValue[5] == 0 || indexValue[5] == 1 || indexValue[5] == 2) {
            vM4 = 0;
        }

        // M5
        if (indexValue[4] > 0 && indexValue[4] < 1) {
            for (int i = 44; i < 59; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM5 = vM5 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[4]) + dataVLower[i];
                    vM5 = vM5 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM5 = vM5 / 15;

        } else if (indexValue[4] > 1 && indexValue[4] < 2) {
            for (int i = 44; i < 59; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM5 = vM5 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[4] - 1) + dataVLower[i];
                    vM5 = vM5 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM5 = vM5 / 15;
        } else if (indexValue[4] == 0 || indexValue[4] == 1 || indexValue[4] == 2) {
            vM5 = 0;
        }

        // M6
        if (indexValue[3] > 0 && indexValue[3] < 1) {
            for (int i = 59; i < 64; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM6 = vM6 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[3]) + dataVLower[i];
                    vM6 = vM6 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM6 = vM6 / 5;
        } else if (indexValue[3] > 1 && indexValue[3] < 2) {
            for (int i = 59; i < 64; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM6 = vM6 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[3] - 1) + dataVLower[i];
                    vM6 = vM6 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM6 = vM6 / 5;
        } else if (indexValue[3] == 0 || indexValue[3] == 1 || indexValue[3] == 2) {
            vM6 = 0;
        }

        // M7
        if (indexValue[2] > 0 && indexValue[2] < 1) {
            for (int i = 64; i < 83; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM7 = vM7 + 0;
                } else {
                    double v = Math.abs(dataVUpper[i] - dataVLower[i]) * (indexValue[2])
                            + Math.min(dataVUpper[i], dataVLower[i]);
                    vM7 = vM7
                            + ((v - Math.min(dataVUpper[i], dataVLower[i])) / Math.abs(dataVUpper[i] - dataVLower[i]));
                }
            }
            vM7 = vM7 / 19;
        } else if (indexValue[2] > 1 && indexValue[2] < 2) {
            for (int i = 64; i < 83; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM7 = vM7 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[2] - 1)
                            + Math.min(dataVUpper[i], dataVLower[i]);
                    vM7 = vM7
                            + ((v - Math.min(dataVUpper[i], dataVLower[i])) / Math.abs(dataVUpper[i] - dataVLower[i]));
                }
            }
            vM7 = vM7 / 19;
        } else if (indexValue[2] == 0 || indexValue[2] == 1 || indexValue[2] == 2) {
            vM7 = 0;
        }

        // M8
        if (indexValue[1] > 0 && indexValue[1] < 1) {
            for (int i = 83; i < 86; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM8 = vM8 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[1]) + dataVLower[i];
                    vM8 = vM8 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM8 = vM8 / 3;
        } else if (indexValue[1] > 1 && indexValue[1] < 2) {
            for (int i = 83; i < 86; i++) {
                if (dataVLower[i] == dataVUpper[i]) {
                    vM8 = vM8 + 0;
                } else {
                    double v = (dataVUpper[i] - dataVLower[i]) * (indexValue[1] - 1) + dataVLower[i];
                    vM8 = vM8 + ((v - dataVLower[i]) / (dataVUpper[i] - dataVLower[i]));
                }
            }
            vM8 = vM8 / 3;
        } else if (indexValue[1] == 0 || indexValue[1] == 1 || indexValue[1] == 2) {
            vM8 = 0;
        }

        // M9
        if (indexValue[0] > 0 && indexValue[0] < 1) {

            if (dataVLower[86] == dataVUpper[86]) {
                vM9 = 0;
            } else {
                double v = (dataVUpper[86] - dataVLower[86]) * (indexValue[0]) + dataVLower[86];
                vM9 = ((v - dataVLower[86]) / (dataVUpper[86] - dataVLower[86]));
            }

        } else if (indexValue[0] > 1 && indexValue[0] < 2) {
            if (dataVLower[86] == dataVUpper[86]) {
                vM9 = 0;
            } else {
                double v = (dataVUpper[86] - dataVLower[86]) * (indexValue[0] - 1) + dataVLower[86];
                vM9 = ((v - dataVLower[86]) / (dataVUpper[86] - dataVLower[86]));
            }
        } else if (indexValue[0] == 0 || indexValue[0] == 1 || indexValue[0] == 2) {
            vM9 = 0;
        }

        double alpha = 1 / count;
        lambda = alpha * (vH1 + vH2 + vH3 + vM1 + vM2 + vM3 + vM4 + vM5 + vM6 + vM7 + vM8 + vM9);
        System.out.println("lambda  " + lambda);
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
        System.out.println("indice  " + index);
        return index;
    }

}
