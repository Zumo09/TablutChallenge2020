package giordani.tabzai.math.test;

import java.util.Arrays;

import giordani.tabzai.math.*;

public class TestMath {
	public static void main(String[] args) {	
		int k1 = 64;
		int k2 = 32;
		int k3 = 64;
		
		Matrix m = Matrix.newRandom(9, 9, 1);
		
		Layer l1 = new Layer(k1, 3, 3, 1);
		Layer l2 = new Layer(k2, 3, 3, k1);
		Layer l3 = new Layer(k3, 3, 3, k2);
		Layer l4 = new Layer(1, 3, 3, k3);
		
		Matrix out1 = l1.convolution(m).tanh();
		Matrix out2 = l2.convolution(out1).tanh();
		Matrix out3 = l3.convolution(out2).tanh();
		Matrix out4 = l4.convolution(out3);
		
		System.out.println(out4.get(0, 0, 0));
		
		System.out.println(Arrays.toString(out1.getShape()));
		System.out.println(Arrays.toString(out2.getShape()));
		System.out.println(Arrays.toString(out3.getShape()));
		System.out.println(Arrays.toString(out4.getShape()));
		
		int p1 = k1*3*3;
		int p2 = k1*k2*3*3;
		int p3 = k2*k3*3*3;
		int p4 = k3*3*3;
		
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p3);
		System.out.println(p4);
		System.out.println(p1+p2+p3+p4);
	}

}
