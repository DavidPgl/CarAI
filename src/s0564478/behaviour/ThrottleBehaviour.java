package s0564478.behaviour;

import lenz.htw.ai4g.ai.Info;
import s0564478.CarAI;

import java.awt.*;
import java.util.Optional;

public class ThrottleBehaviour {

    private final Info info;
    private final CarAI ai;
    private Point previousRoutePoint;

    public ThrottleBehaviour(Info info, CarAI ai) {
        this.info = info;
        this.ai = ai;
        this.previousRoutePoint = new Point((int) info.getX(), (int) info.getY());
    }

    public float getThrottle(Point nextRoutePoint) {
        Optional<Float> startAcceleration = getStartAcceleration(nextRoutePoint);
        if (startAcceleration.isPresent()) {
            System.out.println("AHHHHH");
            return startAcceleration.get();
        }

        double distance = nextRoutePoint.distance(info.getX(), info.getY());
        if (distance < BehaviourStats.GOAL_DISTANCE)
            return 0;
        else if (distance < BehaviourStats.DECELERATE_DISTANCE) {
            double newSpeed = (distance / BehaviourStats.DECELERATE_DISTANCE) * info.getMaxVelocity();
            double speedDiff = newSpeed - info.getVelocity().length();
            return (float) (speedDiff / BehaviourStats.THROTTLE_TIME);
        } else
            return info.getMaxAbsoluteAcceleration();
    }

    /**
     * Checks if the car is still close to the last route point, if so, reduces the speed.
     */
    private Optional<Float> getStartAcceleration(Point nextRoutePoint) {
        // Reached last point? Move
        if (!nextRoutePoint.equals(previousRoutePoint)) {
            double distance = previousRoutePoint.distance(info.getX(), info.getY());
            // Still close to previous point
            if (distance > BehaviourStats.ACCELERATE_DISTANCE)
                previousRoutePoint = nextRoutePoint;
            else {
                double factor = distance / BehaviourStats.ACCELERATE_DISTANCE;
                return Optional.of((float) Math.max(BehaviourStats.MIN_ACCELERATION_FACTOR, factor) * 2);
            }
        }
        return Optional.empty();
    }
}
