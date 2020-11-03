package giordani.tabzai.player.brain;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import giordani.tabzai.player.brain.kernel.Kernel;
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

public class BrainDeepGen implements Brain {

	private Kernel kernel;
	private Node root;
	private Action bestAction;
	private Duration timeout;
	private Game rules;
	private Action selected;
	private int depth;

	public BrainDeepGen(int timeout, int gametype, double mutationProb, double mutationScale, int depth) {
		// Constructor for training phase
		this(timeout, gametype, depth);
		this.kernel = Kernel.of(mutationProb, mutationScale);
	}

	public BrainDeepGen(double mutationProb, double mutationScale, int depth) {
		this(60, 1, mutationProb, mutationScale, depth);
	}
	
	public BrainDeepGen(String name, int timeout, int gametype, int depth) {
		// The constructor for the runtime player
		this(timeout, gametype, depth);
		this.kernel = Kernel.load(name);
	}
	
	private BrainDeepGen(int timeout, int gametype, int depth) {
		State state = null;
		this.depth = depth;
		this.timeout = Duration.ofSeconds(timeout-1);
		switch (gametype) {
		case 1:
			rules = new GameAshtonTablutNoLog(0, 0);
			state = new StateTablut();
			break;
		case 2:
			rules = new GameModernTablut();
			state = new StateTablut();
			break;
		case 3:
			rules = new GameTablut();
			state = new StateTablut();
			break;
		case 4:
			rules = new GameAshtonTablutNoLog(0, 0);
			state = new StateTablut();
			break;
			
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}
		this.root = new Node(state);
	}
	
	@Override
	public Action getAction(State state) {
		selected = findAction(state);
		return this.selected;
	}
	
	@Override
	public void update(State state) {
		for(Node child : this.root.getChildren())
			if(child.getState().equals(state)) {
				this.root = child;
				return;
			}
		// If no child of the root has the given state create a new root
		this.root = new Node(state);
	}
	
	@Override
	public int getTimeout() { return (int) timeout.toSeconds();}
	@Override
	public void setTimeout(int timeout) { this.timeout = Duration.ofSeconds(timeout-1);}
	@Override
	public Game getRules() { return rules;}
	
	public int getDepth() { return this.depth;}
	public void setDepth(int depth) { this.depth = depth;}
	public Kernel getKernel() { return kernel;}
	public void setKernel(Kernel kernel) { this.kernel = kernel;}
	public Node getRoot() { return this.root;}
	
	private Action getBestAction() { return this.bestAction;}

	protected Action findAction(State state) {
		update(state);
		
		this.getRoot().expandAlphaBeta(this.getDepth());
		
		//alphaBeta(this.root, this.getDepth(), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maximize);
		
		System.out.println("\nState evaluation : " + root.getVal() + " [depth = " + this.getDepth() + "]");
						
		return getBestAction();		
	}
	
	/*
	 * Private class Node use the methods of the brain to get the rules and the kernel
	 */
	
	public class Node implements Comparable<Node> {
		private Node parent;
		private Action action;
		private State state;
		private SortedSet<Node> children;
		private double val;
		private Pawn turn;
		
		public Node(Node parent, Action action, State state) {
			super();
			this.action = action;
			this.state = state;
			this.parent = parent;
			this.children = new TreeSet<>();
			this.turn = (this.getState().getTurn().equals(Turn.WHITE)) ? Pawn.WHITE : Pawn.BLACK;
		}
		public Node(State state) {
			this(null, null, state);
		}
		public SortedSet<Node> getChildren() {return children;}
		public State getState() {return state;}
		public Node getParent() {return parent;}
		public Action getAction() {return action;}
		public boolean isLeaf() {return this.getChildren().isEmpty();}
		public void addChild(Node child) {children.add(child);}
		public void addAllChild(Collection<Node> child) {children.addAll(child);}
		public String toString() {return state.toString();}
		public double getVal() {return val;}
		
		@Override
		public int compareTo(Node o) {
			return Double.compare(this.getVal(), o.getVal());
		}
		
		public Action getBest() {
			if(this.getState().getTurn().equals(Turn.BLACK))
				return this.getChildren().first().getAction();
			else return this.getChildren().last().getAction();
		}
		
		private void expand(Node node) {
			State state = node.getState();
			List<int[]> pawns = getPawns();
			
			for(int[] pos : pawns)
				node.addAllChild(tryAllAction(pos[0], pos[1], state, node));
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
		
		private Set<Node> tryAllAction(int row, int col, State state, Node parent) {
			Set<Node> children = new HashSet<Node>();
//			System.out.println("Next Pawn:");
			for(int i=state.getBoard().length; i>0; i--) {
				//System.out.println("\ti = "+i);
				int[][] l = {{row, col+i}, 
							 {row, col-i}, 
					         {row+i, col}, 
					         {row-i, col}};
				List<int[]> newPosList = Arrays.asList(l);
				Collections.shuffle(newPosList);
				for(int[] pos : newPosList) {
					//System.out.println("\t\tpos = " + pos[0] + ", " + pos[1]);
					if(pos[0]>=0 && pos[0]<state.getBoard().length && pos[1]>=0 && pos[1]<state.getBoard().length) {
						//System.out.println("Trying...");
						try {
							Action a = new Action(state.getBox(row, col), state.getBox(pos[0], pos[1]), state.getTurn());
							State newState = getRules().checkMove(state.clone(), a);
							
//							System.out.println("\t\t" + a + " (" + ev + ")");
							
							children.add(new Node(parent, a, newState));
						} catch(ActionException | BoardException | CitadelException | 
								ClimbingCitadelException | ClimbingException | 
								DiagonalException | OccupitedException | PawnException | 
								StopException | ThroneException e) {
							//System.out.println(e.getMessage());
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			return children;
		}
		
		public void expandAlphaBeta(int depth) {
			
		}
		
		private double alphaBeta(Node node, int depth, double alpha, double beta, boolean maximize) {
			// If the maximum depth is reached
			if(depth == 0)
				return kernel.evaluate(node.getState());
			
			// Try to expand that node
			expand(node);
			
			// If no other action are allowed from that node
			if(node.isLeaf())
				return kernel.evaluate(node.getState());
			
			
			if(maximize) {
				double v = Double.NEGATIVE_INFINITY;
				for(Node child : node.getChildren()) {
					double val = alphaBeta(child, depth-1, alpha, beta, false);
					v = Math.max(v, val);
					alpha = Math.max(alpha, v);
					if(beta <= alpha)
						break;
				}
				return v;
			} else {
				double v = Double.POSITIVE_INFINITY;
				for(Node child : node.getChildren()) {
					double val = alphaBeta(child, depth-1, alpha, beta, true);
					v = Math.min(v, val);
					beta = Math.min(beta, v);
					if(beta <= alpha)
						break;
				}
				return v;
			}
		}
	}
}
