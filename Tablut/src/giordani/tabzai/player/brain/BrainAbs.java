package giordani.tabzai.player.brain;

import java.time.Duration;

import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

/**
 * @author zumo0
 * 
 * 	Choose an Action and, if the timeouts occour, it gives back the best solution found.
 *  https://stackoverflow.com/questions/19456313/simple-timeout-in-java
 */
public abstract class BrainAbs {

	private Duration timeout;
	private Game rules;
	private State state;
	private Action selected;
	public BrainAbs(int timeout, int gametype) {
		this.timeout = Duration.ofSeconds(timeout-1);
		switch (gametype) {
		case 1:
			rules = new GameAshtonTablutNoLog(0, 16);
			state = new StateTablut();
			break;
		case 2:
			rules = new GameModernTablut();
			state = new StateTablut();
			break;
		case 3:
			rules = new GameTablut();
			state = new StateTablut();
			break;
		case 4:
			rules = new GameAshtonTablutNoLog(0, 16);
			state = new StateTablut();
			break;
			
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}
	}
	
	protected abstract Action getBestAction(Turn player) throws NoActionFoundException;

	protected abstract Action findAction(State state) throws NoActionFoundException;

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
		
		 
		final 
		ExecutorService executor = Executors.newSingleThreadExecutor();

		final Future<String> handler = executor.submit(new Callable() {
		    @Override
		    public String call() throws Exception {
		        return requestDataFromModem();
		    }
		});

		try {
		    handler.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
		    handler.cancel(true);
		}

		executor.shutdownNow();
		*/
		selected = findAction(state);
		return this.selected;
	}
	
	public abstract void update(State state);

	public int getTimeout() {
		return  (int) timeout.toSeconds();
	}
	
	public void setTimeout(int timeout) {
		this.timeout = Duration.ofSeconds(timeout-1);
	}

	public Game getRules() {
		return rules;
	}

	public State getState() {
		return state;
	}

}
