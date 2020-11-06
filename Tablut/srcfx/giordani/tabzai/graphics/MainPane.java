package giordani.tabzai.graphics;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainPane extends BorderPane{
	private TextField folder;
	private TextField name;
	private TextField par;
	private TextField sep;
	private TextField game;
	private TextField out;
	private ComboBox<String> chooser;
	private Map<String, Node> charts;
	private String ext;
	
	public MainPane() {
		folder = new TextField();
		name = new TextField();
		par = new TextField();
		sep = new TextField();
		game = new TextField();
		out = new TextField();
		chooser = new ComboBox<>();
		charts = new HashMap<>();
		
		folder.setText("kernels");
		name.setText("Eclipse");
		par.setText("4");
		sep.setText("_");
		game.setText("5");
		
		
		folder.setPrefColumnCount(10);
		name.setPrefColumnCount(10);
		par.setPrefColumnCount(1);
		sep.setPrefColumnCount(1);
		game.setPrefColumnCount(2);
		
		
		out.setText("example");
		out.setPrefColumnCount(20);
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
		hb.getChildren().add(chooser);
		this.setBottom(hb);
		chooser.setOnAction(e -> {
			this.setCenter(charts.get(chooser.getSelectionModel().getSelectedItem()));
		});
		
		ext = ".ker";
	}
	
	private void print(ActionEvent e) {
		charts.clear();
		ObservableList<String> list = FXCollections.observableArrayList();
		
		out.setText(folder.getText() + File.separator + name.getText() + par.getText() + sep.getText() + game.getText() + ext);
		
		Map<String, List<List<Double>>> data = readAll();

		for(String key : data.keySet()) {
			NumberAxis ax = new NumberAxis(); ax.setLabel("Generation");
			NumberAxis ay = new NumberAxis(); ay.setLabel("Value");
			LineChart<Number, Number> ch = new LineChart<>(ax, ay);
			ch.setTitle(key);
			for(List<Double> rec : data.get(key)) {
				XYChart.Series<Number, Number> f = new XYChart.Series<Number, Number>();
				for(int d = 0; d<rec.size(); d++)
					f.getData().add(new XYChart.Data<>(d,rec.get(d)));
				ch.getData().add(f);
			}
			charts.put(key, ch);
			list.add(key);
		}
		
		chooser.setItems(list);
		chooser.setValue(list.get(0));
	}

	private Map<String, List<List<Double>>> readAll() {
		Map<String, List<List<Double>>> data = new HashMap<>();
		int parents = 0;
		try{
			parents = Integer.parseInt(par.getText());
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		for(int n=0; n<parents; n++) {	
			Map<String, List<Double>> d = readData(n);
			for(String key : d.keySet()) {
				if(!data.containsKey(key))
					data.put(key, new ArrayList<>());
				data.get(key).add(d.get(key));
			}
		}		
		return data;
	}

	private Map<String, List<Double>> readData(int n) {
		Map<String, List<Double>> data = new HashMap<>();
		int gen = 0;
		try{
			gen = Integer.parseInt(game.getText());
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		for(int g=0; g<gen; g++) {
			Path p = Paths.get(folder.getText() + File.separator + name.getText() + n + sep.getText() + g + ext);
			String path = p.toAbsolutePath().toString();
			try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
				@SuppressWarnings("unchecked")
				Map<String, Double> params = (Map<String, Double>) ois.readObject();
				for(String key : params.keySet()) {
					if(!data.containsKey(key))
						data.put(key, new ArrayList<>());
					data.get(key).add(params.get(key));
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("NOT LOADED " + path);
			} 
		}		
		return data;
	}
}
