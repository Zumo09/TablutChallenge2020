package giordani.tabzai.player.brain;

import giordani.tabzai.training.GameAshtonTablutNoLog;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.Game;
import it.unibo.ai.didattica.competition.tablut.domain.GameModernTablut;
import it.unibo.ai.didattica.competition.tablut.domain.GameTablut;
import it.unibo.ai.didattica.competition.tablut.domain.State;

public abstract class BrainAbs implements Brain {
	private Game rules;
	private int depth;
	private long timeout;
	private long stopTime;
	
	public BrainAbs(int timeout, int gametype) {
		this.resetDepth();
		this.timeout = (long) ((0.75*timeout) * 1000);  // -1 not enough, it runs in server timeout
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
		this.startTimer();
		this.update(state);
		this.resetDepth();
		try{
			while(true) {
				this.incrementDepth();
				this.searchAction();
			}
		} catch(TimeOutException e) {}
		return this.getBestAction();
	}
	
	@Override
	public abstract void update(State state);
	
	@Override
	public abstract String getInfo();
	protected abstract void searchAction() throws TimeOutException;
	protected abstract Action getBestAction();

	public Game getRules() 			{ return rules;		}
	public int getDepth() 			{ return this.depth;}
	public void resetDepth() 		{ this.depth = 2;	}
	public void incrementDepth()	{ this.depth++;		}
	
	private void startTimer() {
		this.stopTime = System.currentTimeMillis() + this.timeout;
	}	
	
	/**
	 * Method to call to interrupt the search at the occurence of the timeout
	 * @throws TimeOutException
	 */
	protected void checkTimeout() throws TimeOutException { 
		if(this.stopTime < System.currentTimeMillis())
			throw new TimeOutException();
	}
}
