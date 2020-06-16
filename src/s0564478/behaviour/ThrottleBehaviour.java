package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import org.lwjgl.util.vector.Vector2f;
import s0564478.CarAI;
import s0564478.navigation.LevelPoint;
import s0564478.navigation.LevelZone;

import java.awt.*;

public class ThrottleBehaviour {

    private final Info info;
    private final CarAI ai;
    private LevelPoint previousRoutePoint;
    private LevelPoint currentRoutePoint;

    public ThrottleBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;
        this.previousRoutePoint = new LevelPoint((int) info.getX(), (int) info.getY(), LevelPoint.Type.CHECKPOINT);
        this.currentRoutePoint = previousRoutePoint;
    }

    public float getThrottle(LevelPoint nextRoutePoint) {
        // Update points
        if (!nextRoutePoint.equals(currentRoutePoint)) {
            previousRoutePoint = currentRoutePoint;
            currentRoutePoint = nextRoutePoint;
        }

        if (currentRoutePoint.levelZone != null && currentRoutePoint.levelZone.zoneType == LevelZone.ZoneType.FAST_ZONE &&
                previousRoutePoint.levelZone == null)
            return info.getMaxAbsoluteAcceleration();

        if (!isPreviousPointOutOfRange())
            return getAngularThrottle(currentRoutePoint);

        double distance = currentRoutePoint.distance(info.getX(), info.getY());

        switch (currentRoutePoint.type) {
            case ROUTE_POINT:
                return getRoutePointThrottle(distance);
            case CHECKPOINT:
                return getCheckPointThrottle(distance, currentRoutePoint);
            default:
                return getAngularThrottle(currentRoutePoint);
        }
    }

    private float getAngularThrottle(LevelPoint nextRoutePoint) {
        Point carPosition = ai.getCarPosition();
        Vector2f vectorToPoint = new Vector2f(nextRoutePoint.x - carPosition.x, nextRoutePoint.y - carPosition.y);
        float angle = Vector2f.angle(ai.getCarDirection(), vectorToPoint);
        double factor = Math.pow((Math.PI - angle) / Math.PI, 10);
        double newSpeed = factor * info.getMaxVelocity();
        double speedDiff = newSpeed - ai.getSignedVelocity();
        speedDiff = Math.max(speedDiff, 0.0001d);
        return (float) (speedDiff / BehaviourStats.RoutePoint.THROTTLE_TIME);
    }

    private float getCheckPointThrottle(double distance, LevelPoint nextRoutePoint) {
        if (distance < BehaviourStats.Checkpoint.GOAL_DISTANCE)
            return 0;

        if (distance > BehaviourStats.Checkpoint.DECELERATE_DISTANCE)
            return info.getMaxAbsoluteAcceleration();

        Point carPosition = ai.getCarPosition();
        Vector2f vectorToPoint = new Vector2f(nextRoutePoint.x - carPosition.x, nextRoutePoint.y - carPosition.y);
        float angle = Vector2f.angle(ai.getCarDirection(), vectorToPoint);

        // Calculate interpolation for slowing down
        double factor = Math.pow((distance / BehaviourStats.Checkpoint.DECELERATE_DISTANCE + (Math.PI - angle) / Math.PI) / 2f, 3);
        double newSpeed = factor * info.getMaxVelocity();
        double speedDiff = newSpeed - info.getVelocity().length();
        return (float) (speedDiff / BehaviourStats.Checkpoint.THROTTLE_TIME);
    }

    private float getRoutePointThrottle(double distance) {
        if (distance > BehaviourStats.RoutePoint.DECELERATE_DISTANCE)
            return info.getMaxAbsoluteAcceleration();

        double factor = Math.pow(((currentRoutePoint.getAngleToNextPoint() / Math.PI) + (distance / BehaviourStats.RoutePoint.DECELERATE_DISTANCE)) / 2f, 3);
        double newSpeed = Math.min(factor, 1) * info.getMaxVelocity();
        double speedDiff = newSpeed - info.getVelocity().length();
        return (float) (speedDiff / BehaviourStats.RoutePoint.THROTTLE_TIME);
    }

    private boolean isPreviousPointOutOfRange() {
        if (previousRoutePoint.type == LevelPoint.Type.STEP_POINT)
            return true;
        Point carPosition = new Point((int) info.getX(), (int) info.getY());
        double distance = previousRoutePoint.distance(carPosition);

        return (previousRoutePoint.type == LevelPoint.Type.CHECKPOINT && distance > BehaviourStats.Checkpoint.DECELERATE_DISTANCE) ||
                (previousRoutePoint.type == LevelPoint.Type.ROUTE_POINT && distance > BehaviourStats.RoutePoint.DECELERATE_DISTANCE);
    }
}
