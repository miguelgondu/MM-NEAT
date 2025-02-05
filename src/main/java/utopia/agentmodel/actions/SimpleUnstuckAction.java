package utopia.agentmodel.actions;

import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
import mockcz.cuni.pogamut.Client.AgentBody;
import mockcz.cuni.pogamut.Client.AgentMemory;
import utopia.Utils;

/**
 * Helps the bot get around obstacles and avoid becoming stuck
 * @author HeDeceives
 */
public class SimpleUnstuckAction extends Action {

    private final AgentMemory memory;

    /**
     * Initializes the action with the agent memory
     * @param memory (agent memory to use)
     */
    public SimpleUnstuckAction(AgentMemory memory) {
        this.memory = memory;
    }

    @Override
    /**
     * allows the bot to print out a description of its action in the form of a string
     */
    public String toString(){
        return "SimpleUnstuck:" + (choice == null ? "?" : choice);
    }

    public String choice = null;

    @Override
    /**
     * tells the bot to execute the action
     */
    public void execute(AgentBody body) {
        Location agentLocation = memory.info.getLocation();
        if (agentLocation != null && memory.senses.isCollidingOnce() && memory.senses.getCollisionLocation() != null) {
            Location v = memory.senses.getCollisionLocation().getLocation().sub(agentLocation).scale(5);
            Location target = agentLocation.sub(v);
            // make the bot to go to the computed location while facing the bump source
            choice = "Colliding:Move:" + target.toString();
            //body.act.act(new Move().setFirstLocation(target));
            new MoveToLocationAction(memory,target).execute(body);
        }else if(agentLocation != null && memory.senses.isBumpingOnce() && memory.senses.getBumpLocation() != null){
            Location v = memory.senses.getBumpLocation().getLocation().sub(agentLocation).scale(5);
            Location target = agentLocation.sub(v);
            // make the bot to go to the computed location while facing the bump source
            choice = "Bump:Move:" + target.toString();
            body.act.act(new Move().setFirstLocation(target).setFocusLocation(memory.senses.getBumpLocation().getLocation()));
        }else if(Math.random() < 0.5) {
            DodgeShootAction action = new DodgeShootAction(Utils.randposneg() * Math.random(), memory.getAgentLocation(), memory.getAgentRotation());
            choice = action.toString();
            action.execute(body);
        }else if(Math.random() < 0.25){
            body.contMove();
        }else{
            new GotoItemAction(memory, memory.info.getNearestItem()).execute(body);
        }
    }
}
