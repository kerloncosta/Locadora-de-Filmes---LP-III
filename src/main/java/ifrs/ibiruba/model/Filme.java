package ifrs.ibiruba.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "filmes")
public class Filme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String genero;

    private String diretor;
    private Integer anoLancamento;

    @Column(nullable = false)
    private Double valorDiaria;

    @Column(nullable = false)
    private Integer quantidadeDisponivel;

    @OneToMany(mappedBy = "filme")
    private List<Locacao> locacoes = new ArrayList<>();

    public Filme() {}

    public Filme(String titulo, String genero, String diretor, Integer anoLancamento,
                 Double valorDiaria, Integer quantidadeDisponivel) {
        this.titulo = titulo;
        this.genero = genero;
        this.diretor = diretor;
        this.anoLancamento = anoLancamento;
        this.valorDiaria = valorDiaria;
        this.quantidadeDisponivel = quantidadeDisponivel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getDiretor() { return diretor; }
    public void setDiretor(String diretor) { this.diretor = diretor; }

    public Integer getAnoLancamento() { return anoLancamento; }
    public void setAnoLancamento(Integer anoLancamento) { this.anoLancamento = anoLancamento; }

    public Double getValorDiaria() { return valorDiaria; }
    public void setValorDiaria(Double valorDiaria) { this.valorDiaria = valorDiaria; }

    public Integer getQuantidadeDisponivel() { return quantidadeDisponivel; }
    public void setQuantidadeDisponivel(Integer quantidadeDisponivel) { this.quantidadeDisponivel = quantidadeDisponivel; }

    public List<Locacao> getLocacoes() { return locacoes; }

    @Override
    public String toString() {
        return titulo + " (" + genero + ") - " + String.format("R$ %.2f/dia", valorDiaria);
    }
}