package giordani.tabzai.player.brain;

import java.time.Duration;

import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class BrainAbs implements Brain {
	private Duration timeout;
	private Game rules;
	private Action selected;
	private int depth;
	
	public BrainAbs(int timeout, int gametype, int depth) {
		this.depth = depth;
		this.timeout = Duration.ofSeconds(timeout-1);
		switch (gametype) {
		case 1:
			rules = new GameAshtonTablutNoLog(0, 0);
			break;
		case 2:
			rules = new GameModernTablut();
			break;
		case 3:
			rules = new GameTablut();
			break;
		case 4:
			rules = new GameAshtonTablutNoLog(0, 0);
			break;
			
		default:
			System.out.println("Error in game selection");
			System.exit(4);
		}
		
	}
	
	protected abstract Action findAction(State state);
	protected abstract Action getBestAction();

	@Override
	public Action getAction(State state) {
		selected = findAction(state);
		return this.selected;
	}

	@Override
	public int getTimeout() { return (int) timeout.toSeconds();}
	@Override
	public void setTimeout(int timeout) { this.timeout = Duration.ofSeconds(timeout-1);}
	@Override
	public Game getRules() { return rules;}
	
	public int getDepth() { return this.depth;}
	public void setDepth(int depth) { this.depth = depth;}

}
