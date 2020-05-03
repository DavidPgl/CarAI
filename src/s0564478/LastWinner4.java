package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;

import java.util.ArrayList;
import java.util.List;

public class LastWinner4 extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;

    private final List<Runnable> debugActions = new ArrayList<>();

    public LastWinner4(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, 3.78f, 10.84f, 1.32f);
        steeringBehaviour = new SteeringBehaviour(info, 0.65f, 28.17f, 0.16f, 39.14f);

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
