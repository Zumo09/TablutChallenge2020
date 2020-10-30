package giordani.tabzai.garbage;

//import java.io.File;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.Date;
//import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import giordani.tabzai.player.brain.NoActionFoundException;
import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;

import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateBrandub;

public class TrainingGeneticAlghoritmNaive {
	BrainGeneticNaive white;
	BrainGeneticNaive black;
	int children;
	int matches;
	int gameChosen;
	boolean enableGui;
	//Logger loggSys;
	
	public TrainingGeneticAlghoritmNaive(int children, int matches, int gameChosen, boolean enableGui) {
		white = new BrainGeneticNaive("white");
		black = new BrainGeneticNaive("black");
		this.enableGui = enableGui;
		this.children = children;
		this.matches = matches;
		this.gameChosen = gameChosen;
		
		// gestire GUI
		/*
		String logs_folder = "train_logs" + File.separator + + new Date().getTime()%100000;
		Path p = Paths.get(logs_folder + File.separator + "Training_systemLog.txt");
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
		}*/
	}
	
	/*
	 * Partendo da 2 Brain:
	 * si fa una partita,
	 * chi perde genera <children> figli, sue mutazioni
	 * si fanno le tot partite
	 * Se i figli le riperdono tutte, si prende il migliore e si muta di nuovo
	 * Altrimenti si tiene il migliore dei mutati e si muta l'avversario
	 * così fino ad una certa
	 */

