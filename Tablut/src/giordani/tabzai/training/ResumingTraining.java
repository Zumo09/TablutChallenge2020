package giordani.tabzai.training;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import giordani.tabzai.player.brain.BrainAlphaBeta;
import giordani.tabzai.player.brain.heuristic.Heuristic;
import giordani.tabzai.player.brain.heuristic.HeuristicNN;

public class ResumingTraining {

	public static void main(String[] args) {		
		int population = 8;
		int matches = 2;
		int gameChosen = 4;
		int parents = 2;
		double mutationProb = 0.5;
		int timeout = 1;
		String tag = "Test";
		
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();
		
		options.addOption("n","name", true, "String: tag to recognize the training");
		options.addOption("p","population", true, "integer: even number of population in each generation, min 4, default 100");
		options.addOption("f","parents", true, "integer: even number of parents in each generation, min 4, default 100");
		options.addOption("t", "timeout", true, "integer: timeout for the move search");
		options.addOption("m", "match", true, "integer: number of match, default 100");
		options.addOption("o","mutation_probability", true, "double: probabilty of changing a paramenter in the kernel (0<=x<=1)");
		options.addOption("r","game_rules", true, "game rules must be an integer; 1 for Tablut, 2 for Modern, 3 for Brandub, 4 for Ashton; default: 4");
		
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Genetic Training", options);

		try {
			CommandLine cmd = parser.parse( options, args );
			if (cmd.hasOption("p")) {
				try {
					population = Integer.parseInt(cmd.getOptionValue("p"));
					if(population < 4){
						System.out.println("At least population of 4");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
					if(population % 2 != 0){
						System.out.println("Population must be even number");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				} catch (NumberFormatException e) {
					System.out.println("Number format is not correct! (p)" + cmd.getOptionValue("p"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if (cmd.hasOption("f")) {
				try {
					parents = Integer.parseInt(cmd.getOptionValue("f"));
					if(parents < 2){
						System.out.println("At least 2 parents");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
					if(parents % 2 != 0){
						System.out.println("Population must be even number");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				} catch (NumberFormatException e) {
					System.out.println("Number format is not correct! (f)" + cmd.getOptionValue("f"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if (cmd.hasOption("m")) {
				try {
					matches = Integer.parseInt(cmd.getOptionValue("m"));
					if(matches < 1) {
						System.out.println("At least 1 match");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				} catch (NumberFormatException e) {
					System.out.println("Number format is not correct! (m)" + cmd.getOptionValue("m"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if (cmd.hasOption("t")) {
				try {
					timeout = Integer.parseInt(cmd.getOptionValue("t"));
					if(timeout < 1) {
						System.out.println("At least 1 second");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				} catch (NumberFormatException e) {
					System.out.println("Number format is not correct! (t)" + cmd.getOptionValue("t"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if(cmd.hasOption("r")) {
				try{
					gameChosen = Integer.parseInt(cmd.getOptionValue("r"));
					if (gameChosen < 0 || gameChosen > 4){
						System.out.println("Game format not allowed!");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				}catch (NumberFormatException e){
					System.out.println("The game format is not correct!" + cmd.getOptionValue("r"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if(cmd.hasOption("o")) {
				try{
					mutationProb = Double.parseDouble(cmd.getOptionValue("o"));
					if (mutationProb < 0 || mutationProb > 1) {
						System.out.println("Mutation Probability must be 0<=x<=1");
						formatter.printHelp("Genetic Training", options);
						System.exit(1);
					}
				}catch (NumberFormatException e){
					System.out.println("The mutationProb format is not correct!" + cmd.getOptionValue("o"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if(cmd.hasOption("n")) {
				tag = cmd.getOptionValue("n");
			}

		} catch (ParseException exp) {
			System.out.println( "Unexpected exception:" + exp.getMessage());
			System.exit(2);
		}
		
		List<BrainAlphaBeta> newPopulation = new ArrayList<>();
		
		while(newPopulation.size()<population)
			newPopulation.add(new BrainAlphaBeta(timeout));
		
		Heuristic par1 = HeuristicNN.load(args[0]);
		Heuristic par2 = HeuristicNN.load(args[1]);
		List<Heuristic> par = List.of(par1, par2);
		List<Heuristic> newGen = Heuristic.nextGeneration(par, newPopulation.size(), mutationProb);
		for(int i=0; i<newPopulation.size(); i++) {
			newPopulation.get(i).setHeuristic(newGen.get(i));
			newPopulation.get(i).resetRoot();
		}
		
		TrainingGeneticAlgorithm trainer = new TrainingGeneticAlgorithm(tag, newPopulation, 
				matches, timeout, parents, gameChosen, mutationProb);
		
		int lastMatch = 0;
		try{
			lastMatch = Integer.parseInt(args[2]) + 1;
		} catch (NumberFormatException e){
			System.out.println("NAME NOT OK");
			System.exit(1);
		}
		System.out.println("Resuming from match " + lastMatch);
		trainer.train(lastMatch);
	}

}
