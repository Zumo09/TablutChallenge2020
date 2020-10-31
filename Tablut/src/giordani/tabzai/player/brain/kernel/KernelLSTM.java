package giordani.tabzai.player.brain.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import giordani.tabzai.player.brain.Node;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public class KernelLSTM extends KernelAbs {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public KernelLSTM(double mutationProb, double mutationScale, int depth) {
		super(mutationProb, mutationScale, depth);
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
		return new KernelLSTM(getMutationProb(), getMutationScale(), getDepth());
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
	
	@Override
	public double evaluateStateTrace(List<Node> trace) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Kernel> crossover(Kernel other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double evaluate(Node node) {
		// TODO Auto-generated method stub
		return 0;
	}
}
