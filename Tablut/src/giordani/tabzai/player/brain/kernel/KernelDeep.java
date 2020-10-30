package giordani.tabzai.player.brain.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public interface KernelDeep extends Serializable {
	public int getDepth();
	public KernelDeep copy();
	//public double evaluateStateTrace(List<Node> trace);
	public void save(String name);
	public double getMutationScale();
	public double getMutationProb();
	public KernelDeep mutate();
	public List<KernelDeep> crossover(KernelDeep other);
	
	public static List<KernelDeep> nextGeneration(List<KernelDeep> parents, int populationSize) {
		if(parents.size() % 2 != 0)
			throw new IllegalArgumentException("Must be an even number of parents");
		
		List<KernelDeep> ret = new ArrayList<>();
		
		for(KernelDeep par : parents)
			ret.add(par.copy());
		
		for(int i=0; i<parents.size(); i+=2) {
			List<KernelDeep> children = parents.get(i).crossover(parents.get(i+1));
			
			for(KernelDeep child : children)
				ret.add(child.copy());
			
			while(ret.size() < populationSize)
				for(KernelDeep child : children)
					ret.add(child.copy().mutate());
		}
		
		return ret;
	}
	
	public static KernelDeep load(String Name) {
		return null;
	}
	public static KernelDeep of(double mutationProb, double mutationScale, int depth) {
		return new KernelLSTM(mutationProb, mutationScale, depth);
	}
}
