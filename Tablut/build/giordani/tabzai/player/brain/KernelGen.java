package giordani.tabzai.player.brain;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class KernelGen implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Double> params;
	private Random rnd;
	private double mutationProb;
	private double mutationScale;
	private Map<String, ToDoubleFunction<State>> paramFun = Map.of(
			"black pawns", this::blackPawns,
			"white pawns", this::whitePawns,
			"king position", this::kingPosition,
			"black pawns near king", this::blackNearKing,
			"white pawns near king", this::whiteNearKing,
			"turn", this::turn
			);
	
	private KernelGen(Map<String, Double> params, double mutationProb, double mutationScale) {
		this.mutationProb = mutationProb;
		this.mutationScale = mutationScale;
		this.rnd = new Random();
		this.params = new HashMap<>(params);
	}
	
	public KernelGen(double mutationProb, double mutationScale) {
		this.mutationProb = mutationProb;
		this.mutationScale = mutationScale;
		this.rnd = new Random();
		this.params = new HashMap<>();
		for(String key : paramFun.keySet())	
			params.put(key, 2*rnd.nextDouble() - 1); //random weight between -1 and 1
	}
	
	//==============================================================
	// Getter and Setters
	//==============================================================
	
	private Map<String, Double> getParams() {
		return params;
	}
	
	private double getMutationProb() {
		return mutationProb;
	}
	
	private double getMutationScale() {
		return mutationScale;
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
	
	private KernelGen mutate() {
		for(String p : params.keySet()) {
			if(rnd.nextDouble() < mutationProb) {
				double value = params.get(p) + mutationScale * 2 * (rnd.nextDouble() - 0.5);
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
		return new KernelGen(params, mutationProb, mutationScale);
	}

	public double evaluateState(State state) {
		double ev = 0;
		for(String key : params.keySet()) {
			ev += params.get(key) * paramFun.get(key).applyAsDouble(state);
		}
		return ev;
	}

	public void save(String name) {
		Path p = Paths.get("kernels_genetic" + File.separator + name);
		String path = p.toAbsolutePath().toString();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))){
			oos.writeObject(this);
			System.out.println("Saved " + name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		}
	}
	
	@Override
	public String toString() {
		return params.toString();
	}
	
	public static List<KernelGen> nextGeneration(KernelGen par1, KernelGen par2, int populationSize) {
		Random rnd = new Random();
		List<KernelGen> ret = new ArrayList<>();
		
		ret.add(par1.copy());
		ret.add(par2.copy());
		
		Map<String, Double> cross1 = new HashMap<>();
		Map<String, Double> cross2 = new HashMap<>();
		
		for(String key : par1.getParams().keySet()) {
			if(rnd.nextDouble() < 0.5) {
				cross1.put(key, par1.getParams().get(key));
				cross2.put(key, par2.getParams().get(key));
			} else {
				cross1.put(key, par2.getParams().get(key));
				cross2.put(key, par1.getParams().get(key));
			}
		}
		
		KernelGen child1 = new KernelGen(cross1, par1.getMutationProb(), par1.getMutationScale());
		KernelGen child2 = new KernelGen(cross2, par1.getMutationProb(), par1.getMutationScale());
		
		ret.add(child1.copy());
		ret.add(child2.copy());
		
		while(ret.size() < populationSize) {
			ret.add(child1.copy().mutate());
			ret.add(child2.copy().mutate());
		}
		
		return ret;
	}
}
