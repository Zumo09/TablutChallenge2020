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

	@Override
	public Action getAction(State state) {
		/*
		 * Gestire la crescita e la diminuzione della depth in funzione del timeout
		 * Trovare un modo per partire con una certa profondità e nel caso incrementarla o diminuirla
		 */
		selected = findAction(state);
		return this.selected;
	}
	
	@Override
	public abstract void update(State state);
	
	protected abstract Action findAction(State state);
	protected abstract Action getBestAction();

	public int getTimeout() 			{ return (int) timeout.toSeconds();}
	public void setTimeout(int timeout) { this.timeout = Duration.ofSeconds(timeout-1);}
	public Game getRules() 				{ return rules;}
	public int getDepth() 				{ return this.depth;}
	public void setDepth(int depth) 	{ this.depth = depth;}

}
