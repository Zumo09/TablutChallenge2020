package giordani.tabzai.player.brain.kernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

public abstract class KernelAbs implements Kernel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Random rnd;
	private double mutationProb;
	private double mutationScale;
	private int depth;
		
	public KernelAbs(double mutationProb, double mutationScale, int depth) {
		this.mutationProb = mutationProb;
		this.mutationScale = mutationScale;
		this.depth = depth;
		this.rnd = new Random();
	}
	
	protected Random getRandom() {
		return rnd;
	}
	
	@Override
	public double getMutationProb() {
		return mutationProb;
	}
	
	@Override
	public double getMutationScale() {
		return mutationScale;
	}
	
	@Override
	public int getDepth() {
		return depth;
	}	
	
	@Override
	public void save(String name) {
		Path p = Paths.get(Kernel.PATH + File.separator + name);
		String path = p.toAbsolutePath().toString();
		new File(Kernel.PATH).mkdirs();
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
}
