package giordani.tabzai.math;

import java.io.Serializable;
import java.util.Random;
import java.util.StringJoiner;

public class Matrix implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private double[][][] matrix;
	private int[] shape;

	public Matrix(int rows, int cols, int ch) {
		this(new double[rows][cols][ch]);
	}
	
	public Matrix(double[][][] matrix) {
		this.matrix = matrix;
		this.shape = new int[] {
				matrix.length,
				matrix[0].length,
				matrix[0][0].length
			};
	}
	
	public double get(int i, int j, int k) {
		return this.matrix[i][j][k];
	}
	
	public void set(int i, int j, int k, double val) {
		this.matrix[i][j][k] = val;
	}
	
	public int[] getShape() {
		return shape;
	}
	
	public int getShape(int c) {
		return shape[c];
	}
	
	

	@Override
	public String toString() {
		StringJoiner sr = new StringJoiner("]\n[", "[", "]");
		for(int i=0; i<this.getShape(0); i++) {
			StringJoiner sc = new StringJoiner("], [", "[", "]");
			for(int j=0; j<this.getShape(1); j++) {
				StringJoiner sh = new StringJoiner(",\t", "[\t", "\t]");
				for(int k=0; k<this.getShape(2); k++) {
					if(k%5 == 0)
						sh.add("\n");
					sh.add(Double.toString(this.get(i, j, k)));
				}
				sc.add(sh.toString());
			}
			sr.add(sc.toString());
		}
		return sr.toString();
	}

	public static Matrix newRandom(int rows, int cols, int ch) {
		Matrix matrix = new Matrix(rows, cols, ch);
		Random rnd = new Random();
		for(int i=0; i<matrix.getShape(0); i++)
			for(int j=0; j<matrix.getShape(1); j++)
				for(int k=0; k<matrix.getShape(2); k++)
					matrix.set(i, j, k, rnd.nextGaussian());
		return matrix;
	}

}
