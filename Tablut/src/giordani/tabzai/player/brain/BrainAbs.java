package giordani.tabzai.player.brain;

import java.time.Duration;

import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class BrainAbs implements Brain {
	private Game rules;
	private int depth;
	protected Timer timer;
	
	public BrainAbs(int timeout, int gametype) {
		this.resetDepth();
		this.timer = new Timer(Duration.ofSeconds(timeout));
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
		this.timer.start();
		this.update(state);
		this.resetDepth();
		System.out.println("Depth: " + this.depth);
		do {
			this.incrementDepth();
			System.out.println("Depth: " + this.depth);
			this.searchAction();
		} while(!this.timer.timeout() && this.depth < 4);
		return this.getBestAction();
	}
	
	@Override
	public abstract void update(State state);
	
	@Override
	public abstract String getInfo();
	protected abstract void searchAction();
	protected abstract Action getBestAction();

	public Game getRules() 			{ return rules;		}
	public int getDepth() 			{ return this.depth;}
	public void resetDepth() 		{ this.depth = 1;	}
	
	public void incrementDepth()	{ this.depth++;		}
	
	public class Timer {
		private long duration;
		private long end;
		
		public Timer(Duration duration) {
			this.duration = duration.toMillis();
		}
		
		public void start() {
			this.end = System.currentTimeMillis() + this.duration;
		}
		
		public boolean timeout() {
			return this.end < System.currentTimeMillis();
		}
	}

}
