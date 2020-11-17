package giordani.tabzai.player.brain;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public interface Brain {

	public Action getAction(State state);
	public void update(State state);
	public String getInfo();
	
	public static Brain of(String kernel, int timeout, int gametype) {
		return new BrainAlphaBeta(kernel, timeout, gametype);
	}

}
