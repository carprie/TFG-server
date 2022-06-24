package proyecto.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileUtils {

    static RandomAccessFile fichero;
    List<String> fileLines;

    public static List<String> getFileLines() throws IOException {

        return Files.readAllLines(Paths.get("..//..//..//datos_unidos.csv"));
    }

}
