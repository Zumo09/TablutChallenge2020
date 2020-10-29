package giordani.tabzai.player.brain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;

public class BrainDummy extends BrainAbs {

	public BrainDummy(int timeout, int gametype) {
		super(timeout, gametype);
	}
	
	public BrainDummy() {
		super(60, 1);
	}

	@Override
	protected Action getBestAction(Turn t) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Action findAction(State state) {
		List<int[]> pawns;
		if(state.getTurn().equals(State.Turn.WHITE))
			pawns = getPawns(state, Pawn.WHITE);
		else if(state.getTurn().equals(State.Turn.BLACK))
			pawns = getPawns(state, Pawn.BLACK);
		else throw new IllegalArgumentException("No action allowed: Endgame");
		Collections.shuffle(pawns);
		if(pawns.size()<1)
			System.out.println("No pawn found");
		for(int[] pos : pawns) {
			Optional<Action> a = tryAllAction(pos[0], pos[1], state);
			if(a.isPresent())
				return a.get();
		}
		throw new IllegalArgumentException("No Action Found");
		
		
	}
	
	private List<int[]> getPawns(State state, Pawn turn) {
		List<int[]> ret = new ArrayList<>();
		for(int i=0; i<state.getBoard().length; i++)
			for(int j=0; j<state.getBoard()[0].length; j++) {
				Pawn p = state.getPawn(i, j);
				int[] pos = {i, j};
				if(p.equals(turn)) {
					ret.add(pos);
				} else if(p.equals(Pawn.KING) && turn.equals(Pawn.WHITE)) 
					ret.add(pos);
			}
		return ret;
	}

	private Optional<Action> tryAllAction(int row, int col, State state) {
		for(int i=8; i>0; i--) {
		int[] rcp = {row, col+i};
		int[] rcm = {row, col-i};
		int[] crp = {row+i, col};
		int[] crm = {row-i, col};
		int[][] newPos = {rcp, rcm, crp, crm};
		List<int[]> newPosList = Arrays.asList(newPos);
		Collections.shuffle(newPosList);
		for(int[] pos : newPosList)
			try {
				Action a = new Action(state.getBox(row, col), state.getBox(pos[0], pos[1]), state.getTurn());
				this.getRules().checkMove(state, a);
				return Optional.of(a);
			} catch(Exception e) {
			}
		}
		return Optional.empty();
	}



}
