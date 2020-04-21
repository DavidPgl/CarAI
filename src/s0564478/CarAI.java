package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import s0564478.behaviour.SteeringBehaviour;
import s0564478.behaviour.ThrottleBehaviour;

import java.awt.*;

public class CarAI extends AI {
    private final ThrottleBehaviour throttleBehaviour;
    private final SteeringBehaviour steeringBehaviour;

    public CarAI(Info info) {
        super(info);

        throttleBehaviour = new ThrottleBehaviour(info);
        steeringBehaviour = new SteeringBehaviour(info);

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
    }

    private void drawLine(Vector2f point, Vector2f direction, Color color) {
        drawLine(point.x, point.y, point.x + direction.x * 10, point.y + direction.y * 10, color);
    }

    private void drawLine(double x1, double y1, double x2, double y2, Color color) {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(color.getRed(), color.getGreen(), color.getBlue());
        GL11.glVertex2d(x1, y1);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
    }
}
