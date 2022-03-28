package io.github.crudzilla;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.crudzilla.persistency.EntityNotFoundException;
import io.github.crudzilla.pojo.AutocompleteOption;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import sample.SampleSpringConfig;
import sample.app.SampleEntityForm;
import sample.domain.SampleEntity;
import sample.domain.SampleEntityRepository;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class CRUDZillaTest {

    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private EntityReflections entityReflections = mock(EntityReflections.class);
    private CRUDZilla crudZilla = new CRUDZilla(entityReflections, mapper);

    @Test
    void save__happy_path_for_new_entity() throws Exception {
        var beansAnnotated = mock(Map.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getBeansWithAnnotation(CRUDZillaGlobalConfig.class)).thenReturn(beansAnnotated);
        when(beansAnnotated.get(any())).thenReturn(new SampleSpringConfig());
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        when(entityReflections.getFormClass(sampleEntityClass)).then(io -> SampleEntityForm.class);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var builder = mock(EntityBuilder.class);
        when(entityReflections.getEntityBuilder(any())).thenReturn(builder);
        when(entityReflections.getNewEntity(any())).thenReturn(new SampleEntity());
        var entityBuilded = new SampleEntity();
        when(builder.buildNew(any(), any())).thenReturn(entityBuilded);
        when(repository.put(entityBuilded)).then(io -> io.getArgument(0));

        var returnedEntity = crudZilla.save("sample", """
                { "id":null, "name":"Renan"}
        """);

        assertThat(returnedEntity, is(entityBuilded));
    }

    @Test
    void save__happy_path_for_update() throws Exception {
        var beansAnnotated = mock(Map.class, RETURNS_DEEP_STUBS);
        when(applicationContext.getBeansWithAnnotation(CRUDZillaGlobalConfig.class)).thenReturn(beansAnnotated);
        when(beansAnnotated.get(any())).thenReturn(new SampleSpringConfig());
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        when(entityReflections.getFormClass(sampleEntityClass)).then(io -> SampleEntityForm.class);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var builder = mock(EntityBuilder.class);
        when(entityReflections.getEntityBuilder(any())).thenReturn(builder);
        var entityBuilded = new SampleEntity();
        when(repository.getEagerLoaded(any())).thenReturn(entityBuilded);
        when(builder.buildExisting(any(), any())).thenReturn(entityBuilded);
        when(repository.put(entityBuilded)).then(io -> io.getArgument(0));

        var returnedEntity = crudZilla.save("sample", """
                { "id":123, "name":"Renan"}
        """);

        assertThat(returnedEntity, is(entityBuilded));
    }

    @Test
    void save__happy_path_for_update_with_skip_persist() throws Exception {
        var sampleEntityClass = SampleEntity.class;
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var builder = mock(EntityBuilder.class);
        when(entityReflections.getEntityBuilder(any())).thenReturn(builder);
        var entityBuilded = new SampleEntity();
        when(repository.getEagerLoaded(any())).thenReturn(entityBuilded);
        when(builder.buildExisting(any(), any())).thenReturn(entityBuilded);
        when(repository.put(entityBuilded)).then(io -> io.getArgument(0));

        var form = new SampleEntityForm();
        form.setId(123);
        var returnedEntity = crudZilla.save(SampleEntity.class, form, null, true);

        assertThat(returnedEntity, is(entityBuilded));
    }

    @Test
    void save__happy_path_for_update_with_doBeforeSave() throws Exception {
        var sampleEntityClass = SampleEntity.class;
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var builder = mock(EntityBuilder.class);
        when(entityReflections.getEntityBuilder(any())).thenReturn(builder);
        var entityBuilded = new SampleEntity();
        when(repository.getEagerLoaded(any())).thenReturn(entityBuilded);
        when(builder.buildExisting(any(), any())).thenReturn(entityBuilded);
        when(repository.put(entityBuilded)).then(io -> io.getArgument(0));

        var form = new SampleEntityForm();
        form.setId(123);
        var doBeforeSaveWasCalled = new AtomicBoolean(false);
        var returnedEntity = crudZilla.save(SampleEntity.class, form, crudZillaEntity -> {
            doBeforeSaveWasCalled.set(true);
        });

        assertThat(returnedEntity, is(entityBuilded));
        assertThat(doBeforeSaveWasCalled.get(), is(true));
    }

    @Test
    void delete_should_call_repo() {
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository("sample")).thenReturn(repository);
        when(repository.convertId("123")).thenReturn(123);

        crudZilla.delete("sample", "123");

        verify(repository).remove(123);
    }

    @Test
    void getById__should_return_eagerLoaded_entity() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        when(repository.convertId("123")).thenReturn(123);
        var entity = new SampleEntity();
        when(repository.getEagerLoaded(123)).thenReturn(entity);

        var result = crudZilla.getById("sample", "123");

        assertThat(result, is(entity));
    }

    @Test
    void getAll__should_return_a_list_when_enabled() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        when(entityReflections.isGetAllDisabled(sampleEntityClass)).thenReturn(false);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var someList = new ArrayList<SampleEntity>();
        when(repository.getAll()).thenReturn(someList);

        var result = crudZilla.getAll("sample");

        assertSame(result, someList);
    }

    @Test
    void getAll__throws_when_disabled() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        when(entityReflections.isGetAllDisabled(sampleEntityClass)).thenReturn(true);

        assertThrows(EntityNotFoundException.class, () -> crudZilla.getAll("sample") );
    }

    @Test
    void getAutocompleteByTerm__transforms_the_entitys_into_dtos() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var entities = asList(
                createEntity(123, "Some Entity"),
                createEntity(999, "Other Entity")
        );
        when(repository.getByTerm("someQuery")).thenReturn(entities);

        var result = crudZilla.getAutocompleteByTerm("sample", "someQuery");

        for (SampleEntity entity : entities) {
            assertThat(result, hasItem(
                    allOf(
                            hasProperty("id", is(entity.getId().toString())),
                            hasProperty("name", is(entity.getLabel()))
                    )
            ));
        }
    }

    @Test
    void getAutocompleteByTermActive__transforms_the_entitys_into_dtos() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var entities = asList(
                createEntity(123, "Some Entity"),
                createEntity(999, "Other Entity")
        );
        when(repository.getByTermActive("someQuery")).thenReturn(entities);

        var result = crudZilla.getAutocompleteByTermActive("sample", "someQuery");

        for (SampleEntity entity : entities) {
            assertThat(result, hasItem(
                    allOf(
                            hasProperty("id", is(entity.getId().toString())),
                            hasProperty("name", is(entity.getLabel()))
                    )
            ));
        }
    }

    @Test
    void getAutocompleteByIds__should_return_autocompleteOption_by_id() {
        var sampleEntityClass = SampleEntity.class;
        when(entityReflections.getEntityClass("sample")).then(io -> sampleEntityClass);
        var repository = mock(SampleEntityRepository.class);
        when(entityReflections.getRepository(sampleEntityClass)).thenReturn(repository);
        var entities = asList(
                createEntity(123, "Some Entity"),
                createEntity(999, "Other Entity")
        );
        when(repository.getByIds(any())).thenReturn(entities);

        var result = crudZilla.getAutocompleteByIds("sample", asList("123", "999"));

        for (SampleEntity entity : entities) {
            assertThat(result, hasItem(
                    allOf(
                            hasProperty("id", is(entity.getId().toString())),
                            hasProperty("name", is(entity.getLabel()))
                    )
            ));
        }
    }

    private SampleEntity createEntity(int id, String name) {
        var e = new SampleEntity();
        e.setId(id);
        e.setName(name);
        return e;
    }
}