	public static void main(String[] args) {
		int children = 10;
		int matches = 10;
		int gameChosen = 4;
		boolean enableGui = false;
		

		CommandLineParser parser = new DefaultParser();

		Options options = new Options();

		options.addOption("c","children", true, "integer: number of children given by the loser, default 10");
		options.addOption("m", "match", true, "integer: number of maximum match, default 1000");
		options.addOption("r","game rules", true, "game rules must be an integer; 1 for Tablut, 2 for Modern, 3 for Brandub, 4 for Ashton; default: 4");
		options.addOption("g","enableGUI", false, "enableGUI if option is present");

		HelpFormatter formatter = new HelpFormatter();
		//formatter.printHelp("Genetic Training", options);

		try {
			CommandLine cmd = parser.parse( options, args );
			if (cmd.hasOption("c")) {
				try {
					children = Integer.parseInt(cmd.getOptionValue("c"));
					if(children < 1){
						System.out.println("At least 1 children per generation");
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
						formatter.printHelp("java Server", options);
						System.exit(1);
					}
				}catch (NumberFormatException e){
					System.out.println("The game format is not correct!" + cmd.getOptionValue("g"));
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
		
		System.out.println("children per generation = " + children +
				"\nmatches = " + matches + "\ngame rules = " + gameChosen + "\nGUI = " + enableGui);
		
		TrainingGeneticAlghoritmNaive trainer = new TrainingGeneticAlghoritmNaive(children, matches, gameChosen, enableGui);	
		
		trainer.train();
	}
	
	private class MatchResult {
		/*
		 * Class to bring all the useful info to determine which is the best player
		 */
		public Turn winner;
		public int moves;
		public MatchResult(Turn winner, int moves) {
			this.winner = Turn.valueOf(winner.name());
			this.moves = moves;
		}
	}
	
	public void train() {
		BrainGeneticNaive toChange;
		Turn expected;
		KernelNaive parent;
		KernelNaive child;
		// first match
		MatchResult result = match("0_0_F");
		System.out.println("First match: result = " + result.winner + " in " + result.moves + "moves");
		
		if(result.winner.equals(Turn.WHITEWIN)) {
			toChange = black;
			expected = Turn.BLACKWIN;
		}
		else {
			toChange = white; // default: if draw modify white
			expected = Turn.WHITEWIN;
		}
		// Training Loop
		
		for(int m=1; m<this.matches+1; m++) {
			Map<KernelNaive, MatchResult> good = new HashMap<>();
			Map<KernelNaive, MatchResult> draw = new HashMap<>();
			Map<KernelNaive, MatchResult> bad = new HashMap<>();
			
			toChange.nextGen();
			parent = toChange.getKernel().copy();
			for(int c=1; c<this.children+1; c++) {
				child = parent.copy().mutate();
				toChange.setKernel(child);
				MatchResult r = match(m+"_"+c+"_"+toChange);
				String msg = m+"_"+c+"_"+toChange+": result = " + r.winner + " in " + r.moves + "moves";
				System.out.println(msg);
				//loggSys.fine(msg);
				
				if(r.winner.equals(expected))
					good.put(child, r);
				else if(r.winner.equals(Turn.DRAW))
					draw.put(child, r);
				else bad.put(child, r);
			}
			
			String msg;
			if(!good.isEmpty()) {
				child = bestKernel(good);
				toChange.setKernel(child.copy());
				if(expected.equals(Turn.BLACKWIN)) {
					toChange = white;
					expected = Turn.WHITEWIN;
				} else {
					toChange = black;
					expected = Turn.BLACKWIN;
				}
				msg = "=============\nSuccess -> next changing " + toChange;
			}
			else if(!draw.isEmpty()) {
				child = bestKernel(draw);
				toChange.setKernel(child.copy());
				msg = "=============\nDraw -> next changing " + toChange;
			}
			else {
				child = bestKernel(bad);
				toChange.setKernel(child.copy());
				msg = "=============\nAll lose -> next changing " + toChange;
			}
			System.out.println(msg);
			//loggSys.fine(msg);
		}
		
		//white.getKernel().save(Kernel.WHITE);
		//black.getKernel().save(Kernel.BLACK);
	}

	private MatchResult match(String id) {
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
		//loggSys.fine("==================\nNew Match " + id);
		int moves = 0;
		State newState;
		// GAME CYCLE
		while (moves < 1000) {
			// RECEIVE MOVE
//			System.out.println("=================\nMove " + moves + ". Turn: " + state.getTurn());
			if(state.getTurn().equals(Turn.WHITE)) {
				try {
					move = white.getAction(state.clone());
				} catch (NoActionFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println("-----------------\nWhite (" + move + ")");
			}
			else if(state.getTurn().equals(Turn.BLACK)) {
				try {
					move = black.getAction(state.clone());
				} catch (NoActionFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				System.out.println("-----------------\nBlack (" + move + ")");
			}
			else {
				//loggSys.fine("==================\nEndgame: " + state.getTurn());
//				System.out.println("-----------------\nENDGAME (" + state.getTurn() + ")");
				return new MatchResult(state.getTurn(), moves);
			}
			moves++;
			try {
				newState = game.checkMove(state.clone(), move);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				if(state.getTurn().equals(Turn.BLACK)) {
					//loggSys.warning("==================\nToo many error for Black");
					System.out.println("=================\nToo many error for Black");
					return new MatchResult(Turn.WHITEWIN, moves);
				}
				else if(state.getTurn().equals(Turn.WHITE)) {
					//loggSys.warning("==================\nToo many error for White");
					System.out.println("=================\nToo many error for White");
					return new MatchResult(Turn.BLACKWIN, moves);
				}
				else {
					// should never reach this code
					//loggSys.warning("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
					System.out.println("=*=*=*=*=*=*=*=*=*=*\nENDGAME ERROR");
					return new MatchResult(Turn.DRAW, moves);
				}
			}
			state = newState;
		}
		
		//loggSys.warning("==================\nGame ended for too many moves ("+moves+")");
		System.out.println("\nGame ended for too many moves ("+moves+")");
		return new MatchResult(Turn.DRAW, moves);
	}

	private KernelNaive bestKernel(Map<KernelNaive, MatchResult> results) {
		int min = 100000;
		KernelNaive best = null;
		for(Map.Entry<KernelNaive, MatchResult> entry : results.entrySet())
			if(entry.getValue().moves < min) {
				min = entry.getValue().moves;
				best = entry.getKey();
			}
		return best;
	}
}
