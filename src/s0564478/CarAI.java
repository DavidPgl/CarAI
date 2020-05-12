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
    private final LevelGraph levelGraph;
    private Point currentCheckpoint = null;

    public CarAI(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, this);
        steeringBehaviour = new SteeringBehaviour(info, this);

        levelGraph = new LevelGraph(info.getTrack().getObstacles());

        addDebugAction(() -> {
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() - 5, point.getY() - 5, point.getX() + 5, point.getY() + 5, Color.RED));
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() + 5, point.getY() - 5, point.getX() - 5, point.getY() + 5, Color.RED));
            levelGraph.getVertices().forEach(vertex -> vertex.getEdges().forEach(edge ->
                    GLUtil.drawLine(vertex.getData(), edge.getTo().getData(), Color.BLACK)));
        });
        System.out.println(levelGraph.getVertices().stream().map(v -> v.getEdges().size()).reduce(0, Integer::sum));
        enlistForTournament(564478, 562886);
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        if (!info.getCurrentCheckpoint().equals(currentCheckpoint)) {
            currentCheckpoint = new Point(info.getCurrentCheckpoint());
            levelGraph.updateCarAndCP(new Point((int) info.getX(), (int) info.getY()), currentCheckpoint);
        }

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
