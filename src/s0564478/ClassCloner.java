package s0564478;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class ClassCloner {

    public static void CloneFile(String fileToClone, String destination, String oldFileName, String newFileName, String extension, String packageName){
        File src = new File(fileToClone);
        new File (destination).mkdir();
        File dst = new File(destination + newFileName + extension);



        try {
            // Copy whole class
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);

            List<String> fileContent = new ArrayList<>(Files.readAllLines(dst.toPath(), StandardCharsets.UTF_8));

            // Set correct package
            fileContent.set(0, "package " + packageName + ";");

            // Find all class references and update them
            for (int i = 0; i < fileContent.size(); i++) {
                if (fileContent.get(i).contains(oldFileName)) {
                    fileContent.set(i, fileContent.get(i).replace(oldFileName, newFileName));
                }
            }

            Files.isWritable(dst.toPath());
            Files.write(dst.toPath(), fileContent, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
