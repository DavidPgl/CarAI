package s0564478;

import lenz.htw.ai4g.ai.Info;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class RandomCar extends CarAI {
    public RandomCar(Info info) {
        super(info);

        goalRadius = 10 * 0;
        decelerateRadius = goalRadius + 50 * 0;
        throttleTime = 0.1f + 5 * 0;

        goalAngle = 10 * 0;
        decelerateAngle = goalRadius + 135 * 0;
        steerTime = 0.1f + 5 * 0;

        try {
            File values = new File("values.txt");
            String data = this.getClass().getSimpleName() + ":  " + String.format("%.1f | %.1f | %.1f | %.1f | %.1f | %.1f", goalRadius, decelerateRadius, throttleTime, goalAngle, decelerateAngle, steerTime) + "\n";
            Files.write(values.toPath(), data.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }

        enlistForTournament(564478, 562886);
    }
}
