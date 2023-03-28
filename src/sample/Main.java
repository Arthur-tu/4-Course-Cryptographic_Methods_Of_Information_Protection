package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("fxml/controller.fxml"));
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(root, 600, 360));
        stage = primaryStage;
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args);}
}