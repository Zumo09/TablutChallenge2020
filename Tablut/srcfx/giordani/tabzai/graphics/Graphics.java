package giordani.tabzai.graphics;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Graphics extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			MainPane root = new MainPane();
			Scene scene = new Scene(root, 1000, 500);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
}
