package giordani.tabzai.player.brain.heuristic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import giordani.tabzai.math.Layer;
import giordani.tabzai.math.Matrix;
import it.unibo.ai.didattica.competition.tablut.domain.State;
import it.unibo.ai.didattica.competition.tablut.domain.State.Pawn;

public class HeuristicNN implements Heuristic {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String EXT = ".hnn";
	
	private List<Layer> layers;

	public HeuristicNN(int k1, int k2, int k3) {
		this.layers = new ArrayList<>();
		this.layers.add(new Layer(k1, 3, 3, 1));
		this.layers.add(new Layer(k2, 3, 3, k1));
		this.layers.add(new Layer(k3, 3, 3, k2));
		this.layers.add(new Layer(1, 3, 3, k3));
	}
	
	private HeuristicNN(List<Layer> layers) {
		this.layers = new ArrayList<>();
		for(Layer l : layers)
			this.layers.add(l.copy());
	}

	@Override
	public Heuristic copy() {
		return new HeuristicNN(this.getLayers());
	}

	@Override
	public double evaluate(State state) {
		Matrix input = this.stateToMatrix(state);
		
		Matrix out = this.getLayer(3).convolution(
						this.getLayer(2).convolution(
							this.getLayer(1).convolution(
								this.getLayer(0).convolution(input).tanh()
								   		 ).tanh()
						               ).tanh()
									);
		
		// System.out.println(out.getShape(0) + ", " + out.getShape(1) + ", " + out.getShape(2));
		return out.get(0, 0, 0);
	}
	
	@Override
	public Heuristic mutate(double mutationProb) {
		Random rnd = new Random();
		
		for(Layer l : this.getLayers())
			for(Matrix m : l.getList())
				for(int i=0; i<m.getShape(0); i++)
					for(int j=0; j<m.getShape(1); j++)
						for(int k=0; k<m.getShape(2); k++)
							if(rnd.nextDouble() < mutationProb) {
								double val = m.get(i, j, k) + rnd.nextGaussian();
								m.set(i, j, k, val);
							}
		return this;
	}

	@Override
	public List<Heuristic> crossover(Heuristic otherParent) {
		if(!(otherParent instanceof HeuristicNN))
			throw new IllegalArgumentException("Heuristic type mismatch");
		HeuristicNN other = (HeuristicNN) otherParent;
		List<Heuristic> ret = new ArrayList<>();
		ret.add(new HeuristicNN(List.of(this.getLayer(0), other.getLayer(1), this.getLayer(2), other.getLayer(3))));
		ret.add(new HeuristicNN(List.of(other.getLayer(0), this.getLayer(1), other.getLayer(2), this.getLayer(3))));
		return ret;
	}
	
	public List<Layer> getLayers() {
		return this.layers;
	}
	
	public Layer getLayer(int l) {
		return this.layers.get(l);
	}

	private Matrix stateToMatrix(State state) {
		Pawn[][] board = state.getBoard();
		Matrix ret = new Matrix(board.length, board[0].length, 1);
		
		for(int i=0; i<ret.getShape(0); i++)
			for(int j=0; j<ret.getShape(1); j++)
				if(board[i][j].equals(Pawn.KING))
					ret.set(i, j, 0, 10);
				else if(board[i][j].equals(Pawn.WHITE))
					ret.set(i, j, 0, 1);
				else if(board[i][j].equals(Pawn.BLACK))
					ret.set(i, j, 0, -1);
		// System.out.println(ret);
		return ret;
	}

	@Override
	public void save(String name) {
		Path p = Paths.get(Heuristic.PATH + File.separator + name + EXT);
		String path = p.toAbsolutePath().toString();
		new File(Heuristic.PATH).mkdirs();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))){
			oos.writeObject(this);
			System.out.println("Saved " + name);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Not Saved " + name);
		}
	}
	
	public static Heuristic load(String filename) {
		Path p = Paths.get(Heuristic.PATH + File.separator + filename + EXT);
		String path = p.toAbsolutePath().toString();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			System.out.println("Loading " + filename);
			return (HeuristicNN) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NOT LOADED");
			e.printStackTrace();
			System.exit(1);
		} 
		return null;
	}
}
