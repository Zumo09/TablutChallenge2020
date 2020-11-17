package giordani.tabzai.player.brain.heuristic;

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


public interface Heuristic extends Serializable {
	public final String PATH = "heuristic";
	public final String BEST = "Kernel_149";
	public final String EXT = ".her";
	
	public Heuristic copy();
	public double evaluate(State state);
	public void save(String name);
	public Heuristic mutate(double mutationProb, double mutationScale);
	public List<Heuristic> crossover(Heuristic other);
	
	public static List<Heuristic> nextGeneration(List<Heuristic> parents, int populationSize, double mutationProb, double mutationScale) {
		if(parents.size() % 2 != 0 || parents.size() < 2)
			throw new IllegalArgumentException("Must be an even number of parents");
				
		List<Heuristic> ret = new ArrayList<>();
		
		for(Heuristic par : parents)
			ret.add(par.copy());
		
		for(int i=0; i<parents.size(); i+=2) {
			List<Heuristic> children = parents.get(i).crossover(parents.get(i+1));
			
			for(Heuristic child : children)
				ret.add(child.copy());
			
			while(ret.size() < (i+2)/parents.size() * populationSize)
				for(Heuristic child : children) {
					ret.add(child.copy().mutate(mutationProb, mutationScale));
					if(ret.size() == populationSize) return ret;
				}
		}
		
		return ret;
	}
	
	public static Heuristic load(String name) {
		if(name.equalsIgnoreCase("new"))
			return new HeuristicGen();
		Path p = Paths.get(Heuristic.PATH + File.separator + name + Heuristic.EXT);
		String path = p.toAbsolutePath().toString();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			@SuppressWarnings("unchecked")
			Map<String, Double> params = (Map<String, Double>) ois.readObject();
			System.out.println("Loading " + name);
			return new HeuristicGen(params);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NOT LOADED");
			e.printStackTrace();
			System.exit(1);
		} 
		return null;
	}
}
