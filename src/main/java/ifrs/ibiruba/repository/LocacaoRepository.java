package ifrs.ibiruba.repository;

import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.model.Locacao;
import ifrs.ibiruba.persistencia.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.List;

public class LocacaoRepository {

    public void registrarLocacao(Locacao locacao) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Filme filmeGerenciado = em.find(Filme.class, locacao.getFilme().getId());
            if (filmeGerenciado == null || filmeGerenciado.getQuantidadeDisponivel() <= 0) {
                throw new IllegalStateException("Não há cópias disponíveis para este filme.");
            }

            filmeGerenciado.setQuantidadeDisponivel(filmeGerenciado.getQuantidadeDisponivel() - 1);
            em.merge(filmeGerenciado);

            locacao.setFilme(filmeGerenciado);
            em.persist(locacao);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Falha ao registrar locação: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void registrarDevolucao(Locacao locacao, LocalDate dataDevolucaoReal, double valorFinal) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Locacao locacaoGerenciada = em.find(Locacao.class, locacao.getId());
            Filme filmeGerenciado = em.find(Filme.class, locacao.getFilme().getId());

            if (locacaoGerenciada == null || "FINALIZADA".equals(locacaoGerenciada.getStatus())) {
                throw new IllegalStateException("Esta locação já foi finalizada ou não existe.");
            }

            locacaoGerenciada.setDataDevolucaoReal(dataDevolucaoReal);
            locacaoGerenciada.setValorTotal(valorFinal);
            locacaoGerenciada.setStatus("FINALIZADA");

            filmeGerenciado.setQuantidadeDisponivel(filmeGerenciado.getQuantidadeDisponivel() + 1);

            em.merge(filmeGerenciado);
            em.merge(locacaoGerenciada);

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Falha ao registrar devolução: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public List<Locacao> listarTodas() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT l FROM Locacao l JOIN FETCH l.cliente JOIN FETCH l.filme ORDER BY l.id DESC",
                            Locacao.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}