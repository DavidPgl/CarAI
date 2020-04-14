package s0564478;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ClassCloner {

    public static void CloneFile(String fileToClone, String destination, String oldFileName, String newFileName, String extension, String packageName) {
        File src = new File(fileToClone);
        new File(destination).mkdir();
        File dst = new File(destination + newFileName + extension);

        try {
            // Copy whole class
            Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);

            List<String> fileContent = new ArrayList<>(Files.readAllLines(dst.toPath(), StandardCharsets.UTF_8));

            // Set correct package
            fileContent.set(0, "package " + packageName + ";");
            fileContent.add(3, "import s0564478.CarAI;");

            Random random = new Random();

            // Find all class references and update them
            for (int i = 0; i < fileContent.size(); i++) {
                String line = fileContent.get(i);

                if (line.contains(oldFileName)) {
                    fileContent.set(i, line.replace(oldFileName, newFileName));
                }

                if (line.contains("goalRadius =") || line.contains("decelerateRadius =") ||
                        line.contains("throttleTime =") || line.contains("goalAngle =") ||
                        line.contains("decelerateAngle =") || line.contains("steerTime ="))
                    fileContent.set(i, line.replace("0;", random.nextFloat() + "f;"));
            }

            Files.isWritable(dst.toPath());
            Files.write(dst.toPath(), fileContent, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
