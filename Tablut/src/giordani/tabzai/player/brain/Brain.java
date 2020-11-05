package giordani.tabzai.player.brain;

import giordani.tabzai.player.brain.kernel.Kernel;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public interface Brain {

	public Action getAction(State state);
	public abstract void update(State state);
	
	public static Brain of(int timeout, int gametype) {
		return new BrainAlphaBeta(Kernel.BEST, timeout, gametype, 5);
	}

}
