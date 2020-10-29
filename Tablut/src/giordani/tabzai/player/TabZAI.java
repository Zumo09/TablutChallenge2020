/**
 * 
 */
package giordani.tabzai.player;

import java.io.IOException;
import java.net.UnknownHostException;

import giordani.tabzai.player.brain.Brain;
import giordani.tabzai.player.brain.BrainGenetic;
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
	public TabZAI(String player, String name, int timeout, String ipAddress, int gametype) throws UnknownHostException, IOException {
		super(player, name, timeout, ipAddress);
		this.brain = BrainGenetic.load(timeout, gametype);
	}

	/**
	 * @param player
	 * @param name
	 * @param timeout
	 * @param ipAddress
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TabZAI(String player, String name, int timeout, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, timeout, ipAddress, 1);
	}

	/**
	 * @param player
	 * @param name
	 * @param timeout
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TabZAI(String player, String name, int timeout) throws UnknownHostException, IOException {
		this(player, name, timeout, "localhost");
	}

	/**
	 * @param player
	 * @param name
	 * @param ipAddress
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TabZAI(String player, String name, String ipAddress) throws UnknownHostException, IOException {
		this(player, name, 60, ipAddress);
	}
	
	/**
	 * @param player
	 * @param name
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public TabZAI(String player, String name) throws UnknownHostException, IOException {
		this(player, name, 60, "localhost");
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
					System.out.println("From " + action.getFrom() + " to " + action.getTo());
					this.write(action);
				} else if (this.getCurrentState().getTurn().equals(other)) {
					brain.update(this.getCurrentState());
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
		
//		if (this.getPlayer() == Turn.WHITE) {
//			while (true) {
//				try {
//					this.read();
//
//					if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {
//						action = brain.getAction(this.getCurrentState());
//						System.out.println("From " + action.getFrom() + " to " + action.getTo());
//						this.write(action);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
//						brain.update(this.getCurrentState());
//						System.out.println("Waiting for your opponent move... ");
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITEWIN)) {
//						System.out.println("YOU WIN!");
//						System.exit(0);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACKWIN)) {
//						System.out.println("YOU LOSE!");
//						System.exit(0);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.DRAW)) {
//						System.out.println("DRAW!");
//						System.exit(0);
//					}
//
//				} catch (Exception e) {
//					e.printStackTrace();
//					System.exit(1);
//				}
//			}
//		} else {
//			while (true) {
//				try {
//					this.read();
//					
//					if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACK)) {
//						action = brain.getAction(this.getCurrentState());
//						System.out.println("From " + action.getFrom() + " to " + action.getTo());
//						this.write(action);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITE)) {
//						brain.update(this.getCurrentState());
//						System.out.println("Waiting for your opponent move... ");
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.WHITEWIN)) {
//						System.out.println("YOU LOSE!");
//						System.exit(0);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.BLACKWIN)) {
//						System.out.println("YOU WIN!");
//						System.exit(0);
//					} else if (this.getCurrentState().getTurn().equals(StateTablut.Turn.DRAW)) {
//						System.out.println("DRAW!");
//						System.exit(0);
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//					System.exit(1);
//				}
//			}
//		}

	}

	/**
	 * @param args
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public static void main(String[] args) throws UnknownHostException, IOException {
		if (args.length == 0) {
			System.out.println("You must specify which player you are (WHITE or BLACK)!");
			System.exit(-1);
		}
		System.out.println("Selected " + args[0]);

		TablutClient client = new TabZAI(args[0], "TabZAI_" + args[0].toUpperCase());

		client.run();

	}

}
