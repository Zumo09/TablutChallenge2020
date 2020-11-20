package giordani.tabzai.player.brain.heuristic.test;

import java.util.List;

import giordani.tabzai.player.brain.heuristic.Heuristic;
import giordani.tabzai.player.brain.heuristic.HeuristicNN;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class Test {
	public static void main(String[] args) {
		StateTablut state = new StateTablut();
		
		HeuristicNN heuristic = new HeuristicNN(16, 32, 64);
//		HeuristicNN heuristic2 = new HeuristicNN(16, 32, 64);
//		
//		System.out.println(heuristic.evaluate(state));
//		System.out.println(heuristic2.evaluate(state));
//		
//		List<Heuristic> children = heuristic.crossover(heuristic2);
//		
//		System.out.println("\n\nAfter Cross");
//		System.out.println(heuristic.evaluate(state));
//		System.out.println(heuristic2.evaluate(state));
//		System.out.println(children.get(0).evaluate(state));
//		System.out.println(children.get(1).evaluate(state));
		
		
		System.out.println(heuristic.getLayer(0).get(0));
		System.out.println(heuristic.evaluate(state));
		
		heuristic.mutate(0.5);
		
		System.out.println(heuristic.getLayer(0).get(0));
		System.out.println(heuristic.evaluate(state));
		
		heuristic.mutate(0.5);
		
		System.out.println(heuristic.getLayer(0).get(0));
		System.out.println(heuristic.evaluate(state));
		
		heuristic.mutate(0.5);
		
		System.out.println(heuristic.getLayer(0).get(0));
		System.out.println(heuristic.evaluate(state));
		
		heuristic.mutate(0.5);
		
		System.out.println(heuristic.getLayer(0).get(0));
		System.out.println(heuristic.evaluate(state));
	}
}
