package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;

import java.util.ArrayList;
import java.util.List;

public class LastWinner2 extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;

    private final List<Runnable> debugActions = new ArrayList<>();

    public LastWinner2(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info, 6.58f, 14.22f, 4.11f);
        steeringBehaviour = new SteeringBehaviour(info, 0.74f, 26, 0.22f, 57.41f);

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
