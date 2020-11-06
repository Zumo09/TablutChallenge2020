/**
 * 
 */
package giordani.tabzai.player;

import java.io.IOException;
import java.net.UnknownHostException;

import giordani.tabzai.player.brain.Brain;
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
	public TabZAI(String player, String name, String kernel, int timeout, String ipAddress, int gametype) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		this.brain = Brain.of(kernel, timeout, gametype);
	}

	public TabZAI(String player, String name, String kernel) throws UnknownHostException, IOException {
		this(player, name, kernel, 60, "localhost", 1);
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
					action = brain.getAction(this.getCurrentState());
					System.out.println(brain);
					System.out.println("From " + action.getFrom() + " to " + action.getTo());
					this.write(action);
				} else if (this.getCurrentState().getTurn().equals(other)) {
					brain.update(this.getCurrentState());
					System.out.println(brain);
					System.out.println("Waiting for your opponent move... ");
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
		if (args.length != 3) {
			System.out.println("You must specify which player you are (WHITE or BLACK)!");
			System.out.println("You must specify the player name");
			System.out.println("You must specify the kernel file name");
			System.exit(-1);
		} 
		System.out.println("Selected " + args[0]);
		System.out.println("GLHF");
		
		TablutClient client = new TabZAI(args[0], args[1], args[2]);

		client.run();
		
		System.out.println("GGWP");
	}

}
