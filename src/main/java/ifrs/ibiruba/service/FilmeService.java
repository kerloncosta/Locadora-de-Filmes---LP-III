package ifrs.ibiruba.service;

import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.repository.FilmeRepository;
import java.util.List;
import java.util.stream.Collectors;

public class FilmeService {

    private final FilmeRepository repository = new FilmeRepository();

    public void salvar(Filme filme) {
        if (filme.getTitulo() == null || filme.getTitulo().trim().isEmpty()) {
            throw new IllegalArgumentException("O título do filme é obrigatório.");
        }
        if (filme.getGenero() == null || filme.getGenero().trim().isEmpty()) {
            throw new IllegalArgumentException("O gênero do filme é obrigatório.");
        }
        if (filme.getValorDiaria() == null || filme.getValorDiaria() <= 0) {
            throw new IllegalArgumentException("O valor da diária deve ser maior que zero.");
        }
        if (filme.getQuantidadeDisponivel() == null || filme.getQuantidadeDisponivel() < 0) {
            throw new IllegalArgumentException("A quantidade disponível não pode ser negativa.");
        }

        filme.setTitulo(filme.getTitulo().trim());
        filme.setGenero(filme.getGenero().trim());
        repository.salvar(filme);
    }

    public void excluir(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Selecione um filme válido para excluir.");
        }
        repository.excluir(id);
    }

    public List<Filme> listarTodos() {
        return repository.listarTodos();
    }

    public List<Filme> listarDisponiveis() {
        return repository.listarTodos().stream()
                .filter(f -> f.getQuantidadeDisponivel() != null && f.getQuantidadeDisponivel() > 0)
                .collect(Collectors.toList());
    }

    public List<Filme> buscarPorFiltro(String termo) {
        if (termo == null || termo.trim().isEmpty()) {
            return listarTodos();
        }
        String filtro = termo.trim().toLowerCase();
        return repository.listarTodos().stream()
                .filter(f -> f.getTitulo().toLowerCase().contains(filtro)
                        || f.getGenero().toLowerCase().contains(filtro)
                        || (f.getDiretor() != null && f.getDiretor().toLowerCase().contains(filtro)))
                .collect(Collectors.toList());
    }
}