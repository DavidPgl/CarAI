package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;
import s0564478.util.GLUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CarAI extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;

    private final List<Runnable> debugActions = new ArrayList<>();

    public CarAI(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, this);
        steeringBehaviour = new SteeringBehaviour(info, this);

        LevelGraph levelGraph = new LevelGraph(info.getTrack().getObstacles());

        addDebugAction(() -> {
            levelGraph.getConvexPoints().forEach(point -> GLUtil.drawLine(point.getX() - 5, point.getY() - 5, point.getX() + 5, point.getY() + 5, Color.BLACK));
            levelGraph.getConvexPoints().forEach(point -> GLUtil.drawLine(point.getX() + 5, point.getY() - 5, point.getX() - 5, point.getY() + 5, Color.BLACK));
        });
        System.out.println(levelGraph.getConvexPoints());

        enlistForTournament(564478, 562886);

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
