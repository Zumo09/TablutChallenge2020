package giordani.tabzai.garbage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import giordani.tabzai.player.brain.BrainAbs;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.exceptions.*;

/*
 * During training his kernel will be mutated and changed.
 * At runtime it only has some wrapper function
 * Useful because only Kernel can be saved
 */

public class BrainGeneticNaive extends BrainAbs {
	private KernelNaive kernel;
	private int gen;
	Turn player;
	SortedMap<Double, Action> actions;

	public BrainGeneticNaive(String player, int timeout, int gametype) {
		super(timeout, gametype);
		this.kernel = new KernelNaive(0.5, 0.5);
		this.gen = 0;
		if(player.equalsIgnoreCase("white"))
			this.player = Turn.WHITE;
		else this.player = Turn.BLACK;
		this.actions = new TreeMap<>();
	}
	
	public BrainGeneticNaive(String player) {
		this(player, 60, 1);
	}

	public KernelNaive getKernel() {
		return kernel;
	}

	public void setKernel(KernelNaive kernel) {
		this.kernel = kernel;
	}
	
	public Turn getPlayer() {
		return this.player;
	}
	
	@Override
	public String toString() {
		return player.toString()+gen;
	}
	
	public void nextGen() {
		this.gen++;
	}

	@Override
	protected Action getBestAction(Turn player) {
		if(player.equals(Turn.WHITE))
			return actions.get(actions.lastKey());
		else return actions.get(actions.firstKey());
	}

	@Override
	protected Action findAction(State state) {
		List<int[]> pawns;
		
		if(state.getTurn().equals(State.Turn.WHITE) && this.player.equals(State.Turn.WHITE))
			pawns = getPawns(state, Pawn.WHITE);
		else if(state.getTurn().equals(State.Turn.BLACK) && this.player.equals(State.Turn.BLACK))
			pawns = getPawns(state, Pawn.BLACK);
		else throw new IllegalArgumentException("No action allowed: Endgame or wrong player");
		
		Collections.shuffle(pawns);
		
		if(pawns.size()<1)
			System.out.println("No pawn found");
//		else System.out.println("Found " + pawns.size() + " pawns");
		actions.clear();
		for(int[] pos : pawns)
			tryAllAction(pos[0], pos[1], state);
		
//		System.out.println("\n\nMoves Found : " + this.actions.size() + "\n\n");
		
		return getBestAction(player);		
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

	private void tryAllAction(int row, int col, State state) {
//		System.out.println("Next Pawn:");
		for(int i=state.getBoard().length; i>0; i--) {
			//System.out.println("\ti = "+i);
			int[] rcp = {row, col+i};
			int[] rcm = {row, col-i};
			int[] crp = {row+i, col};
			int[] crm = {row-i, col};
			int[][] l = {rcp, rcm, crp, crm};
			List<int[]> newPosList = Arrays.asList(l);
			Collections.shuffle(newPosList);
			for(int[] pos : newPosList) {
				//System.out.println("\t\tpos = " + pos[0] + ", " + pos[1]);
				if(pos[0]>=0 && pos[0]<state.getBoard().length && pos[1]>=0 && pos[1]<state.getBoard().length) {
					//System.out.println("Trying...");
					try {
						Action a = new Action(state.getBox(row, col), state.getBox(pos[0], pos[1]), state.getTurn());
						State newState = this.getRules().checkMove(state.clone(), a);
						// evaluate the new state: The best move will be that one that have the best evaluation
						// Positive favorites White
						// Negative favorites Black
						// 0 favorites draw
						double ev = kernel.evaluateState(newState);
//						System.out.println("\t\t" + a + " (" + ev + ")");
						
						actions.put(ev, a);
					} catch(ActionException | BoardException | CitadelException | 
							ClimbingCitadelException | ClimbingException | 
							DiagonalException | OccupitedException | PawnException | 
							StopException | ThroneException e) {
						//System.out.println(e.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void update(State state) {
		// TODO Auto-generated method stub
		
	}
}
