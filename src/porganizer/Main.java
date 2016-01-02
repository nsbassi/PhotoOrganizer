package porganizer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String title = "Photo Organizer";

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("organizer.fxml"));
        primaryStage.setTitle(title);

        primaryStage.setScene(new Scene(root, 552, 325));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
