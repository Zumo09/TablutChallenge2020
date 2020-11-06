package giordani.tabzai.player.brain.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unibo.ai.didattica.competition.tablut.domain.State;


public interface Kernel extends Serializable {
	public final String PATH = "kernels";
	public final String BEST = "Kernel_149";
	public final String EXT = ".ker";
	
	public Kernel copy();
	public double evaluate(State state);
	public void save(String name);
	public double getMutationScale();
	public double getMutationProb();
	public Kernel mutate();
	public List<Kernel> crossover(Kernel other);
	
	public static List<Kernel> nextGeneration(List<Kernel> parents, int populationSize) {
		if(parents.size() % 2 != 0 || parents.size() < 2)
			throw new IllegalArgumentException("Must be an even number of parents");
				
		List<Kernel> ret = new ArrayList<>();
		
		for(Kernel par : parents)
			ret.add(par.copy());
		
		for(int i=0; i<parents.size(); i+=2) {
			List<Kernel> children = parents.get(i).crossover(parents.get(i+1));
			
			for(Kernel child : children)
				ret.add(child.copy());
			
			while(ret.size() < (i+2)/parents.size() * populationSize)
				for(Kernel child : children) {
					ret.add(child.copy().mutate());
					if(ret.size() == populationSize) return ret;
				}
		}
		
		return ret;
	}
	
	public static Kernel load(String name) {
		Path p = Paths.get(Kernel.PATH + File.separator + name + Kernel.EXT);
		String path = p.toAbsolutePath().toString();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			@SuppressWarnings("unchecked")
			Map<String, Double> params = (Map<String, Double>) ois.readObject();
			return new KernelGen(params);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NOT LOADED");
			e.printStackTrace();
			System.exit(1);
		} 
		return null;
	}
	
	public static Kernel of(double mutationProb, double mutationScale) {
		return new KernelGen(mutationProb, mutationScale);
	}
}
