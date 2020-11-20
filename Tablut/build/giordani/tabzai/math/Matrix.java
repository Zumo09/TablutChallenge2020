package giordani.tabzai.math;

import java.io.Serializable;
import java.util.Arrays;
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
		this.matrix = new double[rows][cols][ch];
		this.shape = new int[] {
				this.matrix.length,
				this.matrix[0].length,
				this.matrix[0][0].length
			};
	}
	
	public Matrix(double[][][] matrix) {
		this.matrix = matrix.clone();
		this.shape = new int[] {
				this.matrix.length,
				this.matrix[0].length,
				this.matrix[0][0].length
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
	
	public Matrix relu() {
		for(int i=0; i<this.getShape(0); i++)
			for(int j=0; j<this.getShape(1); j++)
				for(int k=0; k<this.getShape(2); k++)
					if(this.get(i, j, k) < 0)
						this.set(i, j, k, 0);
		return this;
	}
	
	public Matrix tanh() {
		for(int i=0; i<this.getShape(0); i++)
			for(int j=0; j<this.getShape(1); j++)
				for(int k=0; k<this.getShape(2); k++)
					this.set(i, j, k, Math.tanh(this.get(i, j, k)));
		return this;
	}
	
	public Matrix getSub(int inRow, int inCol, int size) {
		Matrix sub = new Matrix(size, size, this.getShape(2));
		for(int i=0; i<size; i++)
			for(int j=0; j<size; j++)
				for(int k=0; k<this.getShape(2); k++)
					sub.set(i, j, k, this.get(i+inRow, j+inCol, k));
		return sub;
	}
	
	public double inner(Matrix other) {
		if(!Arrays.equals(this.getShape(), other.getShape()))
			throw new IllegalArgumentException("Different Shapes");
		double ret = 0;
		for(int i=0; i<this.getShape(0); i++)
			for(int j=0; j<this.getShape(1); j++)
				for(int k=0; k<this.getShape(2); k++)
					ret += this.get(i, j, k) * other.get(i, j, k);
		return ret;
	}

	@Override
	public String toString() {
		StringJoiner sr = new StringJoiner("]\n[", "[", "]");
		for(int i=0; i<this.getShape(0); i++) {
			StringJoiner sc = new StringJoiner("], [", "[", "]");
			for(int j=0; j<this.getShape(1); j++) {
				StringJoiner sh = new StringJoiner(",\t", "(\t", "\t)");
				for(int k=0; k<this.getShape(2); k++) {
					if(k!=0 && k%5 == 0)
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

	public Matrix copy() {
		return new Matrix(this.matrix);
	}

}
