package giordani.tabzai.garbage;

import giordani.tabzai.player.brain.BrainDeepGen;
import giordani.tabzai.player.brain.NoActionFoundException;
import it.unibo.ai.didattica.competition.tablut.domain.StateTablut;

public class Test {

	public static void main(String[] args) {
		BrainDeepGen brain = new BrainDeepGen(0.5,0.5, 4);
		
		try {
			System.out.println(brain.getAction(new StateTablut()));
		} catch (NoActionFoundException e) {
		}

	}

}
