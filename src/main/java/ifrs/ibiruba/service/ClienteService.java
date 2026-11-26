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
}