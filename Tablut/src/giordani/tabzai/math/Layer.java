package giordani.tabzai.math;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Layer implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<Matrix> layer;
	
	public Layer(int filter, int rows, int cols, int ch) {
		layer = new ArrayList<>();
		while(layer.size() < filter)
			layer.add(Matrix.newRandom(rows, cols, ch));
	}
	
	public Matrix get(int index) {
		return layer.get(index);
	}
	
	public List<Matrix> getList() {
		return this.layer;
	}
	
	public Matrix convolution(Matrix input) {
		if(input.getShape(2) != this.get(0).getShape(2) || input.getShape(0) < this.get(0).getShape(0) || input.getShape(1) < this.get(0).getShape(1))
			throw new IllegalArgumentException("Convolution not applicable for shapes " + input.getShape() + " and " + this.get(0).getShape());
		int size = input.getShape(0) - this.get(0).getShape(0) + 1;
		Matrix output = new Matrix(size, size, this.getList().size());
		for(int f=0; f<this.getList().size(); f++)
			for(int i=0; i<size; i++)
				for(int j=0; i<size; i++) {
					// Perform inner product between filter and a submatrix and assign it to out(i, j, f)
					Matrix sub = input.getSub(i, j, this.get(f).getShape(0));
					output.set(i, j, f, this.get(f).inner(sub));
				}
		return output;
	}
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n]\n[\n", "[\n", "\n]");
		for(int k=0; k<this.layer.size(); k++)
			sj.add(layer.get(k).toString());
		return sj.toString();
	}

}