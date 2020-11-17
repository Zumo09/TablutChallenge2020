package giordani.tabzai.player.brain.heuristic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;
import java.util.function.ToDoubleFunction;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class HeuristicGen implements Heuristic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Double> params;
	private Map<String, ToDoubleFunction<State>> paramFun;
	private Random rnd;
			
	HeuristicGen(Map<String, Double> params) {
		this();
		this.params = new HashMap<>(params);
	}
	
	public HeuristicGen() {
		this.rnd = new Random();
		this.paramFun  = Map.of(
				"black pawns", this::blackPawns,
				"white pawns", this::whitePawns,
				"king position", this::kingPosition,
				"black near king", this::blackNearKing,
				"white near king", this::whiteNearKing,
				"player turn", this::turn,
				"white attacked", this::whiteAttacked,
				"black attacked", this::blackAttacked
				);
		this.params = new HashMap<>();
		for(String key : paramFun.keySet())	
			params.put(key, 2*getRandom().nextDouble() - 1); //random weight between -1 and 1
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
			return Double.POSITIVE_INFINITY;
		else if(s.getTurn().equals(Turn.BLACKWIN))
			return Double.NEGATIVE_INFINITY;
		else return 0;
	}
	
	private double whiteAttacked(State s) {
		int cont = 0;
		for(int[] pos : getPawns(s, Pawn.WHITE))
			cont += countClose(s, pos[0], pos[1], Pawn.BLACK);
		return cont;
	}
	
	private double blackAttacked(State s) {
		int cont = 0;
		for(int[] pos : getPawns(s, Pawn.BLACK))
			cont += countClose(s, pos[0], pos[1], Pawn.WHITE);
		return cont;
	}
	
	//==============================================================
	// Private Functions
	//==============================================================
	
	private Random getRandom() {
		return rnd;
	}
	
	@Override
	public HeuristicGen mutate(double mutationProb, double mutationScale) {
		for(String p : params.keySet()) {
			if(getRandom().nextDouble() < mutationProb) {
				double value = params.get(p) + mutationScale * 2 * (getRandom().nextDouble() - 0.5);
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
	private int countClose(State state, int row, int col, Pawn color) {
		List<Pawn> close = getCloserPawn(state, row, col);
		int cont = 0;
		for(Pawn p : close)
			if(p.equals(color))
				cont++;
		return cont;
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
	
	//==============================================================
	// Public Functions
	//==============================================================
	
	public HeuristicGen copy() {
		return new HeuristicGen(params);
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner(System.lineSeparator());
		sj.add("_______________________________________");
		sj.add("   Param name   |        Value");
		sj.add("----------------|----------------------");
		for(String key : this.getParams().keySet())
			sj.add(key.subSequence(0, Math.min(15, key.length())) + "\t| " + this.getParams().get(key));
		return sj.toString();
	}
	
	public Map<String, Double> getParams() {
		return params;
	}
	
	@Override
	public double evaluate(State state) {
		double ev = 0;
		for(String key : params.keySet()) {
			ev += params.get(key) * paramFun.get(key).applyAsDouble(state);
		}
		
		return ev;
	}

	@Override
	public List<Heuristic> crossover(Heuristic parent) {
		if(!(parent instanceof HeuristicGen))
			throw new IllegalArgumentException("Kernel type mismatch");
		
		HeuristicGen other = (HeuristicGen) parent;
		
		List<Heuristic> ret = new ArrayList<>();

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
		
		ret.add(new HeuristicGen(cross1));
		ret.add(new HeuristicGen(cross2));
		
		return ret;
	}
	
	//==============================================================
	// Input/Output
	//==============================================================
	
	@Override
	public void save(String name) {
		Path p = Paths.get(Heuristic.PATH + File.separator + name + Heuristic.EXT);
		String path = p.toAbsolutePath().toString();
		new File(Heuristic.PATH).mkdirs();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))){
			oos.writeObject(this.getParams());
			System.out.println("Saved " + name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		}
	}

	public static Heuristic load(String filename) {
		Path p = Paths.get(Heuristic.PATH + File.separator + filename + Heuristic.EXT);
		String path = p.toAbsolutePath().toString();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			@SuppressWarnings("unchecked")
			Map<String, Double> params = (Map<String, Double>) ois.readObject();
			System.out.println("Loading " + filename);
			return new HeuristicGen(params);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NOT LOADED");
			e.printStackTrace();
			System.exit(1);
		} 
		return null;
	}
}
