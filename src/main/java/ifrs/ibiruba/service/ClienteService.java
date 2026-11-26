package ifrs.ibiruba.service;

import ifrs.ibiruba.model.Cliente;
import ifrs.ibiruba.repository.ClienteRepository;
import java.util.List;
import java.util.stream.Collectors;

public class ClienteService {

    private final ClienteRepository repository = new ClienteRepository();

    public void salvar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("O nome do cliente é obrigatório.");
        }
        if (cliente.getCpf() == null || cliente.getCpf().trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF do cliente é obrigatório.");
        }

        String cpfLimpo = cliente.getCpf().trim();
        boolean cpfDuplicado = repository.listarTodos().stream()
                .anyMatch(c -> c.getCpf().equalsIgnoreCase(cpfLimpo)
                        && (cliente.getId() == null || !c.getId().equals(cliente.getId())));

        if (cpfDuplicado) {
            throw new IllegalArgumentException("Já existe um cliente cadastrado com este CPF.");
        }

        String apenasDigitosCpf = cliente.getCpf().replaceAll("\\D", "");
        if (apenasDigitosCpf.length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter exatamente 11 dígitos.");
        } else if (!isCpfValido(apenasDigitosCpf)) {
            throw new IllegalArgumentException("O CPF informado é inválido.");
        }

        if (cliente.getTelefone() != null && !cliente.getTelefone().isBlank()) {
            int digitosTel = cliente.getTelefone().replaceAll("\\D", "").length();
            if (digitosTel < 10 || digitosTel > 11) {
                throw new IllegalArgumentException("O telefone deve conter 10 ou 11 dígitos com DDD.");
            }
        }

        cliente.setNome(cliente.getNome().trim());
        cliente.setCpf(cpfLimpo);
        repository.salvar(cliente);
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Selecione um cliente válido para excluir.");
        }
        repository.excluir(id);
    }

    public List<Cliente> listarTodos() {
        return repository.listarTodos();
    }

    public List<Cliente> buscarPorFiltro(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        String filtro = termo.trim().toLowerCase();
        return repository.listarTodos().stream()
                .filter(c -> c.getNome().toLowerCase().contains(filtro)
                        || c.getCpf().toLowerCase().contains(filtro))
                .collect(Collectors.toList());
    }
    //Função para verificar se o CPF informado é válido
    private boolean isCpfValido(String cpf) {
        //Não aceita se o tamanho não for 11 ou se for uma sequência de números repetidos, tipo 111.111.111-11
        if (cpf == null || cpf.length() != 11 || cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        // Cálculo do 1º dígito verificador
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int primeiroDigito = (soma * 10) % 11;
        if (primeiroDigito == 10) primeiroDigito = 0;

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        // Cálculo do 2º dígito verificador
        soma = 0;
        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int segundoDigito = (soma * 10) % 11;
        if (segundoDigito == 10) segundoDigito = 0;

        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }
}