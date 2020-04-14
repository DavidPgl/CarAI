package s0564478;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.List;

public class ClassGenerator {

    private static final int numberOfClones = 2000;

    public static void main(String[] args) {

        for (int i = 2; i < numberOfClones + 2; i++) {
            String newFileName = "Car" + i;

            ClassCloner.CloneFile(
                    "src/s0564478/RandomCar.java",
                    "src/s0564478/Clones/",
                    "RandomCar",
                    newFileName,
                    ".java",
                    "s0564478.Clones");
        }



        // Add timestamp to file
        try {
            File values = new File("values.txt");
            String timeStamp = "\n" + "\n" + "\n" + new Timestamp(System.currentTimeMillis()) + "\n";
            Files.write(values.toPath(), timeStamp.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
