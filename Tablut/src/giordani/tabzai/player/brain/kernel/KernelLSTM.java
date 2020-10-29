package giordani.tabzai.player.brain.kernel;

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

public class KernelLSTM implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Random rnd;
	private double mutationProb;
	private double mutationScale;
	private int depth;
	
	private KernelLSTM(double mutationProb, double mutationScale) {
		this.mutationProb = mutationProb;
		this.mutationScale = mutationScale;
		this.rnd = new Random();
	}
	
	public KernelLSTM(double mutationProb, double mutationScale, int depth) {
		this.mutationProb = mutationProb;
		this.mutationScale = mutationScale;
		this.depth = depth;
		this.rnd = new Random();
	}
	
	//==============================================================
	// Getter and Setters
	//==============================================================
		
	private double getMutationProb() {
		return mutationProb;
	}
	
	private double getMutationScale() {
		return mutationScale;
	}
	
	public int getDepth() {
		return depth;
	}	
	
	//==============================================================
	// Private Functions
	//==============================================================
	
	private KernelLSTM mutate() {
		// TODO
		return this;
	}
		
	//==============================================================
	// Public Functions
	//==============================================================
	
	public KernelLSTM copy() {
		// TODO
		return new KernelLSTM(mutationProb, mutationScale);
	}

	public double evaluateState(State state) {
		double ev = 0;
		// TODO
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
		// TODO
		return "";
	}
	
	//==============================================================
	// Static Functions
	//==============================================================
	
	public static List<KernelLSTM> nextGeneration(KernelLSTM par1, KernelLSTM par2, int populationSize) {
		Random rnd = new Random();
		List<KernelLSTM> ret = new ArrayList<>();
		
		ret.add(par1.copy());
		ret.add(par2.copy());
		
		// TODO: Cross
		
		KernelLSTM child1 = new KernelLSTM(par1.getMutationProb(), par1.getMutationScale());
		KernelLSTM child2 = new KernelLSTM(par1.getMutationProb(), par1.getMutationScale());
		
		ret.add(child1.copy());
		ret.add(child2.copy());
		
		while(ret.size() < populationSize) {
			ret.add(child1.copy().mutate());
			ret.add(child2.copy().mutate());
		}
		
		return ret;
	}

	public static KernelLSTM load() {
		// TODO Auto-generated method stub
		return null;
	}

}
