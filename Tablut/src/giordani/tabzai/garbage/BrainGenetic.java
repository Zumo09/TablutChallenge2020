package giordani.tabzai.garbage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import giordani.tabzai.player.brain.BrainAbs;
import giordani.tabzai.player.brain.NoActionFoundException;
import it.unibo.ai.didattica.competition.tablut.domain.Action;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;
import it.unibo.ai.didattica.competition.tablut.domain.State.Turn;
import it.unibo.ai.didattica.competition.tablut.exceptions.ActionException;
import it.unibo.ai.didattica.competition.tablut.exceptions.BoardException;
import it.unibo.ai.didattica.competition.tablut.exceptions.CitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingCitadelException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ClimbingException;
import it.unibo.ai.didattica.competition.tablut.exceptions.DiagonalException;
import it.unibo.ai.didattica.competition.tablut.exceptions.OccupitedException;
import it.unibo.ai.didattica.competition.tablut.exceptions.PawnException;
import it.unibo.ai.didattica.competition.tablut.exceptions.StopException;
import it.unibo.ai.didattica.competition.tablut.exceptions.ThroneException;

public class BrainGenetic extends BrainAbs {
	private KernelGen kernel;
	private SortedMap<Double, Action> actions;
	private Turn player;

	public BrainGenetic(int timeout, int gametype, double mutationProb, double mutationScale) {
		super(timeout, gametype);
		this.kernel = new KernelGen(mutationProb, mutationScale);
		this.actions = new TreeMap<>();
	}

	public BrainGenetic(double mutationProb, double mutationScale) {
		this(60, 1, mutationProb, mutationScale);
	}
	
	public BrainGenetic(int timeout, int gametype) {
		super(timeout, gametype);
		this.kernel = new KernelGen();
		this.actions = new TreeMap<>();
	}

	public KernelGen getKernel() {
		return kernel;
	}

	public void setKernel(KernelGen kernel) {
		this.kernel = kernel;
	}
	
	@Override
	protected Action getBestAction(Turn player) throws NoActionFoundException {
		if(actions.size()<1) {
			throw new NoActionFoundException("No pawn found");
		}
		if(player.equals(Turn.WHITE))
			return actions.get(actions.lastKey());
		else return actions.get(actions.firstKey());
	}

	@Override
	protected Action findAction(State state) throws NoActionFoundException {
//		System.out.println("\n\t\t\tWhite "+ state.getNumberOf(Pawn.WHITE));
//		System.out.println("\t\t\tBlack "+ state.getNumberOf(Pawn.BLACK));
		player = state.getTurn();
		List<int[]> pawns;
		if(player.equals(Turn.WHITE))
			pawns = getPawns(state, Pawn.WHITE);
		else if(player.equals(Turn.BLACK))
			pawns = getPawns(state, Pawn.BLACK);
		else throw new IllegalArgumentException("No action allowed: Endgame or wrong player");
		
		Collections.shuffle(pawns);
		
//		else System.out.println("Found " + pawns.size() + " pawns");
		actions.clear();
		for(int[] pos : pawns)
			tryAllAction(pos[0], pos[1], state);
		
//		System.out.println("\n\nMoves Found : " + this.actions.size() + "\n\n");
		
		return getBestAction(player);		
	}
	
	private List<int[]> getPawns(State state, Pawn turn) {
		List<int[]> ret = new ArrayList<>();
//		System.out.println("\t\t\t\tTurn "+ turn);
		for(int i=0; i<state.getBoard().length; i++)
			for(int j=0; j<state.getBoard()[0].length; j++) {
				Pawn p = state.getPawn(i, j);
				int[] pos = {i, j};
				if(p.equals(turn)) {
					ret.add(pos);
//					System.out.println("add "+ p);
				} else if(p.equals(Pawn.KING) && turn.equals(Pawn.WHITE)) {
					ret.add(pos);
//					System.out.println("add "+ p);
				}
			}
		return ret;
	}

	private void tryAllAction(int row, int col, State state) {
//		System.out.println("Next Pawn:");
		for(int i=state.getBoard().length; i>0; i--) {
			//System.out.println("\ti = "+i);
			int[][] l = {{row, col+i}, 
						 {row, col-i}, 
				         {row+i, col}, 
				         {row-i, col}};
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

	public static BrainAbs load(int timeout, int gametype) {
		BrainAbs brain = new BrainGenetic(timeout, gametype);
		brain.setTimeout(timeout);
		return brain;
	}

	@Override
	public void update(State state) {
		// TODO Auto-generated method stub
		
	}

}
