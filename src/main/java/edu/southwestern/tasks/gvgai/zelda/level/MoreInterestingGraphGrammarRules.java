package edu.southwestern.tasks.gvgai.zelda.level;

import edu.southwestern.util.datastructures.Graph;

public class MoreInterestingGraphGrammarRules extends ZeldaHumanSubjectStudy2019GraphGrammar {
	public MoreInterestingGraphGrammarRules() {
		super();
		GraphRule<ZeldaGrammar> rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.ENEMY_S);
//		rule.grammar().setStart(ZeldaGrammar.START);
//		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
//		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
//		rule.grammar().addNodeBetween(ZeldaGrammar.BOMB_S);
//		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
//		graphRules.add(rule);
		
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		graphRules.add(rule);
		
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);

		graphRules.add(rule);

		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeBetween(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		//rule.grammar().addNodeBetween(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);

		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
	
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().addNodeBetween(ZeldaGrammar.BOMB);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().addNodeBetween(ZeldaGrammar.BOMB);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		graphRules.add(rule);

		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		graphRules.add(rule);
//		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		graphRules.add(rule);
//		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		
		
		
		
		
		
		
		
		
		
		
		
		
		//DEFAULT RULES
		
		//START TO ANYTHING!!
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.BOMB_S);
		rule.grammar().addNodeBetween(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeBetween(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeBetween(ZeldaGrammar.KEY_S);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().setEnd(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.START_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.START);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY_S);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		
		
		//EVERY POSSIBLE KEY COMBO
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().addNodeBetween(ZeldaGrammar.LOCK_S);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.LOCK_S);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.PUZZLE);
		
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.KEY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.KEY);
		rule.grammar().addNodeToStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		
		//EVERY ENEMY COMBO!!
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().addNodeBetween(ZeldaGrammar.SOFT_LOCK_S);
		//rule.grammar().addNodeBetween(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		
		
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.ENEMY_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		
		
		
		
	
		//NOW FOR LOCKS!
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.LOCK_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.LOCK);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		
		
		
		//NOW FOR PUZZLES!
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		
		//rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().addNodeToStart(ZeldaGrammar.SOFT_LOCK_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().addNodeToStart(ZeldaGrammar.ENEMY_S);
		rule.grammar().addNodeToStart(ZeldaGrammar.KEY_S);

		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.RAFT_S);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().setEnd(ZeldaGrammar.RAFT);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.PUZZLE_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.PUZZLE);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		
		
		//NOW FOR RAFTS!
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.KEY_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.KEY);
		graphRules.add(rule);
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.ENEMY_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.ENEMY);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.LOCK_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.LOCK);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.PUZZLE_S);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.PUZZLE);
		graphRules.add(rule);
		
		rule = new GraphRule<ZeldaGrammar>(ZeldaGrammar.RAFT_S, ZeldaGrammar.TREASURE);
		rule.grammar().setStart(ZeldaGrammar.RAFT);
		rule.grammar().setEnd(ZeldaGrammar.TREASURE);
		graphRules.add(rule);
		
		System.out.println("testing:");
		for(GraphRule<ZeldaGrammar> r: graphRules) {
			System.out.println("Start: "+r.getSymbolStart()+", "+r.getSymbolEnd()+" maps to: ");
			System.out.println("Starting");
			System.out.println(r.getStart().getData());
			System.out.println("Between Nodes: ");
			for(Graph<ZeldaGrammar>.Node m : r.getNodesBetween()) {
				System.out.println(m.getData());
			}
			System.out.println("Nodes Added To Start: ");
			for(Graph<ZeldaGrammar>.Node i : r.getNodesToStart()) {
				System.out.println(i.getData());
			}
			System.out.println("ending:");
			System.out.println(r.getEnd());
			System.out.println();

		}
//		System.out.println("testing:");
//		for(GraphRule<ZeldaGrammar> r: graphRules) {
//			System.out.println("Start:"+r.getSymbolStart());
//			System.out.println("End:"+r.getSymbolEnd());
//
//		}
		//initialList.add(ZeldaGrammar.RAFT_S);
		
	}
	
}
