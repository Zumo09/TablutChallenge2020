package giordani.tabzai.player.brain.test;

import java.io.IOException;

import giordani.tabzai.player.brain.BrainAlphaBeta;
import giordani.tabzai.player.brain.BrainAlphaBeta.Node;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class Test {

	public static void main(String[] args) throws IOException {
		BrainAlphaBeta brain = new BrainAlphaBeta(5, 1);
		System.out.println("Inizio");
		State s = new StateTablut();
		s.setTurn(Turn.WHITE);
		long start = System.currentTimeMillis();
		Action a = brain.getAction(s);
		long stop = (System.currentTimeMillis() - start)/1000;
		System.out.println(a + "\n"+brain.getInfo()+"\nFine\n"+ stop + "\n");
		
		System.in.read();
		
		System.out.println("Inizio");
		// s = brain.getRoot().getChildren().get(a).getState();
		start = System.currentTimeMillis();
		a = brain.getAction(s);
		stop = (System.currentTimeMillis() - start)/1000;
		System.out.println(a + "\n"+brain.getInfo()+"\nFine\n"+ stop + "\n");
		
		System.in.read();
		
		System.out.println("Inizio");
		s = brain.getRoot().getChildren().get(a).getState();
		start = System.currentTimeMillis();
		a = brain.getAction(s);
		stop = (System.currentTimeMillis() - start)/1000;
		System.out.println(a + "\n"+brain.getInfo()+"\nFine\n"+ stop + "\n");
		
		Node tree = brain.getRoot();
		
		System.out.println(brain.countNodes(tree));
		
		int cont = 0;
		
		while(! tree.getChildren().isEmpty()) {
			System.out.println(cont++ + System.lineSeparator() + tree.getState());
			tree = tree.getChildren().get(tree.getBestAction());
		}
		
		System.out.println(brain.getHeuristic());
		
		brain.getHeuristic().save("pippo");
		
		BrainAlphaBeta newBrain = new BrainAlphaBeta("pippo", 5, 1);
		
		System.out.println(newBrain.getHeuristic());
		
		System.out.println("Inizio");
		start = System.currentTimeMillis();
		State s2 = new StateTablut();
		s2.setTurn(Turn.WHITE);
		start = System.currentTimeMillis();
		a = newBrain.getAction(s2);
		stop = (System.currentTimeMillis() - start)/1000;
		System.out.println(a + "\n"+newBrain.getInfo()+"\nFine\n"+ stop + "\n");
		
		Node tree2 = newBrain.getRoot();
		
		System.out.println(brain.countNodes(tree2));
	}

}