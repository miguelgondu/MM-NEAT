/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package micro.ai.core;

import java.util.List;

import micro.rts.GameState;
import micro.rts.PlayerAction;

/**
 *
 * @author santi
 */
public abstract class AI {
    public abstract void reset();
    
    
    public abstract PlayerAction getAction(int player, GameState gs) throws Exception;
    
    
    @Override
    public abstract AI clone();   // this function is not supposed to do an exact clone with all the internal state, etc.
                                  // just a copy of the AI with the same configuration.
    
    
    // This method can be used to report any meaningful statistics once the game is over 
    // (for example, average nodes explored per move, etc.)
    public String statisticsString() {
        return null;
    }
    
    
    public void printStats() {
        String stats = statisticsString();
        if (stats!=null) System.out.println(stats);        
    }
    
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }  
    
    
    public abstract List<ParameterSpecification> getParameters();
} 
