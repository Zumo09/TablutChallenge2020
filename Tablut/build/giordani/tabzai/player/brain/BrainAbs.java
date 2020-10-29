package giordani.tabzai.player.brain;

import giordani.tabzai.training.GameAshtonTablut;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

/**
 * @author zumo0
 * 
 * 	Choose an Action and, if the timeouts occour, it gives back the best solution found.
 *  https://stackoverflow.com/questions/19456313/simple-timeout-in-java
 */
public abstract class BrainAbs implements Brain {

	private int timeout;
	private Game rules;
	private Action selected;
	public BrainAbs(int timeout, int gametype) {
		this.timeout=timeout - 1;
		switch (gametype) {
		case 1:
			rules = new GameAshtonTablut(99, 0);
			break;
		case 2:
			rules = new GameModernTablut();
			break;
		case 3:
			rules = new GameTablut();
			break;
			
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}
	}
	
	protected abstract Action getBestAction(Turn player) throws NoActionFoundException;

	protected abstract Action findAction(State state) throws NoActionFoundException;
		

	@Override
	public Action getAction(State state) throws NoActionFoundException {
		/*
		System.out.println("-----------------\nPROVA1");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("-----------------\nPROVA_RUUN");
				selected = findAction(state);
				System.out.println("-----------------\nPROVA_RUUUN_END");
			}
		});
		System.out.println("-----------------\nPROVA2" + this.timeout);
		try {
			int counter = 0;
			System.out.println("-----------------\nPROVA_c");
			while (counter < this.timeout && t.isAlive()) {
				System.out.println("-----------------\nPROVA");
				Thread.sleep(1000);
				counter++;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (t.isAlive()) {
			System.out.println("-----------------\nPROVA_ALIVE");
			this.selected = getBestAction();
		}
		
		 */
		
		selected = findAction(state);
		return this.selected;
	}

	public int getTimeout() {
		return timeout;
	}
	
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Game getRules() {
		return rules;
	}

}
