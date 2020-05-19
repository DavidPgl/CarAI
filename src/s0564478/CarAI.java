package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import s0564478.behaviour.BehaviourStats;
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
    private List<Point> currentRoute = null;
    private Point currentRoutePoint = null;

    public CarAI(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, this);
        steeringBehaviour = new SteeringBehaviour(info, this);

        levelGraph = new LevelGraph(info.getTrack().getObstacles());

        addDebugAction(() -> {
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() - 5, point.getY() - 5, point.getX() + 5, point.getY() + 5, Color.RED));
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() + 5, point.getY() - 5, point.getX() - 5, point.getY() + 5, Color.RED));
            //levelGraph.getVertices().forEach(vertex -> vertex.getEdges().forEach(edge ->
            //       GLUtil.drawLine(vertex.getData(), edge.getTo().getData(), Color.BLACK)));
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
        //Check if we reached the current checkpoint by checking the world's current checkpoint.
        if (!info.getCurrentCheckpoint().equals(currentCheckpoint))
            updateNextCheckpoint();


        // Check if current routePoint is in reach.
        if (currentRoutePoint == null || currentRoutePoint.distance(info.getX(), info.getY()) < BehaviourStats.ROUTE_POINT_GOAL_DISTANCE)
            // Check if next routePoint is current checkpoint.
            if (currentRoute.size() > 1)
                currentRoutePoint = currentRoute.remove(0);
            else
                currentRoutePoint = currentRoute.get(0);

        float throttle = throttleBehaviour.getThrottle(currentRoutePoint);
        System.out.println(throttle);
        return new DriverAction(throttle, steeringBehaviour.getSteering(currentRoutePoint));
    }

    /**
     * Updates the current checkpoint.
     * Adds new checkpoint and current car position to the graph and calculates a new route
     */
    private void updateNextCheckpoint() {
        Point carPosition = new Point((int) info.getX(), (int) info.getY());
        currentCheckpoint = new Point(info.getCurrentCheckpoint());
        levelGraph.updateCarAndCP(carPosition, currentCheckpoint);

        currentRoute = levelGraph.getPath(carPosition, currentCheckpoint);
        for (int i = 0; i < currentRoute.size() - 1; i += 2) {
            Point a = currentRoute.get(i);
            Point b = currentRoute.get(i + 1);
            Point inBetween = new Point(a.x / 2 + b.x / 2, a.y / 2 + b.y / 2);
            currentRoute.add(i + 1, inBetween);
            addDebugAction(() -> GLUtil.drawLine(a, b, Color.BLACK));
            addDebugAction(() -> GLUtil.drawLine(inBetween.getX() - 5, inBetween.getY() - 5, inBetween.getX() + 5, inBetween.getY() + 5, Color.MAGENTA));
        }
        currentRoute.remove(0);
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
