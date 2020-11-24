package giordani.tabzai.player.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import giordani.tabzai.player.brain.heuristic.Heuristic;
import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
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

public class BrainAlphaBeta implements Brain {
	private Heuristic heuristic;
	private Node root;
	private Game rules;
	private int depth;
	private long timeout;
	private long stopTime;
	
	public BrainAlphaBeta(int timeout) {
		this(timeout, 1);
	}
	
	public BrainAlphaBeta(int timeout, int gametype) {
		// Constructor for training phase
		this("new", timeout, gametype);
		this.heuristic = Heuristic.of("new");
		resetRoot();
	}
	
	public BrainAlphaBeta(String filename, int timeout, int gametype) {
		this.heuristic = Heuristic.of(filename);
		resetRoot();
		this.resetDepth();
		this.timeout = (long) ((0.9*timeout) * 1000);  // -1 not enough, it runs in server timeout
		switch (gametype) {
		case 1:
			rules = new GameAshtonTablutNoLog(0, 0);
			break;
		case 2:
			rules = new GameModernTablut();
			break;
		case 3:
			rules = new GameTablut();
			break;
		case 4:
			rules = new GameAshtonTablutNoLog(0, 0);
			break;
			
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}
	}
		
	@Override
	public Action getAction(State state) {
		this.startTimer();
		this.update(state);
		this.resetDepth();
		try{
			while(true) {
				this.incrementDepth();
				this.getRoot().expandAlphaBeta(this.getDepth());
			}
		} catch(TimeOutException e) {}
		return this.getRoot().getBestAction();
	}
	
	private void startTimer() {
		this.stopTime = System.currentTimeMillis() + this.timeout;
	}	
	
	/**
	 * Method to call to interrupt the search at the occurence of the timeout
	 * @throws TimeOutException
	 */
	protected void checkTimeout() throws TimeOutException { 
		if(this.stopTime < System.currentTimeMillis())
			throw new TimeOutException();
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
		return "State evaluation : " + this.getRoot().getVal() + " [depth = " + this.getRoot().calcDepth() + "]";
	}
	
	public Game getRules() 							{ return rules;					}
	
	public int getDepth() 							{ return this.depth;			}
	public void resetDepth() 						{ this.depth = 0;				}
	public void incrementDepth()					{ this.depth++;					}
	
	public Heuristic getHeuristic() 				{ return heuristic;				}
	public void setHeuristic(Heuristic heuristic) 	{ this.heuristic = heuristic;	}
	
	public Node getRoot() 							{ return this.root;				}
	private void setRoot(Node newRoot) 				{ this.root = newRoot;			}
	
	
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
	 * Private class Node use the methods of the brain to get the rules and the heuristic
	 */
	
	public class Node {
		private State state;
		private Map<Action, Node> children;
		private List<Action> allActions;
		private double val;
		
		public Node(State state) {
			super();
			this.state = state;
			this.children = new HashMap<>();
			this.allActions = new ArrayList<>();
			this.val = Double.NaN;
		}
		
		public State getState() 			{return state;}
		public double getVal() 				{return val;}
		private void setVal(double val) 	{ this.val = val;}
		
		public Map<Action, Node> getChildren() {return children;}
		public boolean isLeaf() 			{return this.getChildren().isEmpty();}
		public void addChild(Action action, Node child) 	{children.put(action, child);}
		
		@Override
		public String toString() {
			return "State evaluation : " + this.getVal() + " [depth = " + this.calcDepth() + "]\nChildren : " + this.getChildren().size();
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((children == null) ? 0 : children.hashCode());
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
			double bestVal;
			
			if(this.getState().getTurn().equals(Turn.BLACK)) {
				bestVal = Double.POSITIVE_INFINITY;
				for(Entry<Action, Node> e : this.getChildren().entrySet()) {
					if(e.getValue().getVal() <= bestVal) {
						best = e.getKey();
						bestVal = e.getValue().getVal();
					}
				}
			}
			else {
				bestVal = Double.NEGATIVE_INFINITY;
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
					bestVal = Double.NaN;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.setVal(bestVal);
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
		
		public void expandAlphaBeta(int depth) throws TimeOutException {
			this.alphaBeta(depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		}
		
		private void alphaBeta(int depth, double alpha, double beta) throws TimeOutException {
			// This check if the timeout occurred, the exception stops the loop of increasing depth in the BrainAbs method
			checkTimeout();
			// If the maximum depth is reached or the node is terminal
			if(depth == 0 || this.isTerminal())
				this.setVal(getHeuristic().evaluate(this.getState()));
			
			else if(this.getState().getTurn().equals(Turn.WHITE)) {
				double best = Double.NEGATIVE_INFINITY;
				for(Action action : this.getAllActions()) {
					Node child;
					if(this.getChildren().containsKey(action))
						child = this.getChildren().get(action);
					else child = this.tryAndAdd(action);
					if(child != null) {
						try {
							child.alphaBeta(depth-1, alpha, beta);
						} catch(TimeOutException e) {
								best = Math.max(best, child.getVal());
								if(!Double.isNaN(best))
									this.setVal(best);
								throw e;
						}
						best = Math.max(best, child.getVal());
						alpha = Math.max(alpha, best);
						if(beta <= alpha)
							break;
					}
				}
				this.setVal(best);
			} else {
				double best = Double.POSITIVE_INFINITY;
					for(Action action : this.getAllActions()) {
						Node child;
						if(this.getChildren().containsKey(action))
							child = this.getChildren().get(action);
						else child = this.tryAndAdd(action);
						if(child != null) {
							try {
								child.alphaBeta(depth-1, alpha, beta);
							} catch(TimeOutException e) {
									best = Math.min(best, child.getVal());
									if(!Double.isNaN(best))
										this.setVal(best);
									throw e;
							}
							best = Math.min(best, child.getVal());
							beta = Math.min(beta, best);
							if(beta <= alpha)
								break;
						}
					}
					this.setVal(best);
			}
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
				Node n = new Node(newState);
				this.addChild(action, n);
				return n;
			} catch(ActionException | BoardException | CitadelException | 
					ClimbingCitadelException | ClimbingException | 
					DiagonalException | OccupitedException | PawnException | 
					StopException | ThroneException e) {
				return null;
			}
		}
		
		/**
		 * Get all the possible action deriving from this node.state.
		 * Excluded only those Action.to goes out of the box
		 * The moves goes from the maximum movement of the pawns to the move of one case.
		 * The main part of this method is executed only once for each node
		 * @return
		 */
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
		
		/**
		 * Calculate the depth of the best path find during search
		 * @return
		 */
		public int calcDepth() {
			Node n = this;
			int depth = 0;
			try {
				while(!n.isLeaf()) {
					n = n.getChildren().get(n.getBestAction());
					depth++;
				}
			} catch (NullPointerException e) {}
			
			return depth;
		}

		private BrainAlphaBeta getEnclosingInstance() {
			return BrainAlphaBeta.this;
		}
	}
}
