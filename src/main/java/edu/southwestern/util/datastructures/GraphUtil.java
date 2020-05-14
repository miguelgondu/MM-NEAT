package edu.southwestern.util.datastructures;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.southwestern.tasks.gvgai.zelda.level.Grammar;
import edu.southwestern.tasks.gvgai.zelda.level.GraphRule;
import edu.southwestern.util.MiscUtil;
import edu.southwestern.util.datastructures.Graph.Node;

public class GraphUtil {

	/**
	 * Save a graph of type that extends Grammar, will be saved as a DOT file
	 * @param graph Graph instance with type extending Grammar
	 * @param file File location as a string, including the .dot
	 * @throws IOException
	 */
	public static void saveGrammarGraph(Graph<? extends Grammar> graph, String file) throws IOException {
		File f = new File(file);
		BufferedWriter w = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
		w.write("graph {\n");
		
		Graph<? extends Grammar>.Node n = graph.root();
		List<Graph<? extends Grammar>.Node> visited = new ArrayList<>();
		Queue<Graph<? extends Grammar>.Node> queue = new LinkedList<>();
		queue.add(n);
		while(!queue.isEmpty()) {
			Graph<? extends Grammar>.Node node = queue.poll();
			visited.add(node);
			System.out.println(node.id + ":" + node.adjacencies());
//			MiscUtil.waitForReadStringAndEnterKeyPress();
			w.write(node.getID() + "[label=\"" + node.getData().getLevelType() + "\"]\n");
			for(Graph<? extends Grammar>.Node v : node.adjacencies()) {
				if(!visited.contains(v) && !queue.contains(v)) {
					//visited.add(v);
					queue.add(v);
				}
			}
			
		}
	
		n = graph.root();
		visited = new ArrayList<>();
		queue = new LinkedList<>();
		queue.add(n);
		while(!queue.isEmpty()) {
			Graph<? extends Grammar>.Node node = queue.poll();
	
			visited.add(node);
			for(Graph<? extends Grammar>.Node v : node.adjacencies()) {
				if(!visited.contains(v)) {
					w.write(node.getID() + " -- " + v.getID() +"\n");
					queue.add(v);				
				}
			}
			
		}
		
		
		w.write("}");
		w.close();
	}
	
//	public static void printGrammarGraph(GraphRule<? extends Grammar> r) throws IOException {
//		//File f = new File(file);
//		//BufferedWriter w = new BufferedWriter(new FileWriter(f.getAbsolutePath()));
//		System.out.println("graph!)!)!)!)!)!)!)010101010101010101010101010010101010101001010101010 {\n");
//		System.out.println(r.getSymbolStart());
//		Graph<? extends Grammar> graph = r.getGraph();
//		Graph<? extends Grammar>.Node n = graph.root();
//		List<Graph<? extends Grammar>.Node> visited = new ArrayList<>();
//		Queue<Graph<? extends Grammar>.Node> queue = new LinkedList<>();
//		queue.add(n);
//		while(!queue.isEmpty()) {
//			Graph<? extends Grammar>.Node node = queue.poll();
//			visited.add(node);
//			System.out.println(node.id + ":" + node.adjacencies());
////			MiscUtil.waitForReadStringAndEnterKeyPress();
//			System.out.println(node.getID() + "[label=\"" + node.getData().getLevelType() + "\"]\n");
//			for(Graph<? extends Grammar>.Node v : node.adjacencies()) {
//				if(!visited.contains(v) && !queue.contains(v)) {
//					//visited.add(v);
//					queue.add(v);
//				}
//			}
//			
//		}
//	
//		n = graph.root();
//		visited = new ArrayList<>();
//		queue = new LinkedList<>();
//		queue.add(n);
//		while(!queue.isEmpty()) {
//			Graph<? extends Grammar>.Node node = queue.poll();
//	
//			visited.add(node);
//			for(Graph<? extends Grammar>.Node v : node.adjacencies()) {
//				if(!visited.contains(v)) {
//					System.out.println(node.getID() + " -- " + v.getID() +"\n");
//					queue.add(v);				
//				}
//			}
//			
//		}
//		
//		
//		//w.write("}");
//		//w.close();
//	}
	
}
