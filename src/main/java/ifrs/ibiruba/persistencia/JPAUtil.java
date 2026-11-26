package ifrs.ibiruba.persistencia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("locadoraPU");

    public static EntityManager getEntityManager() {
        return EMF.createEntityManager();
    }

    public static void fechar() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}