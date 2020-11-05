package giordani.tabzai.player.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import giordani.tabzai.player.brain.kernel.Kernel;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.exceptions.ActionException;
import it.unibo.ai.didattica.competition.tablut.exceptions.BoardException;
import it.unibo.ai.didattica.competition.tablut.exceptions.CitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingCitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingException;
import it.unibo.ai.didattica.competition.tablut.exceptions.DiagonalException;
import it.unibo.ai.didattica.competition.tablut.exceptions.OccupitedException;
import it.unibo.ai.didattica.competition.tablut.exceptions.PawnException;
import it.unibo.ai.didattica.competition.tablut.exceptions.StopException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ThroneException;

public class BrainAlphaBeta extends BrainAbs {

	private Kernel kernel;
	private Node root;

	public BrainAlphaBeta(int timeout, int gametype, double mutationProb, double mutationScale, int depth) {
		// Constructor for training phase
		super(timeout, gametype, depth);
		this.kernel = Kernel.of(mutationProb, mutationScale);
		init();
	}

	public BrainAlphaBeta(double mutationProb, double mutationScale, int depth) {
		this(60, 1, mutationProb, mutationScale, depth);
	}
	
	public BrainAlphaBeta(String name, int timeout, int gametype, int depth) {
		// The constructor for the runtime player
		super(timeout, gametype, depth);
		this.kernel = Kernel.load(name);
		init();
	}
	
	public BrainAlphaBeta(String name) {
		this(name, 60, 1, 3);
	}
	
	private void init() {
		State state = new StateTablut();
		state.setTurn(Turn.WHITE);
		setRoot(new Node(state));
	}
	
	@Override
	public void update(State state) {
		for(Node child : getRoot().getChildren())
			if(child.getState().equals(state)) {
				setRoot(child);
				return;
			}
		// If no child of the root has the given state create a new root
		setRoot(new Node(state));
	}
	
	public Kernel getKernel() { return kernel;}
	public void setKernel(Kernel kernel) { this.kernel = kernel;}
	public Node getRoot() { return this.root;}
	
	private void setRoot(Node newRoot) { this.root = newRoot;}
	
	@Override
	protected Action getBestAction() { return root.getBestChild().getAction();}
	
	@Override
	protected Action findAction(State state) {
		update(state);
		
		this.getRoot().expandAlphaBeta(this.getDepth());
		
		//System.out.println("\nState evaluation : " + root.getVal() + " [depth = " + this.getDepth() + "]");
						
		return getBestAction();		
	}
	
	public int countNodes(Node root) {
		int cont=1;
		for(Node child : root.getChildren())
			cont += countNodes(child);
		return cont;
	}
	
	/*
	 * Private class Node use the methods of the brain to get the rules and the kernel
	 */
	
	public class Node {
		private Node parent;
		private Action action;
		private State state;
		private Set<Node> children;
		private double val;
		private Pawn turn;
		
		public Node(Node parent, Action action, State state) {
			super();
			this.action = action;
			this.state = state;
			this.parent = parent;
			this.children = new HashSet<>();
			this.turn = (this.getState().getTurn().equals(Turn.WHITE)) ? Pawn.WHITE : Pawn.BLACK;
			this.val = Double.NaN;
		}
		
		public Node(State state) {
			this(null, null, state);
		}
		
		public Set<Node> getChildren() {return children;}
		public State getState() 			{return state;}
		public Node getParent() 			{return parent;}
		public Action getAction() 			{return action;}
		public boolean isLeaf() 			{return this.getChildren().isEmpty();}
		public void addAllChild(Collection<Node> child) {children.addAll(child);}
		public double getVal() 				{return val;}
		private void setVal(double val) 	{ this.val = val;}
		public void addChild(Node child) 	{children.add(child);}
		
