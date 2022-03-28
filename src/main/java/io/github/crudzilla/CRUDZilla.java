package io.github.crudzilla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.crudzilla.persistency.CRUDZillaEntity;
import io.github.crudzilla.persistency.EntityNotFoundException;
import io.github.crudzilla.pojo.AutocompleteOption;
import io.github.crudzilla.pojo.MultiselectOption;
import io.github.crudzilla.pojo.SelectOption;
import io.github.crudzilla.querybuilder.QueryBuilderJPASQL;
import io.github.crudzilla.querybuilder.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@SuppressWarnings({"unchecked", "rawtypes"})
@Service
public class CRUDZilla {

    private final EntityReflections entityReflections;
    private final ObjectMapper mapper;

    @Autowired
    public CRUDZilla(EntityReflections entityReflections, ObjectMapper mapper) {
        this.entityReflections = entityReflections;
        this.mapper = mapper;
    }

    @Transactional
    public CRUDZillaEntity save(String key, String formString) throws JsonProcessingException {
        var entidadeClass = entityReflections.getEntityClass(key);
        var formClass = entityReflections.getFormClass(entidadeClass);
        var form = mapper.readValue(formString, formClass);

        return save(entidadeClass, form, null);
    }

    CRUDZillaEntity save(Class<? extends CRUDZillaEntity<?>> entidadeClass, CRUDZillaForm form, Consumer<CRUDZillaEntity> doBeforeSave) {
        return this.save(entidadeClass, form,doBeforeSave,false);
    }

    CRUDZillaEntity save(Class<? extends CRUDZillaEntity<?>> entidadeClass, CRUDZillaForm form, Consumer<CRUDZillaEntity> doBeforeSave, boolean skipPersist) {
        var isNovo = form.getId() == null;
        CRUDZillaEntity entity;
        var repository = entityReflections.getRepository(entidadeClass);
        var entidadeBuilder = entityReflections.getEntityBuilder(entidadeClass);

        if (isNovo) {
            entity = entityReflections.getNewEntity(entidadeClass);
            entity = entidadeBuilder.buildNew(form, entity);
        } else {
            entity = repository.getEagerLoaded(form.getId());
            entidadeBuilder.buildExisting(form, entity);
        }

        if (doBeforeSave != null) {
            doBeforeSave.accept(entity);
        }

        return skipPersist ? entity : repository.put(entity);
    }

    @SuppressWarnings({"unchecked"})
    @Transactional
    public void delete(String key, String idString) {
        var repository = entityReflections.getRepository(key);
        repository.remove(repository.convertId(idString));
    }

    public CRUDZillaEntity getById(String key, String idString) {
        var entidadeClass = entityReflections.getEntityClass(key);
        var repository = entityReflections.getRepository(entidadeClass);
        return repository.getEagerLoaded(repository.convertId(idString));
    }

    public Collection<CRUDZillaEntity> getAll(String key) {
        var clazzName = entityReflections.getEntityClass(key);
        if (entityReflections.isGetAllDisabled(clazzName)) {
            throw new EntityNotFoundException(clazzName);
        }
        var repository = entityReflections.getRepository(clazzName);
        return repository.getAll();
    }

    public List<AutocompleteOption> getAutocompleteByTerm(String key, String termo) {
        var clazzName = entityReflections.getEntityClass(key);
        var repository = entityReflections.getRepository(clazzName);
        return (List<AutocompleteOption>) repository.getByTerm(termo)
                .stream()
                .map(x -> {
                    var entidade = (CRUDZillaEntity) x;
                    return new AutocompleteOption(
                            entidade.getId().toString(),
                            entidade.getLabel());
                })
                .collect(toList());
    }

    public List<AutocompleteOption> getAutocompleteByTermActive(String key, String termo) {
        var clazzName = entityReflections.getEntityClass(key);
        var repository = entityReflections.getRepository(clazzName);
        return (List<AutocompleteOption>) repository.getByTermActive(termo)
                .stream()
                .map(x -> {
                    var entidade = (CRUDZillaEntity) x;
                    return new AutocompleteOption(
                            entidade.getId().toString(),
                            entidade.getLabel());
                })
                .collect(toList());
    }

    public List<AutocompleteOption> getAutocompleteByIds(String key, List<String> idsString) {
        var clazzName = entityReflections.getEntityClass(key);
        var repository = entityReflections.getRepository(clazzName);
        return (List<AutocompleteOption>) repository.getByIds(idsString.stream().map(repository::convertId).collect(Collectors.toList()))
                .stream()
                .map(x -> {
                    var entidade = (CRUDZillaEntity) x;
                    return new AutocompleteOption(
                            entidade.getId().toString(),
                            entidade.getLabel());
                })
                .collect(toList());
    }

    public Collection<MultiselectOption> getAllMultiselect(String key) {
        return getAll(key)
                .stream()
                .map(x -> new MultiselectOption(x.getId().toString(), x.getLabel()))
                .collect(Collectors.toList());
    }

    public <T extends Type> List<SelectOption> getTypes(Class<T> className) {
        var enumConstants = className.getEnumConstants();
        return Arrays.stream(enumConstants)
                .map(x -> new SelectOption(x.getId(), x.getDescricao()))
                .collect(Collectors.toList());
    }

    public <T extends Type> List<MultiselectOption> getTypesAsMultiselect(Class<T> className) {
        var enumConstants = className.getEnumConstants();
        return Arrays.stream(enumConstants)
                .map(x -> new MultiselectOption(x.getId(), x.getDescricao()))
                .collect(Collectors.toList());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public QueryResult<Object> search(String key, Map<String, String> filtroParams) throws JsonProcessingException {
        var filtroClass = entityReflections.getFilterClassFromKey(key);
        QueryBuilderJPASQL queryBuilder = entityReflections.getQueryBuilder(filtroClass);
        var filter = mapper.readValue(mapper.writeValueAsString(filtroParams), filtroClass);
        return queryBuilder.build(filter);
    }

    public Object getProjection(String key, String projection, String params) {
        var repository = entityReflections.getRepository(key);
        var declaredMethods = repository.getClass().getDeclaredMethods();
        try {
            for (Method m : declaredMethods) {
                if (m.getName().equals(projection)) {
                    var parameterTypes = m.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        return m.invoke(repository);
                    }
                }
            }
        } catch (IllegalAccessException|InvocationTargetException ignored) {
        }
        throw new CRUDZillaProjectionNotFound();
    }

    @Transactional
    public void toggleActive(String key, String idString) {
        var entidadeClass = entityReflections.getEntityClass(key);
        var repository = entityReflections.getRepository(entidadeClass);
        var entity =  repository.getEagerLoaded(repository.convertId(idString));
        entity.setActive(!entity.isActive());
        repository.put(entity);
    }
}
