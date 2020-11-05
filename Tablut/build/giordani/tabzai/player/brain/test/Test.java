package giordani.tabzai.player.brain.test;

import giordani.tabzai.player.brain.BrainAlphaBeta;
import giordani.tabzai.player.brain.BrainAlphaBeta.Node;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class Test {

	public static void main(String[] args) {
		int depth = 2;
		BrainAlphaBeta brain = new BrainAlphaBeta(60, 1, 0.5, 0.5, depth);
		
		System.out.println("Inizio");
		State s = new StateTablut();
		s.setTurn(Turn.WHITE);
		brain.getAction(s);
		System.out.println("\n\nFine\n\n");
		
		Node tree = brain.getRoot();
		
		System.out.println(brain.countNodes(tree));
		
//		int cont = 0;
//						
//		while(! tree.getChildren().isEmpty()) {
//			System.out.println(cont++ + System.lineSeparator() + tree);
//			tree = tree.getBestChild();
//		}
		
		System.out.println(brain.getKernel());
		
		brain.getKernel().save("pippo");
		
		BrainAlphaBeta newBrain = new BrainAlphaBeta("pippo", 60, 1, depth);
		
		System.out.println(newBrain.getKernel());
		
		System.out.println("Inizio");
		State s2 = new StateTablut();
		s2.setTurn(Turn.WHITE);
		newBrain.getAction(s);
		System.out.println("\n\nFine\n\n");
		
		Node tree2 = newBrain.getRoot();
		
		System.out.println(brain.countNodes(tree2));
	}

}