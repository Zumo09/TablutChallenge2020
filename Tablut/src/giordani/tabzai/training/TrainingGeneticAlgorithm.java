package giordani.tabzai.training;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import giordani.tabzai.player.brain.Brain;
import giordani.tabzai.player.brain.BrainAlphaBeta;
import giordani.tabzai.player.brain.heuristic.Heuristic;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;

import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.exceptions.ActionException;
import it.unibo.ai.didattica.competition.tablut.exceptions.BoardException;
import it.unibo.ai.didattica.competition.tablut.exceptions.CitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingCitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingException;
import it.unibo.ai.didattica.competition.tablut.exceptions.DiagonalException;
import it.unibo.ai.didattica.competition.tablut.exceptions.OccupitedException;
import it.unibo.ai.didattica.competition.tablut.exceptions.PawnException;
import it.unibo.ai.didattica.competition.tablut.exceptions.StopException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ThroneException;
import it.unibo.ai.didattica.competition.tablut.domain.StateBrandub;

public class TrainingGeneticAlgorithm {
	private List<BrainAlphaBeta> population;
	private int matches;
	private int parents;
	private int gameChosen;
	private double mutationProb;
	private Logger loggSys;
	private String date;
	private String tag;
	
	public TrainingGeneticAlgorithm(String tag, List<BrainAlphaBeta> population, int matches, int timeout, int parents, int gameChosen, double mutationProb) {
		if(parents > population.size())
			parents = population.size();
		if(parents % 2 != 0 || parents < 2)
			parents = Math.max(2, parents + 1);
		if(matches<1)
			matches = 1;
		if(gameChosen<0 || gameChosen>4)
			gameChosen = 4;
		if(mutationProb < 0 || mutationProb >= 1)
			mutationProb = 0.99;
		if(timeout<0)
			timeout = 1;
		
		String msg = 
				"======================================\n" +
				"          Resuming TRAINING\n" +
				"======================================" + 
				"\npopulation = " + population.size() +
				"\nparents = " + parents +
				"\nmatches = " + matches + 
				"\ntimeout = " + timeout +
				"\nmutation probablitiy = " + mutationProb + 
				"\ngame rules = " + gameChosen +
				"\n======================================\n";
		
		System.out.println(msg);
		
		this.tag = tag;
		this.parents = parents;
		this.mutationProb = mutationProb;
		this.population = population;
		this.matches = matches;
		this.gameChosen = gameChosen;
		
		LocalDateTime ldt = LocalDateTime.now();
		this.date = ldt.getMonthValue() + "_" + ldt.getDayOfMonth()
					+ "_h" + ldt.getHour() + "_" + ldt.getMinute();
		
		String logs_folder = "Train_log";
		Path p = Paths.get(logs_folder + File.separator + tag + "_" + date +".txt");
		p = p.toAbsolutePath();
		String sysLogName = p.toString();
		loggSys = Logger.getLogger("SysLog");
		try {
			new File(logs_folder).mkdirs();
			File systemLog = new File(sysLogName);
			if (!systemLog.exists())
				systemLog.createNewFile();
			FileHandler fh = null;
			fh = new FileHandler(sysLogName, true);
			loggSys.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
			loggSys.setLevel(Level.FINE);
			loggSys.fine(msg);
		} catch (Exception e) {
			System.out.println("ERRORE");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public TrainingGeneticAlgorithm(String tag, int population, int matches, int timeout, int parents, int gameChosen, double mutationProb) {
		if(population % 2 != 0 || population < 8)
			population = Math.max(8, population + 1);
		if(parents > population)
			parents = population;
		if(parents % 2 != 0 || parents < 2)
			parents = Math.max(2, parents + 1);
		if(matches<1)
			matches = 1;
		if(gameChosen<0 || gameChosen>4)
			gameChosen = 4;
		if(mutationProb < 0 || mutationProb >= 1)
			mutationProb = 0.99;
		if(timeout<0)
			timeout = 1;
		
		String msg = 
				"======================================\n" +
				"          STARTING TRAINING\n" +
				"======================================" + 
				"\npopulation = " + population +
				"\nparents = " + parents +
				"\nmatches = " + matches + 
				"\ntimeout = " + timeout +
				"\nmutation probablitiy = " + mutationProb + 
				"\ngame rules = " + gameChosen +
				"\n======================================\n";
		
		System.out.println(msg);
		
		this.tag = tag;
		this.parents = parents;
		this.mutationProb = mutationProb;
		this.population = new ArrayList<>();
		while(this.population.size()<population)
			this.population.add(new BrainAlphaBeta(timeout));
		this.matches = matches;
		this.gameChosen = gameChosen;
		
		LocalDateTime ldt = LocalDateTime.now();
		this.date = ldt.getMonthValue() + "_" + ldt.getDayOfMonth()
					+ "_h" + ldt.getHour() + "_" + ldt.getMinute();
		
		String logs_folder = "Train_log";
		Path p = Paths.get(logs_folder + File.separator + tag + "_" + date +".txt");
		p = p.toAbsolutePath();
		String sysLogName = p.toString();
		loggSys = Logger.getLogger("SysLog");
		try {
			new File(logs_folder).mkdirs();
			File systemLog = new File(sysLogName);
			if (!systemLog.exists())
				systemLog.createNewFile();
			FileHandler fh = null;
			fh = new FileHandler(sysLogName, true);
			loggSys.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
			loggSys.setLevel(Level.FINE);
			loggSys.fine(msg);
		} catch (Exception e) {
			System.out.println("ERRORE");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
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
		
		TrainingGeneticAlgorithm trainer = new TrainingGeneticAlgorithm(tag, population, 
				matches, timeout, parents, gameChosen, mutationProb);	
				
		trainer.train();
	}
	
	private class Standing {
		private int player;
		private double score;
		private int wins;
		private int ties;
		private int loses;
		
		public Standing(int player, double score, int wins, int ties, int loses) {
			this.player = player;
			this.score = score;
			this.wins = wins;
			this.ties = ties;
			this.loses = loses;
		}
		
		public int getPlayer() {
			return player;
		}

		public double getScore() {
			return score;
		}

		public int getWins() {
			return wins;
		}

		public int getTies() {
			return ties;
		}

		public int getLoses() {
			return loses;
		}
	}
	
	private class TournamentResult {
		private double[][] table;
		
		public TournamentResult() {
			table = new double[population.size()][population.size()];
		}
		
		public void addResult(int white, int black, Turn result) {
			if(white == black)
				throw new IllegalArgumentException("BLACK = WHITE");
			double p;
			
			if(result.equals(Turn.WHITEWIN))
				p = 1;
			else if(result.equals(Turn.BLACKWIN))
				p = 0;
			else if(result.equals(Turn.DRAW))
				p = 0.5;
			else throw new IllegalArgumentException("INVALID RESULT");
			
			table[white][black] = p;
		}
		
		public double getPoints(int player) {
			double points = 0;
			for(int i=0; i<table.length; i++) 
				if(player != i) {
					points += table[player][i];
					points += 1 - table[i][player];
				}
			return points;
		}
		
		public int getWins(int player) {
			int wins = 0;
			for(int i=0; i<table.length; i++)
				if(player != i) {
					if(table[player][i] > 0.9)
						wins += 1;
					if(table[i][player] < 0.1)
						wins += 1;
				}
			return wins;
		}
		
		public int getTies(int player) {
			int ties = 0;
			for(int i=0; i<table.length; i++)
				if(player != i) {
					if(0.4 < table[player][i] && table[player][i] < 0.6)
						ties += 1;
					if(0.4 < table[i][player] && table[i][player] < 0.6)
						ties += 1;
				}
			return ties;
		}
		
		private int getLoses(int player) {
			int loses = 0;
			for(int i=0; i<table.length; i++)
				if(player != i) {
					if(table[player][i] < 0.1)
						loses += 1;
					if(table[i][player] > 0.9)
						loses += 1;
				}
			return loses;
		}
		
		private int getTotWhiteWins() {
			int ww = 0;
			for(int i=0; i<table.length; i++)
				for(int j=0; j<table.length; j++)
					if(i != j && table[i][j] > 0.9)
						ww++;
			return ww;
		}
		
		private int getTotBlackWins() {
			int bw = 0;
			for(int i=0; i<table.length; i++)
				for(int j=0; j<table.length; j++)
					if(i != j && table[i][j] < 0.1)
						bw++;
			return bw;
		}
		
		private int getTotTies() {
			int t = 0;
			for(int i=0; i<table.length; i++)
				for(int j=0; j<table.length; j++)
					if(i != j && 0.4 < table[i][j] && table[i][j] < 0.6)
						t++;
			return t;
		}
		
		
		public List<Standing> getRanking() {
			List<Standing> ranking = new ArrayList<>();
			for(int i=0; i<table.length; i++)
				ranking.add(new Standing(i, getPoints(i), getWins(i), getTies(i), getLoses(i)));
			Collections.sort(ranking, Comparator.comparing(Standing::getScore)
												.thenComparing(Standing::getWins)
												.thenComparing(Standing::getTies)
												.thenComparing(Standing::getLoses)
												.thenComparing(Standing::getPlayer));
			Collections.reverse(ranking);
			return ranking;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Results\t\n\\");
			for(int i=0; i<table.length; i++)
				sb.append("|\t  " + i);
			sb.append("\n");
			for(int i=0; i<table.length; i++) {
				sb.append(i);
				for(int j=0; j<table.length; j++)
					if(i != j)
						sb.append("|\t" + table[i][j]);
					else sb.append("|\t \\");
				sb.append("\n");
			}
			sb.append("\n\nRanking:\n");
			sb.append("\tP|\tS|\tW|\tT|\tL\n");
			for(Standing s : getRanking()) {
				sb.append("\t" + s.getPlayer());
				sb.append("|\t" + s.getScore());
				sb.append("|\t" + s.getWins());
				sb.append("|\t" + s.getTies());
				sb.append("|\t" + s.getLoses() + "\n");
			}
			sb.append("\n");
			sb.append("Win with White = " + getTotWhiteWins());
			sb.append("\nWin with Black = " + getTotBlackWins());
			sb.append("\nTies           = " + getTotTies());
			sb.append("\n\n");
			return sb.toString();
		}
	}
	
	

	public void train() {
		this.train(0);
	}
	
	public void train(int lastMatch) {
		Heuristic par;
		List<Heuristic> parents = new ArrayList<>();
		
		int matchCounter = 0;
		long start = System.nanoTime();
		
		for(int m=lastMatch; m<this.matches; m++) {
			loggSys.fine("===================\nNew Tournament " + m);
			System.out.println("===================\nNew Tournament " + m);
			TournamentResult results = new TournamentResult();
			for(int i=0; i<population.size(); i++)
				for(int j=0; j<population.size(); j++)
					if(i != j) {
						Turn res = match(m+"_"+i+"_"+j, population.get(i), population.get(j));
						results.addResult(i, j, res);
						matchCounter++;
					}
			
			loggSys.fine(results.toString());
			System.out.println(results.toString());
			List<Standing> ranking = results.getRanking();
			parents.clear();
			for(int i=0; i<this.parents; i++) {
				par = population.get(ranking.get(i).getPlayer()).getHeuristic().copy();
				par.save(tag + i + "_" + m);
				parents.add(par.copy());
				loggSys.fine("Par1:\n" + par);
			}
			List<Heuristic> newGen = Heuristic.nextGeneration(parents, population.size(), this.mutationProb);
			for(int i=0; i<population.size(); i++) {
				population.get(i).setHeuristic(newGen.get(i));
				population.get(i).resetRoot();
			}
		}
				
		long stop = System.nanoTime();
		Duration d = Duration.ofNanos(stop-start);
		
		for(Heuristic p : parents)
			System.out.println(p);
		
		long h = d.toHours();
		int m = d.toMinutesPart();
		int s = d.toSecondsPart();
		int mill = d.toMillisPart();
		
		String msg = "==========================\n"
				+ "==========================\n"
				+ "Match simulated: " + matchCounter 
				+ "\nin " + (h<10?"0":"") + h + ":" 
						  + (m<10?"0":"") + m + ":"
						  + (s<10?"0":"") + s + "."
						  + (mill<100?"0":"") 
						  + (mill<10?"0":"") + mill
				+ "\n" + d.toMillis()/matchCounter + " ms/match";
		
		System.out.println(msg);
		loggSys.fine(msg);
	}

	private Turn match(String id, Brain white, Brain black) {
		Game game = null;
		State state = null;
		switch (this.gameChosen) {
			case 1:
				state = new StateTablut();
				game = new GameTablut(-1);
				break;
			case 2:
				state = new StateTablut();
				game = new GameModernTablut(-1);
				break;
			case 3:
				state = new StateBrandub();
				game = new GameTablut(-1);
				break;
			case 4:
				state = new StateTablut();
				state.setTurn(Turn.WHITE);
//				System.out.println("Starting turn: " + state.getTurn());
				game = new GameAshtonTablutNoLog(state, 0, -1);
				break;
			default:
				System.out.println("Error in game selection");
				System.exit(4);
		}

		Action move = null;
		System.out.println("\n==================\nNew Match " + id);
		loggSys.fine("==================\nMatch " + id);
		int moves = 0;
		State newState;
		// GAME CYCLE
		while (moves < 1000) {
			// RECEIVE MOVE
//			System.out.println("=================\nMove " + moves + ". Turn: " + state.getTurn());
			if(state.getTurn().equals(Turn.WHITE)) {
				move = white.getAction(state.clone());
				System.out.println("White: " + white.getInfo() + move);
				loggSys.fine("White:\n" + white.getInfo() + move);
			}
			else if(state.getTurn().equals(Turn.BLACK)) {
				move = black.getAction(state.clone());
				System.out.println("Black: " + black.getInfo() + move);
				loggSys.fine("Black:\n" + black.getInfo() + move);
			}
			else {
//				loggSys.fine("==================\nEndgame: " + state.getTurn());
				System.out.println("-----------------\nENDGAME (" + state.getTurn() + ")");
				return state.getTurn();
			}
			moves++;
			try {
				newState = game.checkMove(state.clone(), move);
			} catch (BoardException | ActionException | StopException | 
					PawnException | DiagonalException | ClimbingException |
					ThroneException | OccupitedException |
					ClimbingCitadelException | CitadelException e) {
				//System.out.println(e.getMessage());
				if(state.getTurn().equals(Turn.BLACK)) {
//					loggSys.fine("==================\nToo many error for Black");
//						System.out.println("=================\nToo many error for Black");
					return Turn.WHITEWIN;
				}
				else if(state.getTurn().equals(Turn.WHITE)) {
//					loggSys.fine("==================\nToo many error for White");
//						System.out.println("=================\nToo many error for White");
					return Turn.BLACKWIN;
				}
				else {
					// should never reach this code
					loggSys.fine("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR in " + id);
					System.out.println("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR in " + id);
					return Turn.DRAW;
				}
			}
			state = newState;
			
		}
		
//		loggSys.fine("==================\nGame " + id + " ended for too many moves ("+moves+")");
//		System.out.println("\nGame ended for too many moves ("+moves+")");
		return Turn.DRAW;
	}
}

