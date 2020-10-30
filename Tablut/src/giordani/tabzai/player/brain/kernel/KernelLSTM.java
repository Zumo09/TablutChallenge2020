package giordani.tabzai.player.brain.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import it.unibo.ai.didattica.competition.tablut.domain.State;

public class KernelLSTM implements KernelDeep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	private Random rnd;
	private double mutationProb;
	private double mutationScale;
	private int depth;
	
	public KernelLSTM(double mutationProb, double mutationScale) {
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
	
	@Override
	public double getMutationProb() {
		return mutationProb;
	}
	
	@Override
	public double getMutationScale() {
		return mutationScale;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}	
	
	//==============================================================
	// Private Functions
	//==============================================================
	
	@Override
	public KernelLSTM mutate() {
		// TODO
		return this;
	}
		
	//==============================================================
	// Public Functions
	//==============================================================
	
	@Override
	public KernelLSTM copy() {
		// TODO
		return new KernelLSTM(mutationProb, mutationScale);
	}

	public double evaluateState(State state) {
		double ev = 0;
		// TODO
		return ev;
	}
	
	@Override
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
	
//	@Override
//	public double evaluateStateTrace(List<Node> trace) {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public List<KernelDeep> crossover(KernelDeep other) {
		// TODO Auto-generated method stub
		return null;
	}

}
