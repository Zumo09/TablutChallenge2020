package giordani.tabzai.player.brain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class Node {
	private boolean root;
	private Node parent;
	private Action action;
	private State state;
	private Set<Node> children;
	
	public Node(Node parent, Action action, State state) {
		super();
		this.action = action;
		this.state = state;
		this.parent = parent;
		if(parent == null) root = true;
		else root = false;
		this.children = new HashSet<>();
	}
	public Set<Node> getChildren() {
		return children;
	}
	public State getState() {
		return state;
	}
	public Node getParent() {
		return parent;
	}
	public Action getAction() {
		return action;
	}
	public boolean isLeaf() {
		return this.getChildren().isEmpty();
	}
	public boolean isRoot() {
		return root;
	}
	public void setRoot(boolean root) {
		this.root = root;
	}
	public void addChild(Node child) {
		children.add(child);
	}
	public void addAllChild(Set<Node> child) {
		children.addAll(child);
	}
	public List<Node> getTrace() {
		List<Node> ret = new ArrayList<>();
		this.getTrace(ret);
		return ret;
	}
	private void getTrace(List<Node> upTo) {
		if(!this.isRoot())
			getParent().getTrace(upTo);
		upTo.add(this);
		
	}
	public String toString() {
		return state.toString();		
	}
}
