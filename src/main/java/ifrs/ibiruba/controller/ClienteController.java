package ifrs.ibiruba.controller;

import ifrs.ibiruba.model.Cliente;
import ifrs.ibiruba.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class ClienteController {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private TextField txtBusca;

    @FXML private Button btnSalvar;
    @FXML private Button btnNovo;
    @FXML private Button btnExcluir;
    @FXML private Label lblTotal;

    @FXML private TableView<Cliente> tabelaClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEmail;

    private final ClienteService clienteService = new ClienteService();
    private final ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
    private Cliente clienteSelecionado;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarCpf(cellData.getValue().getCpf())));
        colTelefone.setCellValueFactory(cellData ->
                new SimpleStringProperty(formatarTelefone(cellData.getValue().getTelefone())));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));

        MaskCpf(txtCpf);
        MaskTelefone(txtTelefone);

        tabelaClientes.setItems(listaClientes);

        btnExcluir.disableProperty().bind(
                tabelaClientes.getSelectionModel().selectedItemProperty().isNull()
        );

        tabelaClientes.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            clienteSelecionado = novo;
            if (novo != null) {
                txtNome.setText(novo.getNome());
                txtCpf.setText(novo.getCpf() != null ? novo.getCpf() : "");
                txtTelefone.setText(novo.getTelefone() != null ? novo.getTelefone() : "");
                txtEmail.setText(novo.getEmail() != null ? novo.getEmail() : "");
            }
        });

        txtBusca.textProperty().addListener((obs, antigo, novoTexto) -> {
            listaClientes.setAll(clienteService.buscarPorFiltro(novoTexto));
            atualizarContador();
        });

        carregarTabela();
    }

    @FXML
    private void onSalvar() {
        try {
            Cliente cliente = new Cliente();
            if (clienteSelecionado != null) {
                cliente.setId(clienteSelecionado.getId());
            }
            cliente.setNome(txtNome.getText());
            cliente.setCpf(txtCpf.getText());
            cliente.setTelefone(txtTelefone.getText());
            cliente.setEmail(txtEmail.getText());

            clienteService.salvar(cliente);
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
        Cliente selecionado = tabelaClientes.getSelectionModel().getSelectedItem();
        if (selecionado == null) return;

        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Exclusão");
        confirmacao.setHeaderText("Excluir cliente " + selecionado.getNome() + "?");
        confirmacao.setContentText("Esta ação não poderá ser desfeita.");

        Optional<ButtonType> resultado = confirmacao.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                clienteService.excluir(selecionado.getId());
                limparFormulario();
                carregarTabela();
            } catch (Exception e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Erro ao excluir", e.getMessage());
            }
        }
    }

    private void carregarTabela() {
        listaClientes.setAll(clienteService.buscarPorFiltro(txtBusca.getText()));
        atualizarContador();
    }

    private void limparFormulario() {
        tabelaClientes.getSelectionModel().clearSelection();
        clienteSelecionado = null;
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtEmail.clear();
    }

    private void atualizarContador() {
        lblTotal.setText("Total de clientes: " + listaClientes.size());
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    private void MaskCpf(TextField campo) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String resultado = formatarCpf(change.getControlNewText());
            change.setText(resultado);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(resultado.length());
            change.setAnchor(resultado.length());
            return change;
        }));
    }

    private String formatarCpf(String valor) {
        if (valor == null) return "";
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() > 11) {
            digitos = digitos.substring(0, 11);
        }

        StringBuilder formatado = new StringBuilder();
        for (int i = 0; i < digitos.length(); i++) {
            if (i == 3 || i == 6) formatado.append('.');
            if (i == 9) formatado.append('-');
            formatado.append(digitos.charAt(i));
        }
        return formatado.toString();
    }

    private String formatarTelefone(String valor) {
        if (valor == null) return "";
        String digitos = valor.replaceAll("\\D", "");
        if (digitos.length() > 11) {
            digitos = digitos.substring(0, 11);
        }

        StringBuilder formatado = new StringBuilder();
        int len = digitos.length();
        if (len > 0) {
            formatado.append('(');
            formatado.append(digitos, 0, Math.min(2, len));
        }
        if (len >= 3) {
            formatado.append(") ");
            if (len <= 10) {
                formatado.append(digitos, 2, Math.min(6, len));
                if (len >= 7) {
                    formatado.append('-').append(digitos.substring(6));
                }
            } else {
                formatado.append(digitos, 2, 7);
                formatado.append('-').append(digitos.substring(7));
            }
        }
        return formatado.toString();
    }

    private void MaskTelefone(TextField campo) {
        campo.setTextFormatter(new TextFormatter<>(change -> {
            String resultado = formatarTelefone(change.getControlNewText());
            change.setText(resultado);
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(resultado.length());
            change.setAnchor(resultado.length());
            return change;
        }));
    }
}