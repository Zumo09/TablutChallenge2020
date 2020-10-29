package giordani.tabzai.player.brain.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.ToDoubleFunction;

import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class KernelNaive implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, Double> params;
	private Random rnd;
	private double mutationProb;
	private double scale;
	private Map<String, ToDoubleFunction<State>> paramFun = Map.of(
			"black pawns", this::blackPawns,
			"white pawns", this::whitePawns,
			"king position", this::kingPosition,
			"black pawns near king", this::blackNearKing,
			"white pawns near king", this::whiteNearKing
			);
	
	private KernelNaive(Map<String, Double> params, double mutationProb, double scale) {
		this.mutationProb = mutationProb;
		this.scale = scale;
		this.rnd = new Random();
		this.params = new HashMap<>(params);
	}
	
	public KernelNaive(double mutationProb, double scale) {
		this.mutationProb = mutationProb;
		this.scale = scale;
		this.rnd = new Random();
		this.params = new HashMap<>();
		for(String key : paramFun.keySet())	
			params.put(key, 2*rnd.nextDouble() - 1);
	}
	
	//==============================================================
	// Features Functions
	//==============================================================
	
	private double whiteNearKing(State s) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double blackNearKing(State s) {
		// TODO Auto-generated method stub
		return 0;
	}

	private double kingPosition(State s) {
		// higher if closer to the edge
		for(int i=0; i<s.getBoard().length; i++)
			for(int j=0; j<s.getBoard()[0].length; j++)
				if(s.getPawn(i, j) == Pawn.KING) {
					return (i-4)*(i-4) + (j-4)*(j-4);
				}
		return 0; // should never reach that
	}

	private double whitePawns(State s) {
		return (double) s.getNumberOf(Pawn.WHITE);
	}

	private double blackPawns(State s) {
		return (double) s.getNumberOf(Pawn.BLACK);
	}
	
	//==============================================================
	// Public Functions
	//==============================================================
	

	//==============================================================
	// Public Functions
	//==============================================================
	
	public double evaluateState(State state) {
		double ev = 0;
		for(String key : params.keySet()) {
			ev += params.get(key) * paramFun.get(key).applyAsDouble(state);
		}
		return ev;
	}
	
	public KernelNaive copy() {
		return new KernelNaive(params, mutationProb, scale);
	}

	public KernelNaive mutate() {
		for(String p : params.keySet()) {
			if(rnd.nextDouble() < mutationProb) {
				double value = params.get(p) + scale * (rnd.nextDouble() - 0.5);
				params.put(p, value);
			}
		}
		return this;
	}
	
	public void save(String name) {
		Path p = Paths.get("kernels_naive" + File.separator + name);
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
}
