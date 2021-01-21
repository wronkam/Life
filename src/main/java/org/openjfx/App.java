package org.openjfx;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;



/**
 * JavaFX App
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        View view=new View();
        Scene scene=new Scene(view,Constants.width,Constants.height+Constants.toolBarHeight);
        stage.setScene(scene);
        stage.show();
        stage.setTitle("Life");
        view.draw();
    }

    public static void main(String[] args) {
        launch();
        return;
    }

}