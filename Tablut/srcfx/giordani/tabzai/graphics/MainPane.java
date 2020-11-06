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
	private TextField upTo;
	private TextField separator;
	private TextField ext;
	private TextField out;
	private ComboBox<String> chooser;
	private Map<String, Node> charts;
	
	public MainPane() {
		name = new TextField();
		folder = new TextField();
		ext = new TextField();
		separator = new TextField();
		upTo = new TextField();
		out = new TextField();
		chooser = new ComboBox<>();
		charts = new HashMap<>();
		folder.setText("kernels_firtsTry");
		name.setText("Kernel_");
		separator.setText("");
		separator.setPrefColumnCount(1);
		upTo.setText("50");
		upTo.setPrefColumnCount(2);
		ext.setText("");
		ext.setPrefColumnCount(5);
		out.setText("example");
		out.setPrefColumnCount(20);
		out.setEditable(false);
		HBox hb = new HBox();
		Button b = new Button();
		b.setText("Print");
		b.setOnAction(this::print);
		hb.getChildren().add(folder);
		hb.getChildren().add(name);
		hb.getChildren().add(separator);
		hb.getChildren().add(upTo);
		hb.getChildren().add(ext);
		hb.getChildren().add(b);
		hb.getChildren().add(out);
		hb.getChildren().add(chooser);
		this.setBottom(hb);
		chooser.setOnAction(e -> {
			this.setCenter(charts.get(chooser.getSelectionModel().getSelectedItem()));
		});
	}
	
	private void print(ActionEvent e) {
		charts.clear();
		ObservableList<String> list = FXCollections.observableArrayList();
		
		out.setText(folder.getText() + File.separator + name.getText() + 1 + separator.getText() + 0 + ext.getText());
		Map<String, List<Double>> data1 = readData(1);
		Map<String, List<Double>> data2 = readData(2);
		int i = 0;
		for(String key : data1.keySet()) {
			NumberAxis ax = new NumberAxis(); ax.setLabel("Generation");
			NumberAxis ay = new NumberAxis(); ay.setLabel("Value");
			XYChart.Series<Number, Number> f1 = new XYChart.Series<Number, Number>();
			XYChart.Series<Number, Number> f2 = new XYChart.Series<Number, Number>();
			
			for(int d = 0; d<data1.get(key).size(); d++) {
				f1.getData().add(new XYChart.Data<>(d+1,data1.get(key).get(d)));
				f2.getData().add(new XYChart.Data<>(d+1,data2.get(key).get(d)));
			}
			
			LineChart<Number, Number> ch = new LineChart<>(ax, ay);
			ch.setTitle(key);
			ch.getData().add(f1);
			ch.getData().add(f2);
			charts.put(key, ch);
			list.add(key);
		}
		
		chooser.setItems(list);
		chooser.setValue(list.get(0));
	}

	private Map<String, List<Double>> readData(int n) {
		Map<String, List<Double>> data = new HashMap<>();
		int gen = 0;
		try{
			gen = Integer.parseInt(upTo.getText());
		} catch(NumberFormatException e) {
			e.printStackTrace();
			System.exit(1);
		}
		for(int g=1; g<gen; g++) {
			Path p = Paths.get(folder.getText() + File.separator + name.getText() + n  + separator.getText() + g + ext.getText());
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
