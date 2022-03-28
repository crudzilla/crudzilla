package io.github.crudzilla.persistency;

import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.SQLTemplates;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public abstract class AbstractRepositoryJPA<E extends CRUDZillaEntity<K>, K> {

    @Autowired
    private Validator validator;
    @Autowired
    private SQLTemplates sqlTemplate;

    /**
     * Método a abstrato que retorna o entity manager utilizado nas transações.
     *
     * @return Retorna o entity manager utilizado nas transações do repositório.
     */
    protected abstract EntityManager getEntityManager();

    /**
     * Obtém uma entidade a partir de seu ID. Caso a entidade não seja encontrada, uma exceção será lançada.
     *
     * @param id ID da entidade a ser buscada.
     * @return A entidade persistente com o id passado.
     * @throws EntityNotFoundException Caso nenhuma entidade com o
     *                                        id passado seja encontrada.
     */
    public E get(K id) {
        E entidade = getEntityManager().find(getConcreteEntityClass(), id);
        if (entidade == null) {
            throw new EntityNotFoundException(getConcreteEntityClass(), id);
        }
        return entidade;
    }

    /**
     * Obtém uma entidade forçando um eager load de todos fields com @OneToMany e @ManyToMany.
     *
     * @param id ID da entidade a ser buscada.
     * @return A entidade persistente com o id passado.
     * @throws EntityNotFoundException Caso nenhuma entidade com o
     *                                        id passado seja encontrada.
     */
    @Transactional
    public E getEagerLoaded(K id) {
        E entity = get(id);
        JPAUtils.initializeObject(entity);
        return entity;
    }

    /**
     * Remove uma entidade a partir do ID
     *
     * @param id ID da entidade a ser removida
     * @throws EntityNotFoundException Caso nenhuma entidade com o
     *                                        id passado seja encontrada.
     */
    public void remove(K id) {
        E entidade = get(id);
        getEntityManager().remove(entidade);
    }

    /**
     * Remove uma entidade
     *
     * @param entidade que será removida
     * @throws EntityNotFoundException Caso nenhuma entidade com o
     *                                        id passado seja encontrada.
     */
    public void remove(E entidade) {
        getEntityManager().remove(entidade);
    }

    /**
     * Remove entidades que cujo id esteja presente na lista
     */
    public void removeByIds(List<K> ids) {
        ids.forEach(this::remove);
    }

    /**
     * Remove uma lista de entidades
     */
    public void remove(List<E> entidades) {
        entidades.forEach(this::remove);
    }

    /**
     * Obtém todas as entidades do tipo específico no banco.
     *
     * @return Uma lista contendo todas as entidades armazenadas do tipo.
     */
    public List<E> getAll() {
        return new JPAQueryFactory(getEntityManager()).selectFrom(getQEntity()).fetch();
    }

    /**
     * Obtém um JPAQuery
     */
    public JPAQueryFactory getJPAQuery() {
        return new JPAQueryFactory(getEntityManager());
    }

    public JPASQLQuery getJPASQLQuery() {
        return new JPASQLQuery(getEntityManager(), sqlTemplate);
    }

    /**
     * Valida e, caso não existam violações, insere ou atualiza a entity passada
     * na camada de persistência.
     *
     * @param entity Entidade a ser validada e inserida/atualizada.
     * @return Entidade após o salvamento. Quando a operação é de atualização, a entity
     * retornada <b>pode</b> ser diferente da passada, se já existir uma instância gerenciada
     * da mesma classe e ID.
     */
    public E put(E entity) {
        validate(entity);
        if (entity.getId() == null) {
            getEntityManager().persist(entity);
            return entity;
        }
        return getEntityManager().merge(entity);
    }

    /**
     * Valida a entity passada. Caso encontre violações de restrições, uma
     * {@link InvalidEntityException} será lançada.
     *
     * @param entity Entidade a ser validada.
     * @throws InvalidEntityException Caso a entity passada viole
     *                                   alguma restrição (constraint).
     */
    public void validate(E entity) {
        Set<ConstraintViolation<E>> violations = validator.validate(entity);
        if (!violations.isEmpty()) {
            throw new InvalidEntityException(violations);
        }
    }

    /**
     * <p>Executa o flush no <code>EntityManager</code>, sincronizando o contexto de persistência com o banco de dados.</p>
     * <p>
     * <em><strong>NOTA:</strong> O uso deste método é desencorajado, pois ele interfere com a política de gestão de
     * persistência do <code>EntityManager</code> (que, sem o flush, poderia decidir o melhor momento para realizar a sincrionia),
     * podendo gerar impactos de performance, além de gerar um acoplamento indesejado entre o código de negócio/aplicação
     * e os detalhes/políticas da camada de persistência.</em>
     */
    public void flush() {
        getEntityManager().flush();
    }

    @SuppressWarnings("unchecked")
    public EntityPathBase<E> getQEntity() {
        String nomeCompletoClasseQ = getConcreteEntityClass().getPackage().getName() + ".Q" + getConcreteEntityClass().getSimpleName();
        try {
            Class<?> clazz = Class.forName(nomeCompletoClasseQ);
            String simpleNameQClass = StringUtils.uncapitalize(getConcreteEntityClass().getSimpleName());
            return (EntityPathBase<E>) clazz.getField(simpleNameQClass).get(null);
        } catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e) {
            throw new EntityNotMappedException("A classe " + nomeCompletoClasseQ + " não foi encontrada no classpath", e);
        }
    }

    /**
     * Retorna o objeto <code>Class&lt;E&gt;</code> da classe concreta usada, ou seja, o equivalente a: <code>E.class</code>.
     */
    @SuppressWarnings("unchecked")
    private Class<E> getConcreteEntityClass() {
        ParameterizedType genericSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<E>) genericSuperclass.getActualTypeArguments()[0];
    }

    public abstract <K> K convertId(String idString);

    public List<E> getByTerm(String q) {
        throw new NotImplementedException();
    }

    public List<E> getByIds(List<K> ids) {
        throw new NotImplementedException();
    }

    public List<E> getByTermActive(String q) {
        throw new NotImplementedException();
    }
}
