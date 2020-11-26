package giordani.tabzai.player;

import java.io.IOException;
import java.net.UnknownHostException;

import giordani.tabzai.player.brain.Brain;
import giordani.tabzai.player.brain.heuristic.Heuristic;
import it.unibo.ai.didattica.competition.tablut.client.TablutClient;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * @author zumo09
 *
 */
public class TabZAI extends TablutClient {
	private Brain brain;
	
	/**
	 * @param player
	 * @param name
	 * @param timeout
	 * @param ipAddress
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TabZAI(String player, String name, String filename, int timeout, String ipAddress, int gametype) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		this.brain = Brain.of(filename, timeout, gametype);
	}

	@Override
	public void run() {
		System.out.println("You are player " + this.getPlayer().toString() + "!");
		Action action;
		try {
			this.declareName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Turn other;
		Turn win;
		Turn lose;
		
		if(this.getPlayer().equals(Turn.WHITE)) {
			other = Turn.BLACK;
			win = Turn.WHITEWIN;
			lose = Turn.BLACKWIN;
		} else {
			other = Turn.WHITE;
			win = Turn.BLACKWIN;
			lose = Turn.WHITEWIN;
		}
		
		while (true) {
			try {
				this.read();

				if (this.getCurrentState().getTurn().equals(this.getPlayer())) {
					System.out.println(System.lineSeparator() + "Evaluating new position... ");
					action = brain.getAction(this.getCurrentState());
					System.out.println(brain.getInfo());
					System.out.println("Move choosen: From " + action.getFrom() + " to " + action.getTo());
					this.write(action);
				} else if (this.getCurrentState().getTurn().equals(other)) {
					System.out.println(System.lineSeparator() + "Waiting for your opponent move... ");
					brain.update(this.getCurrentState());
				} else if (this.getCurrentState().getTurn().equals(win)) {
					System.out.println("YOU WIN!");
					System.exit(0);
				} else if (this.getCurrentState().getTurn().equals(lose)) {
					System.out.println("YOU LOSE!");
					System.exit(0);
				} else if (this.getCurrentState().getTurn().equals(Turn.DRAW)) {
					System.out.println("DRAW!");
					System.exit(0);
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length < 2) {
			System.out.println("Too few arguments:");
			System.out.println("You must specify which player you are (WHITE or BLACK)");
			System.out.println("You must specify the ipAddress");
			System.exit(-1);
		}
		
		String player = args[0].trim();
		
		if(!(player.equalsIgnoreCase("white") || player.equalsIgnoreCase("black"))) {
			System.out.println("args[0] error:\nYou must specify which player you are (WHITE or BLACK)");
			System.exit(-1);
		}
		
		System.out.println("GLHF");
		
		TablutClient client = new TabZAI(player, "TabZAI", Heuristic.BEST, 60, args[1], 1);
		
		client.run();
		
		System.out.println("GGWP");
	}

}
