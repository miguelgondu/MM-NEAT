package utopia.agentmodel.actions;

import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Move;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import edu.utexas.cs.nn.Constants;
import java.util.Collection;
import java.util.Iterator;
import mockcz.cuni.pogamut.Client.AgentBody;
import mockcz.cuni.pogamut.Client.AgentMemory;
import utopia.Utils;
import utopia.controllers.scripted.PathController;

/**
 *
 * @author nvh
 */
public class GotoItemAction extends OpponentRelativeAction {

    @Override
    /**
     * allows the bot to print out a statement describing its actions
     */
    public String toString() {
        String name = (item == null ? "[NONE]" : item.getType().getName());
        return "GotoItem:" + name.substring(name.indexOf(".") + 1);
    }
    private final Item item;

    /**
     * Initializes the actions
     * @param memory (agent memory to use)
     * @param item (item that the bot is looking for)
     * @param shoot (should the bot shoot)
     * @param secondary (should the bot use secondary firing mode)
     * @param jump (shoudl the bot jump)
     */
    public GotoItemAction(AgentMemory memory, Item item, boolean shoot, boolean secondary, boolean jump) {
        super(memory, shoot, secondary, jump);
        this.item = item;
    }

    /**
     * Initializes the action with only memory and item bot assumes it shouldnot be shooting or jumping
     * @param memory (agent memory to use)
     * @param item (item that the bot is looking for)
     */
    public GotoItemAction(AgentMemory memory, Item item) {
        this(memory, item, false, false, false);
    }

    /**
     * decides whether the bot will retrieve an item
     * @param memory (agent memory to use)
     * @param candidate (item looking at)
     * @return returns whether the bot will pick up item
     */
    public static boolean willGoto(AgentMemory memory, Item candidate) {
        return (candidate != null && PathController.wantItem(memory, candidate));
    }

    @Override
    /**
     * tells the bot to carry out the action
     */
    public void execute(AgentBody body) {
        Player enemy = this.memory.getCombatTarget();
        if (enemy != null && enemy.getLocation() != null) {
            super.shootDecision(enemy);
        }

        if (item == null) {
            System.out.println("Goto Item Failure: Null Item");
            return;
        }

        Location itemLocation = item.getLocation();
        Location agentLocation = memory.info.getLocation();
        if (itemLocation != null && agentLocation != null) {
            Location second = null; //itemLocation.add(memory.info.getVelocity().normalize().scale(100));
            Collection<NavPoint> visibleNavs = body.world.getAllVisible(NavPoint.class).values();
            visibleNavs = removeNavWithLocation(visibleNavs, itemLocation);
            if (!visibleNavs.isEmpty()) {
                second = Utils.getFarthest(visibleNavs, agentLocation).getLocation();
            } else {
                second = itemLocation.sub(memory.info.getVelocity().normalize().scale(200));
            }
            Move move = new Move().setFirstLocation(itemLocation).setSecondLocation(second);
            if (enemy != null) {
                move.setFocusTarget(enemy.getId());
            }

            memory.body.act.act(move);
            //System.out.println(move);
        }

        jumpDecision(body);
    }

    /**
     * removes a nav point from the list of visible ones
     * @param visibleNavs (visible nav points)
     * @param itemLocation (location of the given item)
     * @return
     */
    private static Collection<NavPoint> removeNavWithLocation(Collection<NavPoint> visibleNavs, Location itemLocation) {
        Iterator<NavPoint> itr = visibleNavs.iterator();
        while (itr.hasNext()) {
            if (itr.next().getLocation().equals(itemLocation, 20)) {
                itr.remove();
            }
        }
        return visibleNavs;
    }
}
