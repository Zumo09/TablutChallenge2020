package giordani.tabzai.player.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import giordani.tabzai.player.brain.heuristic.Heuristic;
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
	private Heuristic kernel;
	private Node root;

	public BrainAlphaBeta(int timeout, int gametype) {
		// Constructor for training phase
		super(timeout, gametype);
		this.kernel = Heuristic.load("new");
		resetRoot();
	}

	public BrainAlphaBeta() {
		this(60, 1);
	}
	
	public BrainAlphaBeta(String name, int timeout, int gametype) {
		// The constructor for the runtime player
		super(timeout, gametype);
		this.kernel = Heuristic.load(name);
		resetRoot();
	}
	
	public BrainAlphaBeta(String name) {
		this(name, 60, 1);
	}
	
	@Override
	public void update(State state) {
		for(Node child : getRoot().getChildren().values())
			if(child.getState().equals(state)) {
				setRoot(child);
				return;
			}
		// If no child of the root has the given state create a new root
		setRoot(new Node(state));
	}
	
	@Override
	public String getInfo() {
		return "State evaluation : " + this.getRoot().getVal() + " [depth = " + this.getDepth() + "]";
	}
	
	public Heuristic getHeuristic() { return kernel;}
	public void setHeuristic(Heuristic kernel) { this.kernel = kernel;}
	public Node getRoot() { return this.root;}
	
	private void setRoot(Node newRoot) { this.root = newRoot;}
	
	@Override
	protected Action getBestAction() { return root.getBestAction();}
	
	@Override
	protected void searchAction() {
		this.getRoot().expandAlphaBeta(this.getDepth());					
	}
	
	@Override
	public String toString() {
		return "State evaluation : " + this.getRoot().getVal() + " [depth = " + this.getDepth() + "]";
	}
	
	public void resetRoot() {
		State state = new StateTablut();
		state.setTurn(Turn.WHITE);
		setRoot(new Node(state));
	}
	
	public int countNodes(Node root) {
		int cont=1;
		for(Node child : root.getChildren().values())
			cont += countNodes(child);
		return cont;
	}
	
	/*
	 * Private class Node use the methods of the brain to get the rules and the kernel
	 */
	
	public class Node {
		private Node parent;
		private State state;
		private Map<Action, Node> children;
		private List<Action> allActions;
		private double val;
		
		public Node(Node parent, State state) {
			super();
			this.state = state;
			this.parent = parent;
			this.children = new HashMap<>();
			this.allActions = new ArrayList<>();
			this.val = Double.NaN;
		}
		
		public Node(State state) {
			this(null, state);
		}
		
		public State getState() 			{return state;}
		public Node getParent() 			{return parent;}
		public double getVal() 				{return val;}
		private void setVal(double val) 	{ this.val = val;}
		
		public Map<Action, Node> getChildren() {return children;}
		public boolean isLeaf() 			{return this.getChildren().isEmpty();}
		public void addChild(Action action, Node child) 	{children.put(action, child);}
		
		@Override
		public String toString() {
			return "State evaluation : " + this.getVal() + " [depth = " + getDepth() + "]\nChildren : " + this.getChildren().size();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((children == null) ? 0 : children.hashCode());
			result = prime * result + ((parent == null) ? 0 : parent.hashCode());
			result = prime * result + ((state == null) ? 0 : state.hashCode());
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
			if (children == null) {
				if (other.children != null)
					return false;
			} else if (!children.equals(other.children))
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
			if (Double.doubleToLongBits(val) != Double.doubleToLongBits(other.val))
				return false;
			return true;
		}

		public Action getBestAction() {
			Action best = null;
			
			if(this.getState().getTurn().equals(Turn.BLACK)) {
				double bestVal = Double.POSITIVE_INFINITY;
				for(Entry<Action, Node> e : this.getChildren().entrySet()) {
					if(e.getValue().getVal() <= bestVal) {
						best = e.getKey();
						bestVal = e.getValue().getVal();
					}
				}
			}
			else {
				double bestVal = Double.NEGATIVE_INFINITY;
				for(Entry<Action, Node> e : this.getChildren().entrySet()) {
					if(e.getValue().getVal() >= bestVal) {
						best = e.getKey();
						bestVal = e.getValue().getVal();
					}
				}
			}
			if(best == null) {
				try {
					System.out.println("\n\n\n\n========== NO ACTION FOUND!! ============\n\n\n");
					best = new Action("a1", "a1", this.getState().getTurn());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return best;
		}
		
		private List<int[]> getPawns() {
			Pawn turn = (this.getState().getTurn().equals(Turn.WHITE)) ? Pawn.WHITE : Pawn.BLACK;
			List<int[]> ret = new ArrayList<>();
			for(int i=0; i<state.getBoard().length; i++)
				for(int j=0; j<state.getBoard()[0].length; j++) {
					Pawn p = state.getPawn(i, j);
					int[] pos = {i, j};
					if(p.equals(turn)) {
						ret.add(pos);
					} else if(p.equals(Pawn.KING) && turn.equals(Pawn.WHITE)) {
						ret.add(0, pos);
					}
				}
			return ret;
		}
		
		public void expandAlphaBeta(int depth) {
			this.setVal(alphaBeta(this, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
		}
		
		private double alphaBeta(Node node, int depth, double alpha, double beta) {
			// If the maximum depth is reached or the node is terminal
			if(depth == 0 || node.isTerminal())
				return getHeuristic().evaluate(node.getState());
			
			double best;
			
			if(node.getState().getTurn().equals(Turn.WHITE)) {
				best = Double.NEGATIVE_INFINITY;
				for(Action action : node.getAllActions()) {
					Node child;
					if(node.getChildren().containsKey(action))
						child = node.getChildren().get(action);
					else child = node.tryAndAdd(action);
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
					Node child;
					if(node.getChildren().containsKey(action))
						child = node.getChildren().get(action);
					else child = node.tryAndAdd(action);
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
		
		private boolean isTerminal() {
			Turn turn = this.getState().getTurn();
			return turn.equals(Turn.DRAW) ||
					turn.equals(Turn.BLACKWIN) ||
					turn.equals(Turn.WHITEWIN);
		}

		private Node tryAndAdd(Action action) {
			try {
				State newState = getRules().checkMove(this.getState().clone(), action);
				Node n = new Node(this, newState);
				this.addChild(action, n);
				return n;
			} catch(ActionException | BoardException | CitadelException | 
					ClimbingCitadelException | ClimbingException | 
					DiagonalException | OccupitedException | PawnException | 
					StopException | ThroneException e) {
				return null;
			}
		}

		private List<Action> getAllActions() {
			if(this.allActions.isEmpty()) {
				State state = this.getState();
				List<int[]> pawns = this.getPawns();
				for(int i=state.getBoard().length; i>0; i--) {
					for(int[] pawn : pawns) {
						int row = pawn[0];
						int col = pawn[1];
						//System.out.println("\ti = "+i);
						int[][] newPosList = {{row, col+i}, 
											 {row, col-i}, 
									         {row+i, col}, 
									         {row-i, col}};
						for(int[] pos : newPosList) 
							if(pos[0]>=0 && pos[0]<state.getBoard().length && pos[1]>=0 && pos[1]<state.getBoard().length) 
								try {
									this.allActions.add(new Action(state.getBox(row, col), state.getBox(pos[0], pos[1]), state.getTurn()));
								} catch (IOException e) {}
					}
				}
			}
			
			return this.allActions;
		}

		private BrainAlphaBeta getEnclosingInstance() {
			return BrainAlphaBeta.this;
		}
	}
}