		@Override
		public String toString() {
			if(this.action == null)
				return "root\n" + this.state.toString() + 
						"\nState evaluation : " + this.getVal() + " [depth = " + getDepth() + "]";
			return this.action.toString() + "\n" + this.state.toString() + 
					"\nState evaluation : " + this.getVal() + " [depth = " + getDepth() + "]";
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((action == null) ? 0 : action.hashCode());
			result = prime * result + ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + ((state == null) ? 0 : state.hashCode());
			result = prime * result + ((turn == null) ? 0 : turn.hashCode());
			long temp;
			temp = Double.doubleToLongBits(val);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (action == null) {
				if (other.action != null)
					return false;
			} else if (!action.equals(other.action))
				return false;
			if (parent == null) {
				if (other.parent != null)
					return false;
			} else if (!parent.equals(other.parent))
				return false;
			if (state == null) {
				if (other.state != null)
					return false;
			} else if (!state.equals(other.state))
				return false;
			if (turn != other.turn)
				return false;
			if (Double.doubleToLongBits(val) != Double.doubleToLongBits(other.val))
				return false;
			return true;
		}

		public Node getBestChild() {
			Comparator<Node> comparator = Comparator.comparing(Node::getVal);
			if(this.getState().getTurn().equals(Turn.BLACK))
				return this.getChildren().stream().min(comparator).get();
			else return this.getChildren().stream().max(comparator).get();
		}
		
		private List<int[]> getPawns() {
			List<int[]> ret = new ArrayList<>();
			for(int i=0; i<state.getBoard().length; i++)
				for(int j=0; j<state.getBoard()[0].length; j++) {
					Pawn p = state.getPawn(i, j);
					int[] pos = {i, j};
					if(p.equals(turn)) {
						ret.add(pos);
					} else if(p.equals(Pawn.KING) && turn.equals(Pawn.WHITE)) {
						ret.add(pos);
					}
				}
			return ret;
		}
		
		public void expandAlphaBeta(int depth) {
			this.setVal(alphaBeta(this, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		}
		
		private double alphaBeta(Node node, int depth, double alpha, double beta) {
			// If the maximum depth is reached
			if(depth == 0 || node.getState().getTurn().equals(Turn.DRAW) ||
					node.getState().getTurn().equals(Turn.BLACKWIN) ||
					node.getState().getTurn().equals(Turn.WHITEWIN))
				return getKernel().evaluate(node.getState());
			
			double best;
			
			if(node.getState().getTurn().equals(Turn.WHITE)) {
				best = Double.NEGATIVE_INFINITY;
				for(Action action : node.getAllActions()) {
					Node child = node.tryAndAdd(action);
					if(child != null) {
						child.setVal(alphaBeta(child, depth-1, alpha, beta));
						best = Math.max(best, child.getVal());
						alpha = Math.max(alpha, best);
						if(beta <= alpha)
							break;
					}
				}
			} else {
				best = Double.POSITIVE_INFINITY;
				for(Action action : node.getAllActions()) {
					Node child = node.tryAndAdd(action);
					if(child != null) {
						child.setVal(alphaBeta(child, depth-1, alpha, beta));
						best = Math.min(best, child.getVal());
						beta = Math.min(beta, best);
						if(beta <= alpha)
							break;
					}
				}
			}
			return best;
		}
		
		private Node tryAndAdd(Action action) {
			try {
				State newState = getRules().checkMove(this.getState().clone(), action);
				Node n = new Node(this, action, newState);
				this.addChild(n);
				return n;
			} catch(ActionException | BoardException | CitadelException | 
					ClimbingCitadelException | ClimbingException | 
					DiagonalException | OccupitedException | PawnException | 
					StopException | ThroneException e) {
				return null;
			}
		}

		private List<Action> getAllActions() {
			List<Action> ret = new ArrayList<>();
			for(int[] pawn : this.getPawns()) {
				int row = pawn[0];
				int col = pawn[1];
				State state = this.getState();
				for(int i=state.getBoard().length; i>0; i--) {
					//System.out.println("\ti = "+i);
					int[][] newPosList = {{row, col+i}, 
										 {row, col-i}, 
								         {row+i, col}, 
								         {row-i, col}};
					for(int[] pos : newPosList) {
						if(pos[0]>=0 && pos[0]<state.getBoard().length && pos[1]>=0 && pos[1]<state.getBoard().length) {
							try {
								ret.add(new Action(state.getBox(row, col), state.getBox(pos[0], pos[1]), state.getTurn()));
							} catch (IOException e) {}
						}
					}
				}
			}
			return ret;
		}

		private BrainAlphaBeta getEnclosingInstance() {
			return BrainAlphaBeta.this;
		}
	}
}
