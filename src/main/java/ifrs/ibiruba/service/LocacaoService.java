package ifrs.ibiruba.service;

import ifrs.ibiruba.model.Cliente;
import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.model.Locacao;
import ifrs.ibiruba.repository.LocacaoRepository;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class LocacaoService {

    private final LocacaoRepository repository = new LocacaoRepository();

    public double calcularValorPrevisto(Filme filme, LocalDate dataRetirada, LocalDate dataDevolucaoPrevista) {
        if (filme == null || dataRetirada == null || dataDevolucaoPrevista == null) {
            return 0.0;
        }
        if (dataDevolucaoPrevista.isBefore(dataRetirada)) {
            throw new IllegalArgumentException("A data prevista para devolução não pode ser anterior à retirada.");
        }
        long dias = Math.max(1, ChronoUnit.DAYS.between(dataRetirada, dataDevolucaoPrevista));
        return dias * filme.getValorDiaria();
    }

    public double calcularValorFinalComMulta(Locacao locacao, LocalDate dataDevolucaoReal) {
        if (locacao == null || dataDevolucaoReal == null) {
            throw new IllegalArgumentException("Dados inválidos para calcular devolução.");
        }
        if (dataDevolucaoReal.isBefore(locacao.getDataRetirada())) {
            throw new IllegalArgumentException("A data efetiva da devolução não pode ser anterior à retirada.");
        }

        double valorFinal = locacao.getValorTotal();
        long diasAtraso = ChronoUnit.DAYS.between(locacao.getDataDevolucaoPrevista(), dataDevolucaoReal);

        if (diasAtraso > 0) {
            double diariaComMulta = locacao.getFilme().getValorDiaria() * 1.5;
            valorFinal += diasAtraso * diariaComMulta;
        }
        return valorFinal;
    }

    public void registrarLocacao(Cliente cliente, Filme filme, LocalDate dataRetirada, LocalDate dataDevolucaoPrevista) {
        if (cliente == null) {
            throw new IllegalArgumentException("Selecione um cliente para a locação.");
        }
        if (filme == null) {
            throw new IllegalArgumentException("Selecione um filme para a locação.");
        }
        if (filme.getQuantidadeDisponivel() == null || filme.getQuantidadeDisponivel() <= 0) {
            throw new IllegalStateException("O filme selecionado não possui cópias disponíveis.");
        }
        if (dataRetirada == null || dataDevolucaoPrevista == null) {
            throw new IllegalArgumentException("Informe as datas de retirada e devolução prevista.");
        }

        double valorTotal = calcularValorPrevisto(filme, dataRetirada, dataDevolucaoPrevista);
        Locacao novaLocacao = new Locacao(cliente, filme, dataRetirada, dataDevolucaoPrevista, valorTotal);
        repository.registrarLocacao(novaLocacao);
    }

    public void registrarDevolucao(Locacao locacao, LocalDate dataDevolucaoReal) {
        if (locacao == null) {
            throw new IllegalArgumentException("Selecione uma locação para finalizar.");
        }
        if (!"Aberta".equals(locacao.getStatus())) {
            throw new IllegalStateException("Esta locação já está finalizada.");
        }

        double valorFinal = calcularValorFinalComMulta(locacao, dataDevolucaoReal);
        repository.registrarDevolucao(locacao, dataDevolucaoReal, valorFinal);
    }

    public List<Locacao> listarTodas() {
        return repository.listarTodas();
    }

    public List<Locacao> filtrarPorStatus(String status) {
        if (status == null || "Todos".equalsIgnoreCase(status)) {
            return listarTodas();
        }
        return repository.listarTodas().stream()
                .filter(l -> status.equalsIgnoreCase(l.getStatus()))
                .collect(Collectors.toList());
    }
}
