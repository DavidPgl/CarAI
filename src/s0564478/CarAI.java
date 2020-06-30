package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.behaviour.BehaviourStats;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;
import s0564478.graph.Edge;
import s0564478.graph.Vertex;
import s0564478.navigation.LevelGraph;
import s0564478.navigation.LevelPoint;
import s0564478.navigation.LevelZone;
import s0564478.util.GLUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CarAI extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;
    private final List<Runnable> debugActions = new ArrayList<>();
    private final LevelGraph levelGraph;
    private LevelPoint currentCheckpoint = null;
    private List<LevelPoint> currentRoute = null;
    private LevelPoint currentRoutePoint = null;

    public CarAI(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, this);
        steeringBehaviour = new SteeringBehaviour(info, this);

        levelGraph = new LevelGraph(info.getTrack(), this);

        addDebugAction(() -> {
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() - 5, point.getY() - 5, point.getX() + 5, point.getY() + 5,
                    point.levelZone == null ? Color.RED : point.levelZone.zoneType == LevelZone.ZoneType.FAST_ZONE ? Color.GREEN : point.levelZone.zoneType == LevelZone.ZoneType.SLOW_ZONE ? Color.BLUE : Color.PINK));
            levelGraph.getVerticesPoints().forEach(point -> GLUtil.drawLine(point.getX() + 5, point.getY() - 5, point.getX() - 5, point.getY() + 5,
                    point.levelZone == null ? Color.RED : point.levelZone.zoneType == LevelZone.ZoneType.FAST_ZONE ? Color.GREEN : Color.BLUE));
        });
        System.out.println(levelGraph.getVertices().stream().map(v -> v.getEdges().size()).reduce(0, Integer::sum));
        enlistForTournament(564478, 562886);
    }

    @Override
    public String getName() {
        return "DriftKing";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        if (wasResetAfterCollision)
            updatePath();

        //Check if we reached the current checkpoint by checking the world's current checkpoint.
        if (!info.getCurrentCheckpoint().equals(currentCheckpoint))
            updateNextCheckpoint();

        if (currentRoutePoint == null)
            currentRoutePoint = currentRoute.remove(0);

        final double currentDistance = currentRoutePoint.distance(getCarPosition());
        final float goalDistance = currentRoutePoint.type == LevelPoint.Type.STEP_POINT ?
                BehaviourStats.StepPoint.GOAL_DISTANCE : BehaviourStats.RoutePoint.GOAL_DISTANCE;

        if (currentRoutePoint.type != LevelPoint.Type.CHECKPOINT && currentDistance < goalDistance)
            currentRoutePoint = currentRoute.remove(0);

        float throttle = throttleBehaviour.getThrottle(currentRoutePoint);
        return new DriverAction(throttle, steeringBehaviour.getSteering(currentRoutePoint));
    }

    public LevelPoint getCarPosition() {
        return new LevelPoint((int) info.getX(), (int) info.getY(), LevelPoint.Type.CHECKPOINT);
    }

    public Vector2f getCarDirection() {
        float orientation = info.getOrientation();
        return new Vector2f((float) Math.cos(orientation), (float) Math.sin(orientation));
    }

    public float getSignedVelocity() {
        Vector2f velocity = info.getVelocity();
        return velocity.length() * (Vector2f.angle(getCarDirection(), velocity) < Math.PI / 2 ? 1 : -1);
    }

    /**
     * Updates the current checkpoint.
     * Adds new checkpoint and current car position to the graph and calculates a new route
     */
    private void updateNextCheckpoint() {
        currentCheckpoint = new LevelPoint(info.getCurrentCheckpoint(), LevelPoint.Type.CHECKPOINT);
        levelGraph.updateCarAndCP(getCarPosition(), currentCheckpoint);
        updatePath();
    }

    private void updatePath() {
        currentRoutePoint = null;
        currentRoute = levelGraph.getPath(getCarPosition(), currentCheckpoint);
        for (int i = 0; i < currentRoute.size() - 1; i += 2) {
            Point a = currentRoute.get(i);
            Point b = currentRoute.get(i + 1);
            LevelPoint inBetween = new LevelPoint(a.x / 2 + b.x / 2, a.y / 2 + b.y / 2, LevelPoint.Type.STEP_POINT);
            currentRoute.add(i + 1, inBetween);
            addDebugAction(() -> GLUtil.drawLine(a, b, Color.BLACK));
            addDebugAction(() -> GLUtil.drawLine(inBetween.getX() - 5, inBetween.getY() - 5, inBetween.getX() + 5, inBetween.getY() + 5, Color.MAGENTA));
        }
        //addDebugAction(() -> levelGraph.getVertexFromPoint(getCarPosition()).getEdges().forEach(edge -> GLUtil.drawLine(getCarPosition(), edge.getTo().getData(), Color.BLACK)));

        currentRoute.remove(0);

        addDebugAction(() -> {
            Vertex<LevelPoint> v = levelGraph.getVertexFromPoint(currentRoutePoint);
            if (v == null)
                return;
            v.getEdges().forEach(edge -> {
                double max = v.getEdges().stream().map(Edge::getWeight).max(Double::compareTo).get();
                double colorDelta = edge.getWeight() / max;
                Color color;
                if (colorDelta > 0.8) {
                    color = Color.RED;
                } else if (colorDelta > 0.6) {
                    color = Color.PINK;
                } else if (colorDelta > 0.4) {
                    color = Color.YELLOW;
                } else if (colorDelta > 0.2) {
                    color = Color.CYAN;
                } else
                    color = Color.GREEN;
//                Color color = edge.getWeight() == 9999 ? Color.RED : Color.GREEN;

                GLUtil.drawLine(v.getData(), edge.getTo().getData(), color);
            });
        });
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
