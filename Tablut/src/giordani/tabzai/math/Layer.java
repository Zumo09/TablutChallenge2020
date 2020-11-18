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
	
	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n]\n[\n", "[\n", "\n]");
		for(int k=0; k<this.layer.size(); k++)
			sj.add(layer.get(k).toString());
		return sj.toString();
	}

}