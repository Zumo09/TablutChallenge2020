package giordani.tabzai.player.brain.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.ToDoubleFunction;

import giordani.tabzai.player.brain.Node;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class KernelGen extends KernelAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Double> params;
	private Map<String, ToDoubleFunction<State>> paramFun = Map.of(
			"black pawns", this::blackPawns,
			"white pawns", this::whitePawns,
			"king position", this::kingPosition,
			"black pawns near king", this::blackNearKing,
			"white pawns near king", this::whiteNearKing,
			"turn", this::turn
			);
	
	private KernelGen(Map<String, Double> params, double mutationProb, double mutationScale, int depth) {
		super(mutationProb, mutationScale, depth);
		this.params = new HashMap<>(params);
	}
	
	public KernelGen(double mutationProb, double mutationScale, int depth) {
		super(mutationProb, mutationScale, depth);
		this.params = new HashMap<>();
		for(String key : paramFun.keySet())	
			params.put(key, 2*getRandom().nextDouble() - 1); //random weight between -1 and 1
	}
	
	public KernelGen() {
		super(0,0, 1);
		this.params = Map.of("king position", 20.579720069300826, 
							"black pawns", -7.6624105876364474, 
							"white pawns", -10.829288637073532, 
							"black pawns near king", 1.8824658618107062,
							"turn", 6.949590191093299,
							"white pawns near king", -5.229963655418505);
	}
	
		
	//==============================================================
	// Features Functions
	//==============================================================
	
	private double whiteNearKing(State s) {
		for(int i=0; i<s.getBoard().length; i++)
			for(int j=0; j<s.getBoard()[0].length; j++)
				if(s.getPawn(i, j) == Pawn.KING)
					return countClose(s, i, j, Pawn.WHITE);
		throw new IllegalArgumentException("INVALID STATE: NO KING");
	}

	private double blackNearKing(State s) {
		for(int i=0; i<s.getBoard().length; i++)
			for(int j=0; j<s.getBoard()[0].length; j++)
				if(s.getPawn(i, j) == Pawn.KING) 
					return countClose(s, i, j, Pawn.BLACK);
		throw new IllegalArgumentException("INVALID STATE: NO KING");
	}

	private double kingPosition(State s) {
		// higher if closer to the edge
		for(int i=0; i<s.getBoard().length; i++)
			for(int j=0; j<s.getBoard()[0].length; j++)
				if(s.getPawn(i, j) == Pawn.KING)
					return (i-4)*(i-4) + (j-4)*(j-4);
		throw new IllegalArgumentException("INVALID STATE: NO KING");
	}

	private double whitePawns(State s) {
		return s.getNumberOf(Pawn.WHITE);
	}

	private double blackPawns(State s) {
		return s.getNumberOf(Pawn.BLACK);
	}
	
	private double turn(State s) {
		if(s.getTurn().equals(Turn.WHITE))
			return 1;
		else if(s.getTurn().equals(Turn.BLACK))
			return -1;
		else if(s.getTurn().equals(Turn.WHITEWIN))
			return 100;
		else if(s.getTurn().equals(Turn.BLACKWIN))
			return -100;
		else return 0;
	}
	
	//==============================================================
	// Private Functions
	//==============================================================
	
	@Override
	public KernelGen mutate() {
		for(String p : params.keySet()) {
			if(getRandom().nextDouble() < getMutationProb()) {
				double value = params.get(p) + getMutationScale() * 2 * (getRandom().nextDouble() - 0.5);
				params.put(p, value);
			}
		}
		return this;
	}
	
	private List<Pawn> getCloserPawn(State state, int row, int col) {
		List<Pawn> close = new ArrayList<>();
		
		int[][] c = {{row, col+1}, 
				     {row, col-1}, 
				     {row+1, col}, 
				     {row-1, col}};
		
		for(int[] pos : c) {
			if(pos[0]>=0 && pos[0]<state.getBoard().length && 
					pos[1]>=0 && pos[1]<state.getBoard().length) {
				close.add(state.getPawn(pos[0], pos[1]));
			}
		}
		
		return close;
	}
	/*
	 * Count the Pawn of the passed color close to the position row,col, in the state state
	 */
	private double countClose(State state, int row, int col, Pawn black) {
		List<Pawn> close = getCloserPawn(state, row, col);
		int cont = 0;
		for(Pawn p : close)
			if(p.equals(Pawn.WHITE))
				cont++;
		return cont;
	}
	
	//==============================================================
	// Public Functions
	//==============================================================
	
	public KernelGen copy() {
		return new KernelGen(params, getMutationProb(), getMutationScale(), getDepth());
	}

	public double evaluateState(State state) {
		double ev = 0;
		for(String key : params.keySet()) {
			ev += params.get(key) * paramFun.get(key).applyAsDouble(state);
		}
		return ev;
	}
	
	@Override
	public String toString() {
		return params.toString();
	}
	
	public Map<String, Double> getParams() {
		return params;
	}

	@Override
	public double evaluateStateTrace(List<Node> trace) {
		return evaluate(trace.get(trace.size()-1));
	}

	@Override
	public double evaluate(Node node) {
		return evaluateState(node.getState());
	}

	@Override
	public List<Kernel> crossover(Kernel parent) {
		if(!(parent instanceof KernelGen))
			throw new IllegalArgumentException("Kernel type mismatch");
		
		KernelGen other = (KernelGen) parent;
		
		List<Kernel> ret = new ArrayList<>();

		Map<String, Double> cross1 = new HashMap<>();
		Map<String, Double> cross2 = new HashMap<>();
		
		for(String key : this.getParams().keySet()) {
			if(getRandom().nextDouble() < 0.5) {
				cross1.put(key, this.getParams().get(key));
				cross2.put(key, other.getParams().get(key));
			} else {
				cross1.put(key, other.getParams().get(key));
				cross2.put(key, this.getParams().get(key));
			}
		}
		
		ret.add(new KernelGen(cross1, getMutationProb(), getMutationScale(), getDepth()));
		ret.add(new KernelGen(cross2, getMutationProb(), getMutationScale(), getDepth()));
		
		return ret;
	}
}
