package ifrs.ibiruba;

import ifrs.ibiruba.persistencia.JPAUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        stage.setTitle("Locadora de Filmes");
        stage.setScene(scene);
        stage.setMinWidth(880);
        stage.setMinHeight(560);
        stage.show();
    }

    @Override
    public void stop() {
        JPAUtil.fechar();
    }

    public static void main(String[] args) {
        launch(args);
    }
}