package io.github.crudzilla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import infra.ControllerTest;
import io.github.crudzilla.persistency.CRUDZillaEntity;
import io.github.crudzilla.persistency.InvalidEntityException;
import io.github.crudzilla.pojo.AutocompleteOption;
import io.github.crudzilla.pojo.MultiselectOption;
import io.github.crudzilla.pojo.SelectOption;
import io.github.crudzilla.querybuilder.QueryResult;
import org.junit.jupiter.api.Test;
import sample.domain.SampleEntity;
import sample.SampleProjection;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class CRUDZillaControllerTest extends ControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final CRUDZilla crudZilla = mock(CRUDZilla.class);

    private final CRUDZillaController controller = new CRUDZillaController(mapper, crudZilla);

    @Override
    public Object getController() {
        return controller;
    }

    @Test
    void search__should_return_OK() throws Exception {
        when(crudZilla.search(any(), anyMap())).thenReturn(new QueryResult<>(Collections.emptyList(), 0L));
        mockMvc.perform(get("/api/auth/crudzilla/myentity"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "data":[], "count":0 }
                        """));
    }

    @Test
    void search__should_return_400_when_exception_parsing_parameters() throws Exception {
        var exception = mock(JsonProcessingException.class);
        when(exception.getMessage()).thenReturn("some error occured");
        when(crudZilla.search(any(), anyMap())).thenThrow(exception);

        mockMvc.perform(get("/api/auth/crudzilla/myentity"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("some error occured"));
    }

    @Test
    void save__should_call_crudzilla_and_return_entity_and_OK() throws Exception {
        var content = """
                { "name": "CRUDZILLA!!!" }
                """;
        var entity = mock(CRUDZillaEntity.class);
        when(entity.getId()).thenReturn(12345);
        when(crudZilla.save("myentity", content)).thenReturn(entity);
        mockMvc.perform(post("/api/auth/crudzilla/myentity")
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().string("12345"));
    }

    @Test
    void save__should_return_bad_request_when_json_parse_problem() throws Exception {
        var exception = mock(JsonProcessingException.class);
        var content = "some invalid json";
        when(exception.getMessage()).thenReturn("some error occured");
        when(crudZilla.save("myentity", content)).thenThrow(exception);
        mockMvc.perform(post("/api/auth/crudzilla/myentity")
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("some error occured"));
    }

    @Test
    void save__should_return_bad_request_when_validation_error_occurs() throws Exception {
        var exception = new InvalidEntityException(asList("some field is required", "some other problem"));
        var content = "some invalid json";
        when(crudZilla.save("myentity", content)).thenThrow(exception);
        mockMvc.perform(post("/api/auth/crudzilla/myentity")
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                        ["some field is required", "some other problem"]
                        """));
    }

    @Test
    void toggleActive__should_call_crudzilla_and_return_ok() throws Exception {
        mockMvc.perform(post("/api/auth/crudzilla/myentity/123/toggle-active")
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(crudZilla).toggleActive("myentity", "123");
    }

    @Test
    void delete_should_call_crudzilla_and_return_ok() throws Exception {
        mockMvc.perform(delete("/api/auth/crudzilla/myentity/123")
                        .contentType("application/json"))
                .andExpect(status().isOk());

        verify(crudZilla).delete("myentity", "123");
    }

    @Test
    void getById__should_return_entity() throws Exception {
        SampleEntity sampleEntity = createSampleEntity(1234, "Some example");
        when(crudZilla.getById("sample", "1234")).thenReturn(sampleEntity);
        mockMvc.perform(get("/api/auth/crudzilla/sample/1234"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        { "id":1234, "name":"Some example", "label":"Some example", "active":true }
                        """, true));
    }

    @Test
    @SuppressWarnings("rawtypes")
    void getAll__should_return_all_entitys() throws Exception {
        List<CRUDZillaEntity> all = asList(
                createSampleEntity(1234, "Some example"),
                createSampleEntity(999, "Some example 2")
        );
        when(crudZilla.getAll("sample")).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample/all"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":1234,"name":"Some example","label":"Some example","active":true},
                        {"id":999,"name":"Some example 2","label":"Some example 2","active":true}]
                        """, true));
    }

    @Test
    void autocomplete__should_return_dto() throws Exception {
        var all = asList(
                new AutocompleteOption("1", "Abc"),
                new AutocompleteOption("2", "Abc2")
        );
        var searchTerm = "abc";
        when(crudZilla.getAutocompleteByTerm("sample", searchTerm)).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample/autocomplete")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":"1","name":"Abc"},
                        {"id":"2","name":"Abc2"}]
                        """, true));
    }

    @Test
    void autocompleteActive__should_return_dto() throws Exception {
        var all = asList(
                new AutocompleteOption("1", "Abc"),
                new AutocompleteOption("2", "Abc2")
        );
        var searchTerm = "abc";
        when(crudZilla.getAutocompleteByTermActive("sample", searchTerm)).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample/autocomplete/active")
                        .param("q", searchTerm))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":"1","name":"Abc"},
                        {"id":"2","name":"Abc2"}]
                        """, true));
    }

    @Test
    void autocompleteIds__should_return_dto() throws Exception {
        var all = asList(
                new AutocompleteOption("1", "Abc"),
                new AutocompleteOption("2", "Abc2")
        );
        var ids = asList("1", "2");
        when(crudZilla.getAutocompleteByIds("sample", ids)).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample/autocomplete/ids")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":"1","name":"Abc"},
                        {"id":"2","name":"Abc2"}]
                        """, true));
    }

    @Test
    void multiselect__should_return_dto() throws Exception {
        var all = asList(
                new MultiselectOption("1", "Abc"),
                new MultiselectOption("2", "Abc2")
        );
        var ids = asList("1", "2");
        when(crudZilla.getAllMultiselect("sample")).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample/multiselect")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":"1","value":"Abc"},
                        {"id":"2","value":"Abc2"}]
                        """, true));
    }

    @Test
    void types__should_return_dtos() throws Exception {
        var all = asList(
                new SelectOption("1", "Abc"),
                new SelectOption("2", "Abc2")
        );
        var ids = asList("1", "2");
        when(crudZilla.getTypes(any())).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample.SampleType/types"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"value":"1","text":"Abc"},
                        {"value":"2","text":"Abc2"}]
                        """, true));
    }

    @Test
    void typesMultiselect__should_return_dtos() throws Exception {
        var all = asList(
                new MultiselectOption("1", "Abc"),
                new MultiselectOption("2", "Abc2")
        );
        var ids = asList("1", "2");
        when(crudZilla.getTypesAsMultiselect(any())).thenReturn(all);
        mockMvc.perform(get("/api/auth/crudzilla/sample.SampleType/types/multiselect"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":"1","value":"Abc"},
                        {"id":"2","value":"Abc2"}]
                        """, true));
    }

    @Test
    void projection__should_return_results() throws Exception {
        var projections = asList(
                new SampleProjection("Renan", 100),
                new SampleProjection("Val", 200)
        );

        when(crudZilla.getProjection("sample", "getSumByName","someJsonGoesHere")).thenReturn(projections);
        mockMvc.perform(get("/api/auth/crudzilla/sample/projection/getSumByName")
                        .content("someJsonGoesHere")
                        .contentType("application/json")
                )
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"name":"Renan","sum":100},
                        {"name":"Val","sum":200}]
                        """, true));
    }

    private SampleEntity createSampleEntity(int id, String name) {
        var sampleEntity = new SampleEntity();
        sampleEntity.setId(id);
        sampleEntity.setName(name);
        return sampleEntity;
    }
}