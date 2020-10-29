package giordani.tabzai.player.brain;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

/**
 * @author zumo0
 * It's the class that calculate the Action.
 * There can be different kind of brains, for Black and White, and a timeout can be set.
 */
public interface Brain {
	
	public static Brain of(String player, int timeout) {
		return of(player, timeout, 1);
	}
	/*
	 * Factory Method: choose what kind of brain to be given
	 */
	public static Brain of(String player, int timeout, int gametype) {
		Brain brain = new BrainDummy();
		brain.setTimeout(timeout);
		return brain;
	}
	
	public Action getAction(State state) throws NoActionFoundException;
	
	public void setTimeout(int timeout);
}
