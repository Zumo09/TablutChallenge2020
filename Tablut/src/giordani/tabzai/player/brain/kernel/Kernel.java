package giordani.tabzai.player.brain.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import giordani.tabzai.player.brain.Node;


public interface Kernel extends Serializable {
	public final String PATH = "kernels";
	
	public int getDepth();
	public Kernel copy();
	public double evaluateStateTrace(List<Node> trace);
	public double evaluate(Node node);
	public void save(String name);
	public double getMutationScale();
	public double getMutationProb();
	public Kernel mutate();
	public List<Kernel> crossover(Kernel other);
	
	public static List<Kernel> nextGeneration(List<Kernel> parents, int populationSize) {
		if(parents.size() % 2 != 0)
			throw new IllegalArgumentException("Must be an even number of parents");
		
		List<Kernel> ret = new ArrayList<>();
		
		for(Kernel par : parents)
			ret.add(par.copy());
		
		for(int i=0; i<parents.size(); i+=2) {
			List<Kernel> children = parents.get(i).crossover(parents.get(i+1));
			
			for(Kernel child : children)
				ret.add(child.copy());
			
			while(ret.size() < populationSize)
				for(Kernel child : children)
					ret.add(child.copy().mutate());
		}
		
		return ret;
	}
	
	public static Kernel load(String Name) {
		return null;
	}
	public static Kernel of(double mutationProb, double mutationScale, int depth) {
		return new KernelLSTM(mutationProb, mutationScale, depth);
	}
}
