package giordani.tabzai.player.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import giordani.tabzai.player.brain.kernel.Kernel;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
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

public class BrainDeepGen extends BrainAbs {

	private Kernel kernel;
	private int depth;
	private Node root;
	private Set<Node> leafs;
	private Map<Action, Double> actions;

	public BrainDeepGen(int timeout, int gametype, double mutationProb, double mutationScale, int depth) {
		// Constructor for training phase
		super(timeout, gametype);
		this.kernel = Kernel.of(mutationProb, mutationScale, depth);
		init();
	}

	public BrainDeepGen(double mutationProb, double mutationScale, int depth) {
		this(60, 1, mutationProb, mutationScale, depth);
	}
	
	public BrainDeepGen(String path, int timeout, int gametype) {
		// The constructor for the runtime player
		super(timeout, gametype);
		this.kernel = Kernel.load(path);
		init();
	}
	
	private void init() {
		this.depth = kernel.getDepth();
		this.root = new Node(null, null, getState());
		this.leafs = new HashSet<>();
		this.actions = new HashMap<>();
	}

	public Kernel getKernel() {
		return kernel;
	}

	public void setKernel(Kernel kernel) {
		this.kernel = kernel;
	}
		
	@Override
	protected Action getBestAction(Turn player) throws NoActionFoundException {
		if(actions.size()<1) {
			throw new NoActionFoundException("No pawn found");
		}
		Action ret = null;
		double best = 0;
		for(Action a : actions.keySet())
			if(player.equals(Turn.WHITE)) {
				if(actions.get(a) >= best) {
					ret = a;
					best = actions.get(a);
				}
			} else {
				if(actions.get(a) <= best) {
					ret = a;
					best = actions.get(a);
				}
			}
		return ret;
	}

	@Override
	protected Action findAction(State state) throws NoActionFoundException {
		update(state);
		this.leafs.clear();
		this.actions.clear();
		
		expandDeep(root, 0);
		
		System.out.println(leafs.size());
						
		return getBestAction(state.getTurn());		
	}
	
	@Override
	public void update(State state) {
		for(Node child : this.root.getChildren())
			if(child.getState().equals(state)) {
				this.root = child;
				child.setRoot(true);
				return;
			}
		this.root = new Node(null, null, state);
	}
	
	private void expand(Node node) {
		State state = node.getState();
		Turn player = state.getTurn();
		List<int[]> pawns;
		if(player.equals(Turn.WHITE))
			pawns = getPawns(state, Pawn.WHITE);
		else if(player.equals(Turn.BLACK))
			pawns = getPawns(state, Pawn.BLACK);
		else {
			leafs.add(node);
			return;
		}
		
		for(int[] pos : pawns)
			node.addAllChild(tryAllAction(pos[0], pos[1], state, node));
	}
	
	// depth first with maximum depth
	private void expandDeep(Node node, int depth) {
		System.out.println(depth);
		if(depth == this.depth) return;
		
		if(node.isLeaf()) expand(node);
		
		for(Node n : node.getChildren())
			expandDeep(n, depth+1);
		return;		
	}

	private List<int[]> getPawns(State state, Pawn turn) {
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
//		System.out.println("Next Pawn:");
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
						State newState = this.getRules().checkMove(state.clone(), a);
						
//						System.out.println("\t\t" + a + " (" + ev + ")");
						
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
	
	@SuppressWarnings("unused")
	private double alphaBeta(Node node, int depth, double alpha, double beta, boolean maximize) {
		expand(node);
		
		if(depth == 0 || node.isLeaf())
			return kernel.evaluate(node);
		
		if(maximize) {
			double v = Double.NEGATIVE_INFINITY;
			for(Node child : node.getChildren()) {
				v = Math.max(v, alphaBeta(child, depth-1, alpha, beta, false));
				alpha = Math.max(alpha, v);
				if(beta <= alpha)
					break;
			}
			return v;
		} else {
			double v = Double.POSITIVE_INFINITY;
			for(Node child : node.getChildren()) {
				v = Math.min(v, alphaBeta(child, depth-1, alpha, beta, true));
				beta = Math.min(beta, v);
				if(beta <= alpha)
					break;
			}
			return v;
		}
	}
}