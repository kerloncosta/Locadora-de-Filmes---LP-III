package ifrs.ibiruba.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Tab tabLocacoes;
    @FXML private LocacaoController locacoesViewController;

    @FXML
    public void initialize() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, abaAntiga, abaNova) -> {
            if (abaNova == tabLocacoes && locacoesViewController != null) {
                locacoesViewController.atualizarDados();
            }
        });
    }
}