package giordani.tabzai.player.brain.heuristic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.unibo.ai.didattica.competition.tablut.domain.State;

/**
 * Interface of the Heuristic function to evaluate a state
 * Implementing classes must also have a static method load(String filename) to handle the
 * loading of an existing set of parameter from the file obtained by the method save(name)
 * 
 * @author zumo0
 *
 */
public interface Heuristic extends Serializable {
	public final String PATH = "heuristic";
	public final String BEST = "Kernel_149";
	public final String EXT = ".her";
	
	public Heuristic copy();
	public double evaluate(State state);
	public void save(String name);
	public Heuristic mutate(double mutationProb, double mutationScale);
	public List<Heuristic> crossover(Heuristic other);
	
	/**
	 * perform crossover between pairs of parents, and return a new population with:
	 * 	- All the parents
	 * 	- All the crossover
	 *  - Mutation of the crossover	
	 * @param parents : List of parents. They must be an even number
	 * @param populationSize : size of the population to return. It must be at least twice the number of parents
	 * @param mutationProb : the probability to mutate a parameter
	 * @param mutationScale : the scale that multiply the random number that will be summed to the parameter to modify
	 * @return A list of new Heuristic that must be put into the existing brains for the new Tournament
	 */
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
	
	/**
	 * Return an heuristic from the file <filename>. if <filename> == "new" it will return a new Heuristic
	 * @param name
	 * @return
	 */
	public static Heuristic of(String filename) {
		if(filename.equalsIgnoreCase("new"))
			return new HeuristicGen();
		return HeuristicGen.load(filename);
	}
}
