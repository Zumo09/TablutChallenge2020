package giordani.tabzai.player.brain.test;

import java.io.IOException;

import giordani.tabzai.player.brain.BrainAlphaBeta;
import giordani.tabzai.player.brain.BrainAlphaBeta.Node;
import giordani.tabzai.player.brain.heuristic.Heuristic;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class TestBrain {

	public static void main(String[] args) throws IOException {
		BrainAlphaBeta brain = new BrainAlphaBeta(30);
		System.out.println("Inizio");
		State s = new StateTablut();
		s.setTurn(Turn.WHITE);
		long start = System.currentTimeMillis();
		Action a = brain.getAction(s);
		long stop = (System.currentTimeMillis() - start);
		System.out.println(a + "\n"+brain.getInfo()+"\nFine\n"+ stop/1000.0 + " s\n");
		Node tree = brain.getRoot();
		
		System.out.println(brain.countNodes(tree));	
		
		int cont = 0;
		
		while(! tree.getChildren().isEmpty()) {
			System.out.println(cont++ + System.lineSeparator() + tree);
			tree = tree.getChildren().get(tree.getBestAction());
		}
		
		/*
		brain.getHeuristic().save("paperino");
		
		System.out.println("Inizio 2?");
		System.in.read();
		
		brain.setHeuristic(Heuristic.of("paperino"));
		// s = brain.getRoot().getChildren().get(a).getState();
		start = System.currentTimeMillis();
		a = brain.getAction(s);
		stop = (System.currentTimeMillis() - start);
		System.out.println(a + "\n"+brain.getInfo()+"\nFine\n"+ stop/1000.0 + " s\n");
		
		tree = brain.getRoot();
		
		System.out.println(brain.countNodes(tree));	
		
		cont = 0;
		
		while(! tree.getChildren().isEmpty()) {
			System.out.println(cont++ + System.lineSeparator() + tree);
			tree = tree.getChildren().get(tree.getBestAction());
		}*/
	}

}