package ifrs.ibiruba.controller;

import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.service.FilmeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class FilmeController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtGenero;
    @FXML private TextField txtDiretor;
    @FXML private TextField txtAno;
    @FXML private TextField txtValorDiaria;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtBusca;

    @FXML private Button btnSalvar;
    @FXML private Button btnNovo;
    @FXML private Button btnExcluir;
    @FXML private Label lblTotal;

    @FXML private TableView<Filme> tabelaFilmes;
    @FXML private TableColumn<Filme, Long> colId;
    @FXML private TableColumn<Filme, String> colTitulo;
    @FXML private TableColumn<Filme, String> colGenero;
    @FXML private TableColumn<Filme, String> colDiretor;
    @FXML private TableColumn<Filme, Integer> colAno;
    @FXML private TableColumn<Filme, String> colValorDiaria;
    @FXML private TableColumn<Filme, Integer> colQuantidade;

    private final FilmeService filmeService = new FilmeService();
    private final ObservableList<Filme> listaFilmes = FXCollections.observableArrayList();
    private Filme filmeSelecionado;

    @FXML
    public void initialize() {
        aplicarFiltroInteiro(txtAno, 4);
        aplicarFiltroInteiro(txtQuantidade, 4);
        aplicarFiltroDecimal(txtValorDiaria);

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colGenero.setCellValueFactory(new PropertyValueFactory<>("genero"));
        colDiretor.setCellValueFactory(new PropertyValueFactory<>("diretor"));
        colAno.setCellValueFactory(new PropertyValueFactory<>("anoLancamento"));
        colValorDiaria.setCellValueFactory(cellData -> {
            Double valor = cellData.getValue().getValorDiaria();
            String formatado = (valor != null) ? String.format("R$ %.2f", valor) : "";
            return new SimpleStringProperty(formatado);
        });
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidadeDisponivel"));

        tabelaFilmes.setItems(listaFilmes);

        btnExcluir.disableProperty().bind(
                tabelaFilmes.getSelectionModel().selectedItemProperty().isNull()
        );

        tabelaFilmes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            filmeSelecionado = novo;
            if (novo != null) {
                txtTitulo.setText(novo.getTitulo());
                txtGenero.setText(novo.getGenero());
                txtDiretor.setText(novo.getDiretor() != null ? novo.getDiretor() : "");
                txtAno.setText(novo.getAnoLancamento() != null ? String.valueOf(novo.getAnoLancamento()) : "");
                txtValorDiaria.setText(novo.getValorDiaria() != null
                        ? String.format("%.2f", novo.getValorDiaria()).replace('.', ',') : "");
                txtQuantidade.setText(novo.getQuantidadeDisponivel() != null
                        ? String.valueOf(novo.getQuantidadeDisponivel()) : "");
            }
        });

        txtBusca.textProperty().addListener((obs, antigo, novoTexto) -> {
            listaFilmes.setAll(filmeService.buscarPorFiltro(novoTexto));
            atualizarContador();
        });

        carregarTabela();
    }

    @FXML
    private void onSalvar() {
        try {
            Filme filme = new Filme();
            if (filmeSelecionado != null) {
                filme.setId(filmeSelecionado.getId());
            }

            filme.setTitulo(txtTitulo.getText());
            filme.setGenero(txtGenero.getText());
            filme.setDiretor(txtDiretor.getText() != null ? txtDiretor.getText().trim() : null);

            String anoTexto = txtAno.getText().trim();
            filme.setAnoLancamento(anoTexto.isEmpty() ? null : Integer.parseInt(anoTexto));

            String valorTexto = txtValorDiaria.getText().trim().replace(',', '.');
            filme.setValorDiaria(valorTexto.isEmpty() ? null : Double.parseDouble(valorTexto));

            String qtdTexto = txtQuantidade.getText().trim();
            filme.setQuantidadeDisponivel(qtdTexto.isEmpty() ? null : Integer.parseInt(qtdTexto));

            filmeService.salvar(filme);
            limparFormulario();
            carregarTabela();
        } catch (IllegalArgumentException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro ao salvar", e.getMessage());
        }
    }

    @FXML
    private void onNovo() {
        limparFormulario();
    }

    @FXML
    private void onExcluir() {
        Filme selecionado = tabelaFilmes.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir o filme \"" + selecionado.getTitulo() + "\"?");
        confirmacao.setContentText("Esta ação não poderá ser desfeita.");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                filmeService.excluir(selecionado.getId());
                limparFormulario();
                carregarTabela();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao excluir", e.getMessage());
            }
        }
    }

    private void carregarTabela() {
        listaFilmes.setAll(filmeService.buscarPorFiltro(txtBusca.getText()));
        atualizarContador();
    }

    private void limparFormulario() {
        tabelaFilmes.getSelectionModel().clearSelection();
        filmeSelecionado = null;
        txtTitulo.clear();
        txtGenero.clear();
        txtDiretor.clear();
        txtAno.clear();
        txtValorDiaria.clear();
        txtQuantidade.clear();
    }

    private void atualizarContador() {
        lblTotal.setText("Total de filmes: " + listaFilmes.size());
    }

    private void aplicarFiltroInteiro(TextField campo, int maxDigitos) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.matches("\\d{0," + maxDigitos + "}")) {
                return change;
            }
            return null;
        }));
    }

    private void aplicarFiltroDecimal(TextField campo) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String novoTexto = change.getControlNewText();
            if (novoTexto.matches("\\d{0,6}([.,]\\d{0,2})?")) {
                return change;
            }
            return null;
        }));
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}