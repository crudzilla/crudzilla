package io.github.crudzilla.persistency;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @param <E> entidade JPA que implemente Entidade K
 * @param <K> tipo da chave da Entidade E
 */
public class RepositoryJpa<E extends CRUDZillaEntity<K>, K> extends AbstractRepositoryJPA<E, K> {

    @PersistenceContext
    protected EntityManager entityManager;

    @Override
    protected EntityManager getEntityManager() { return this.entityManager; }

    @Override
    @SuppressWarnings("unchecked")
    /**
     * Override nesse método se to tipo K não for integer
     */
    public K convertId(String idString) {
        Object o = Integer.parseInt(idString);
        return (K) o;
    }
}