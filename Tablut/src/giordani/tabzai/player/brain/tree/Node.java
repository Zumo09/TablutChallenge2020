package giordani.tabzai.player.brain.tree;

import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Node {
	private State state;
	private List<Node> children;
	private double val;
	private boolean root;
	private boolean leaf;
	
	public Node(State state) {
		this.state = state;
	}

}
