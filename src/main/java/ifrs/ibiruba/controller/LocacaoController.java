package ifrs.ibiruba.controller;

import ifrs.ibiruba.model.Cliente;
import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.model.Locacao;
import ifrs.ibiruba.service.ClienteService;
import ifrs.ibiruba.service.FilmeService;
import ifrs.ibiruba.service.LocacaoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class LocacaoController {

    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<Filme> cbFilme;
    @FXML private DatePicker dpRetirada;
    @FXML private DatePicker dpDevolucaoPrevista;
    @FXML private Label lblValorPrevisto;
    @FXML private Button btnRegistrarLocacao;

    @FXML private DatePicker dpDevolucaoReal;
    @FXML private Button btnRegistrarDevolucao;
    @FXML private ComboBox<String> cbFiltroStatus;

    @FXML private TableView<Locacao> tabelaLocacoes;
    @FXML private TableColumn<Locacao, Long> colId;
    @FXML private TableColumn<Locacao, String> colCliente;
    @FXML private TableColumn<Locacao, String> colFilme;
    @FXML private TableColumn<Locacao, String> colDataRetirada;
    @FXML private TableColumn<Locacao, String> colDataPrevista;
    @FXML private TableColumn<Locacao, String> colDataReal;
    @FXML private TableColumn<Locacao, String> colValorTotal;
    @FXML private TableColumn<Locacao, String> colStatus;
    @FXML private Label lblTotal;

    private final ClienteService clienteService = new ClienteService();
    private final FilmeService filmeService = new FilmeService();
    private final LocacaoService locacaoService = new LocacaoService();

    private final ObservableList<Locacao> listaLocacoes = FXCollections.observableArrayList();
    private final DateTimeFormatter formatoData = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        dpRetirada.setValue(LocalDate.now());
        dpDevolucaoPrevista.setValue(LocalDate.now().plusDays(3));
        dpDevolucaoReal.setValue(LocalDate.now());

        cbFiltroStatus.setItems(FXCollections.observableArrayList("Todos", "Aberta", "Finalizada"));
        cbFiltroStatus.setValue("Todos");

        configurarColunas();
        tabelaLocacoes.setItems(listaLocacoes);

        btnRegistrarDevolucao.setDisable(true);
        tabelaLocacoes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            boolean podeDevolver = (novo != null && "Aberta".equals(novo.getStatus()));
            btnRegistrarDevolucao.setDisable(!podeDevolver);
        });

        cbFiltroStatus.valueProperty().addListener((obs, antigo, novoStatus) -> carregarTabela());

        // Recalcula o valor previsto sempre que o filme ou as datas mudarem
        cbFilme.valueProperty().addListener((obs, a, b) -> recalcularValorPrevisto());
        dpRetirada.valueProperty().addListener((obs, a, b) -> recalcularValorPrevisto());
        dpDevolucaoPrevista.valueProperty().addListener((obs, a, b) -> recalcularValorPrevisto());

        atualizarDados();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCliente.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCliente().getNome()));
        colFilme.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getFilme().getTitulo()));
        colDataRetirada.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarData(cell.getValue().getDataRetirada())));
        colDataPrevista.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarData(cell.getValue().getDataDevolucaoPrevista())));
        colDataReal.setCellValueFactory(cell ->
                new SimpleStringProperty(formatarData(cell.getValue().getDataDevolucaoReal())));
        colValorTotal.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("R$ %.2f", cell.getValue().getValorTotal())));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
    }

    public void atualizarDados() {
        Cliente clienteAtual = cbCliente.getValue();
        Filme filmeAtual = cbFilme.getValue();

        cbCliente.setItems(FXCollections.observableArrayList(clienteService.listarTodos()));
        cbFilme.setItems(FXCollections.observableArrayList(filmeService.listarDisponiveis()));

        if (clienteAtual != null) {
            cbCliente.getItems().stream()
                    .filter(c -> c.getId().equals(clienteAtual.getId()))
                    .findFirst()
                    .ifPresent(cbCliente::setValue);
        }
        if (filmeAtual != null) {
            cbFilme.getItems().stream()
                    .filter(f -> f.getId().equals(filmeAtual.getId()))
                    .findFirst()
                    .ifPresent(cbFilme::setValue);
        }

        recalcularValorPrevisto();
        carregarTabela();
    }

    @FXML
    private void onRegistrarLocacao() {
        try {
            locacaoService.registrarLocacao(
                    cbCliente.getValue(),
                    cbFilme.getValue(),
                    dpRetirada.getValue(),
                    dpDevolucaoPrevista.getValue()
            );

            cbCliente.getSelectionModel().clearSelection();
            cbFilme.getSelectionModel().clearSelection();
            dpRetirada.setValue(LocalDate.now());
            dpDevolucaoPrevista.setValue(LocalDate.now().plusDays(3));

            atualizarDados();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Locação registrada e estoque atualizado.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    @FXML
    private void onRegistrarDevolucao() {
        Locacao selecionada = tabelaLocacoes.getSelectionModel().getSelectedItem();
        if (selecionada == null) return;

        try {
            LocalDate dataReal = dpDevolucaoReal.getValue();
            double valorFinal = locacaoService.calcularValorFinalComMulta(selecionada, dataReal);
            double multa = valorFinal - selecionada.getValorTotal();

            String resumo = String.format(
                    "Cliente: %s\nFilme: %s\nValor Original: R$ %.2f\nMulta por Atraso: R$ %.2f\nTotal a Cobrar: R$ %.2f",
                    selecionada.getCliente().getNome(),
                    selecionada.getFilme().getTitulo(),
                    selecionada.getValorTotal(),
                    Math.max(0.0, multa),
                    valorFinal
            );

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Confirmar Devolução");
            confirmacao.setHeaderText("Finalizar locação #" + selecionada.getId() + "?");
            confirmacao.setContentText(resumo);

            Optional<ButtonType> resultado = confirmacao.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                locacaoService.registrarDevolucao(selecionada, dataReal);
                atualizarDados();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Devolução Concluída",
                        "Devolução registrada e cópia devolvida ao estoque.");
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validação", e.getMessage());
        } catch (Exception e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Erro", e.getMessage());
        }
    }

    private void recalcularValorPrevisto() {
        try {
            double valor = locacaoService.calcularValorPrevisto(
                    cbFilme.getValue(),
                    dpRetirada.getValue(),
                    dpDevolucaoPrevista.getValue()
            );
            lblValorPrevisto.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
            lblValorPrevisto.setText(String.format("Valor Previsto: R$ %.2f", valor));
        } catch (IllegalArgumentException e) {
            lblValorPrevisto.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dc2626;");
            lblValorPrevisto.setText(e.getMessage());
        }
    }

    private void carregarTabela() {
        listaLocacoes.setAll(locacaoService.filtrarPorStatus(cbFiltroStatus.getValue()));
        lblTotal.setText("Total de locações: " + listaLocacoes.size());
    }

    private String formatarData(LocalDate data) {
        return (data != null) ? data.format(formatoData) : "-";
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}