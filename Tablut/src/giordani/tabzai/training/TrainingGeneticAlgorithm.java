package giordani.tabzai.training;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

import giordani.tabzai.player.brain.BrainDeepGen;
import giordani.tabzai.player.brain.NoActionFoundException;
import giordani.tabzai.player.brain.kernel.Kernel;
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
	List<BrainDeepGen> population;
	int matches;
	int gameChosen;
	boolean enableGui;
	Logger loggSys;
	
	public TrainingGeneticAlgorithm(int population, int matches, int gameChosen, double mutationProb, double mutationScale, int depth, boolean enableGui) {
		this.enableGui = enableGui;
		this.population = new ArrayList<>();
		while(this.population.size()<population)
			this.population.add(new BrainDeepGen(mutationProb, mutationScale, depth));
		this.matches = matches;
		this.gameChosen = gameChosen;
		
		// gestire GUI
		
		String logs_folder = "train_logs";
		Path p = Paths.get(logs_folder + File.separator + new Date().getTime() + "_training_systemLog.txt");
		p = p.toAbsolutePath();
		String sysLogName = p.toString();
		loggSys = Logger.getLogger("SysLog");
		try {
			new File(logs_folder).mkdirs();
			File systemLog = new File(sysLogName);
			if (!systemLog.exists()) {
				System.out.println("Creating new file");
				systemLog.createNewFile();
			}
			FileHandler fh = null;
			fh = new FileHandler(sysLogName, true);
			loggSys.addHandler(fh);
			fh.setFormatter(new SimpleFormatter());
			loggSys.setLevel(Level.FINE);
			loggSys.fine("======================================\n"
					+ "\t\tSTARTING TRAINING\n"
					+ "======================================");
		} catch (Exception e) {
			System.out.println("ERRORE");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		int population = 16;
		int matches = 50;
		int gameChosen = 4;
		int depth = 4;
		double mutationProb = 0.2;
		double mutationScale = 10;
		boolean enableGui = false;
		
		CommandLineParser parser = new DefaultParser();

		Options options = new Options();

		options.addOption("p","population", true, "integer: even number of population in each generation, min 4, default 100");
		options.addOption("m", "match", true, "integer: number of match, default 100");
		options.addOption("d", "depth", true, "integer: number of moves that the brain watch ahead, default 4");
		options.addOption("o","mutation_probability", true, "double: probabilty of changing a paramenter in the kernel (0<=x<=1)");
		options.addOption("s", "mutation_scale", true, "double: scale of updating in kernel mutation");
		options.addOption("r","game rules", true, "game rules must be an integer; 1 for Tablut, 2 for Modern, 3 for Brandub, 4 for Ashton; default: 4");
		options.addOption("g","enable_GUI", false, "enableGUI if option is present (not implemented)");

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
					System.out.println("Number format is not correct! (c)" + cmd.getOptionValue("c"));
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
			if(cmd.hasOption("s")) {
				try{
					mutationScale = Double.parseDouble(cmd.getOptionValue("s"));
				}catch (NumberFormatException e){
					System.out.println("The mutationScale format is not correct!" + cmd.getOptionValue("s"));
					formatter.printHelp("Genetic Training", options);
					System.exit(1);
				}
			}
			if(cmd.hasOption("g")) {
				enableGui=true;
			}

		} catch (ParseException exp) {
			System.out.println( "Unexpected exception:" + exp.getMessage());
			System.exit(2);
		}
		
		System.out.println("==========================" +
						"\npopulation = " + population +
						"\nmatches = " + matches + 
						"\nmutation probablitiy = " + mutationProb + 
						"\nmutation scale = " + mutationScale + 
						"\ngame rules = " + gameChosen + 
						"\nGUI = " + enableGui +
						"\n==========================" );
		
		TrainingGeneticAlgorithm trainer = new TrainingGeneticAlgorithm(population, 
				matches, gameChosen, mutationProb, mutationScale, depth, enableGui);	
		
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
				if(player != i ) {
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
		List<TournamentResult> history = new ArrayList<>();
		List<List<Kernel>> kernelHistory = new ArrayList<>();
		Kernel par1 = null;
		Kernel par2 = null;
		List<Kernel> parents = new ArrayList<>();
		
		int matchCounter = 0;
		long start = System.nanoTime();
		
		for(int m=0; m<this.matches; m++) {
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
			
			history.add(results);
			loggSys.fine(results.toString());
			System.out.println(results.toString());
			List<Standing> ranking = results.getRanking();
			parents.clear();
			par1 = population.get(ranking.get(0).getPlayer()).getKernel().copy();
			par2 = population.get(ranking.get(1).getPlayer()).getKernel().copy();
			loggSys.fine("Par1:\n" + par1 + "\nPar2:\n" + par2);
			parents.add(par1.copy()); parents.add(par2.copy());
			List<Kernel> newGen = Kernel.nextGeneration(parents, population.size());
			List<Kernel> kh = new ArrayList<>();
			for(int i=0; i<population.size(); i++) {
				kh.add(population.get(i).getKernel().copy());
				population.get(i).setKernel(newGen.get(i));
			}
			kernelHistory.add(kh);
		}
		
		System.out.println("Par 1 =\n" + par1);
		System.out.println("Par 2 =\n" + par2);
		
		long stop = System.nanoTime();
		Duration d = Duration.ofNanos(stop-start);
		
		System.out.println("==========================\n"
				+ "==========================\n"
				+ "Match simulated: " + matchCounter 
				+ "\nin " + d.toHours() + ":" + d.toMinutesPart() 
				+ ":" + d.toSecondsPart() + "." + d.toMillisPart()
				+ "\n" + d.toMillis()/matchCounter + " ms/match");
		
//		par1.save("Kernel_1");
//		par2.save("Kernel_2");
	}

	private Turn match(String id, BrainDeepGen white, BrainDeepGen black) {
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
//		System.out.println("\n%%%%%%%%%%%%%%%%\nNew Match " + id);
		loggSys.fine("==================\nMatch " + id);
		int moves = 0;
		State newState;
		// GAME CYCLE
		while (moves < 1000) {
			try {
				// RECEIVE MOVE
	//			System.out.println("=================\nMove " + moves + ". Turn: " + state.getTurn());
				if(state.getTurn().equals(Turn.WHITE)) {
					move = white.getAction(state.clone());
	//				System.out.println("-----------------\nWhite (" + move + ")");
				}
				else if(state.getTurn().equals(Turn.BLACK)) {
					move = black.getAction(state.clone());
	//				System.out.println("-----------------\nBlack (" + move + ")");
				}
				else {
					loggSys.fine("==================\nEndgame: " + state.getTurn());
	//				System.out.println("-----------------\nENDGAME (" + state.getTurn() + ")");
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
						loggSys.fine("==================\nToo many error for Black");
//						System.out.println("=================\nToo many error for Black");
						return Turn.WHITEWIN;
					}
					else if(state.getTurn().equals(Turn.WHITE)) {
						loggSys.fine("==================\nToo many error for White");
//						System.out.println("=================\nToo many error for White");
						return Turn.BLACKWIN;
					}
					else {
						// should never reach this code
						loggSys.fine("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
						System.out.println("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
						return Turn.DRAW;
					}
				}
				state = newState;
			} catch(NoActionFoundException e) {
				//System.out.println(e.getMessage());
				if(state.getTurn().equals(Turn.BLACK)) {
					loggSys.fine("==================\nBLACK RUN OUT OF MOVES");
//					System.out.println("=================\nBLACK RUN OUT OF MOVES");
					return Turn.WHITEWIN;
				}
				else if(state.getTurn().equals(Turn.WHITE)) {
					loggSys.fine("==================\nWHITE RUN OUT OF MOVES");
//					System.out.println("=================\nWHITE RUN OUT OF MOVES");
					return Turn.BLACKWIN;
				}
				else {
					// should never reach this code
					loggSys.fine("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
					System.out.println("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
					return Turn.DRAW;
				}
			}
		}
		
		loggSys.fine("==================\nGame ended for too many moves ("+moves+")");
//		System.out.println("\nGame ended for too many moves ("+moves+")");
		return Turn.DRAW;
	}
}

