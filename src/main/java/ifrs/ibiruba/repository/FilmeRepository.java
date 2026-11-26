package ifrs.ibiruba.repository;

import ifrs.ibiruba.model.Filme;
import ifrs.ibiruba.persistencia.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import java.util.List;

public class FilmeRepository {

    public void salvar(Filme filme) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (filme.getId() == null) {
                em.persist(filme);
            } else {
                em.merge(filme);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erro ao salvar filme: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public void excluir(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Filme filme = em.find(Filme.class, id);
            if (filme != null) {
                em.remove(filme);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Não foi possível excluir o filme. Verifique se existem locações registradas para ele.", e);
        } finally {
            em.close();
        }
    }

    public List<Filme> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT f FROM Filme f ORDER BY f.titulo", Filme.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}