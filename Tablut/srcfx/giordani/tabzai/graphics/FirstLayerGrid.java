package giordani.tabzai.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import giordani.tabzai.math.Layer;
import giordani.tabzai.math.Matrix;
import giordani.tabzai.player.brain.heuristic.HeuristicNN;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class FirstLayerGrid extends BorderPane {
	private TextField folder;
	private TextField name;
	private TextField par;
	private TextField sep;
	private TextField game;
	private TextField out;
	private ComboBox<Integer> genChooser;
	private ComboBox<Integer> parChooser;
	private Map<Integer, Map<Integer, Node>> charts;
	private String ext;
	
	public FirstLayerGrid() {
		folder = new TextField();
		name = new TextField();
		par = new TextField();
		sep = new TextField();
		game = new TextField();
		out = new TextField();
		genChooser = new ComboBox<>();
		parChooser = new ComboBox<>();
		charts = new HashMap<>();
		
		folder.setText("heuristic");
		name.setText("CNN_f2_p8_m100_o50_t2");
		par.setText("2");
		sep.setText("_");
		game.setText("100");
		
		
		folder.setPrefColumnCount(7);
		name.setPrefColumnCount(15);
		par.setPrefColumnCount(1);
		sep.setPrefColumnCount(1);
		game.setPrefColumnCount(2);
		
		
		out.setText("example");
		out.setPrefColumnCount(25);
		out.setEditable(false);
		
		HBox hb = new HBox();
		Button b = new Button();
		b.setText("Print");
		b.setOnAction(this::print);
		hb.getChildren().add(folder);
		hb.getChildren().add(name);
		hb.getChildren().add(par);
		hb.getChildren().add(sep);
		hb.getChildren().add(game);
		hb.getChildren().add(b);
		hb.getChildren().add(out);
		hb.getChildren().add(parChooser);
		hb.getChildren().add(genChooser);
		this.setBottom(hb);
		
		ext = HeuristicNN.EXT;
		
		print(null);
		this.setCenter(charts.get(0).get(0));
		

		parChooser.setOnAction(this::select);
		genChooser.setOnAction(this::select);
	}
	
	private void select(ActionEvent e) {
		int par = this.parChooser.getValue();
		int gen = this.genChooser.getValue();
		out.setText(folder.getText() + File.separator + name.getText() + par + sep.getText() + gen + ext);
		this.setCenter(charts.get(par).get(gen));
	}
	
	private void print(ActionEvent e) {
		charts.clear();
		ObservableList<Integer> listGen = FXCollections.observableArrayList();
		ObservableList<Integer> listPar = FXCollections.observableArrayList();
				
		int par = 0;
		try{
			par = Integer.parseInt(this.par.getText());
		} catch(NumberFormatException e2) {
			e2.printStackTrace();
			System.exit(1);
		}
		
		int gen = 0;
		try{
			gen = Integer.parseInt(game.getText());
		} catch(NumberFormatException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		for(int p=0; p<par; p++) {
			Map<Integer, Node> data = new HashMap<>();
			for(int g=0; g<gen; g++) {
				Optional<HeuristicNN> d = readData(p, g);
				if(d.isPresent())
					data.put(g, this.getGraph(d.get()));	
			}
			this.charts.put(p, data);
		}
		

		for(int p=0; p<par; p++)
			listPar.add(p);
		for(int g=0; g<gen; g++)
			listGen.add(g);
		
		parChooser.setItems(listPar);
		parChooser.setValue(listPar.get(0));
		genChooser.setItems(listGen);
		genChooser.setValue(listGen.get(0));
		
		out.setText(folder.getText() + File.separator + name.getText() + par + sep.getText() + gen + ext);
	}

	private Node getGraph(HeuristicNN heuristic) {
		Layer layer = heuristic.getLayer(0);
		GridPane pane = new GridPane();
		
		int size = layer.getList().size();
		
		double max = 0;
		for(Matrix matrix : layer.getList())
			for(int i=0; i<matrix.getShape(0); i++)
				for(int j=0; j<matrix.getShape(1); j++)
					max = Math.max(max, Math.abs(matrix.get(i, j, 0)));
		
		for(int i=0; i<size; i++)
			pane.add(this.getMat(layer.get(i), max), i%8, i/8);
		
		return pane;
	}
	private Optional<HeuristicNN> readData(int p, int g) {
		Path pt = Paths.get(folder.getText() + File.separator + name.getText() + p + sep.getText() + g + ext);
		String path = pt.toAbsolutePath().toString();
		try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
			return Optional.of((HeuristicNN) ois.readObject());
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("NOT LOADED " + path);
			return Optional.empty();
		}
	}
	
	private Node getMat(Matrix matrix, double max) {
		GridPane pane = new GridPane();
		if(matrix.getShape(2) != 1)
			throw new IllegalArgumentException("Error");
		
		for(int i=0; i<matrix.getShape(0); i++)
			for(int j=0; j<matrix.getShape(1); j++) {
				int c = (int) (255 * (matrix.get(i, j, 0))/max);
				Rectangle rec = new Rectangle(30, 30);
				int r = 0;
				int g = 0;
				if(c < 0)
					r = -c;
				else
					g = c;
				rec.setFill(Color.rgb(r, g, 0));
				pane.add(rec, j, i);
			}
		
		pane.setPadding(new Insets(10, 0, 0, 10));
		return pane;
	}
}
