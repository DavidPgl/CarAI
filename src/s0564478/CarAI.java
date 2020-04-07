package s0564478;

import lenz.htw.ai4g.ai.AI;
import lenz.htw.ai4g.ai.DriverAction;
import lenz.htw.ai4g.ai.Info;
import org.lwjgl.opengl.GL11;

public class CarAI extends AI{
    public CarAI(Info info) {
        super(info);
        enlistForInternalDevelopmentPurposesOnlyAndDoNOTConsiderThisAsPartOfTheHandedInSolution();
    }

    @Override
    public String getName() {
        return "Kaes3din0";
    }

    @Override
    public DriverAction update(boolean wasResetAfterCollision) {
        return new DriverAction(1, 0.1f);
    }

    @Override
    public String getTextureResourceName() {
        return "/s0564478/art/car.png";
    }

    @Override
    public void doDebugStuff() {
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor3f(1,0,0);
        GL11.glVertex2f(info.getX(), info.getY());
        GL11.glVertex2d(info.getCurrentCheckpoint().getX(), info.getCurrentCheckpoint().getY());
        GL11.glEnd();
    }
}
