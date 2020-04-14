package s0564478;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Timestamp {

    public static void main(String[] args) {
        // Add timestamp to file
        try {
            File values = new File(System.getProperty("user.dir") + "/values.txt");
            String timeStamp = "\n" + "\n" + "\n" + new java.sql.Timestamp(System.currentTimeMillis()) + "\n";
            Files.write(values.toPath(), timeStamp.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
