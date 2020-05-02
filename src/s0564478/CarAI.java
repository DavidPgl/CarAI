package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;
import s0564478.testUtil.RandomExtender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CarAI extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;

    private final List<Runnable> debugActions = new ArrayList<>();

    public CarAI(Info info) {
        super(info);

        RandomExtender r = new RandomExtender();
        float goalRadius = r.between(0.1f, 10);
        throttleBehaviour = new ThrottleBehaviour(info, goalRadius, goalRadius + r.between(0, 50), r.between(0.1f, 5));
        float goalAngle = r.between(0.1f, 10);
        steeringBehaviour = new SteeringBehaviour(info, goalAngle, goalAngle + r.between(0, 135), r.between(0.1f, 5), r.between(20, 100));

        enlistForTournament(564478, 562886);

        try {
            File values = new File(System.getProperty("user.dir") + "/values.txt");
            String carValues = String.format(getClass().getSimpleName() + ": %.2f | %.2f | %.2f | %.2f | %.2f | %.2f | %.2f\n",
                    throttleBehaviour.getGoalRadius(), throttleBehaviour.getDecelerateRadius(), throttleBehaviour.getThrottleTime(),
                    steeringBehaviour.getGoalAngle(), steeringBehaviour.getDecelerateAngle(), steeringBehaviour.getSteerTime(), steeringBehaviour.getCollisionAvoidanceRadius());

            Files.write(values.toPath(), carValues.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        return new DriverAction(throttleBehaviour.getThrottle(), steeringBehaviour.getSteering());
    }

    @Override
    public String getTextureResourceName() {
        return "/s0564478/art/car.png";
    }

    @Override
    public void doDebugStuff() {
        debugActions.forEach(Runnable::run);
    }

    public void addDebugAction(Runnable action) {
        debugActions.add(action);
    }
}
