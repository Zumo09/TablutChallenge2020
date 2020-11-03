package giordani.tabzai.player.brain;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public interface Brain {

	public Action getAction(State state);
	public abstract void update(State state);
	public int getTimeout();
	public void setTimeout(int timeout);
	public Game getRules();
	
	public static Brain of(int timeout, int gametype) {
		// TODO Auto-generated method stub
		return null;
	}

}
