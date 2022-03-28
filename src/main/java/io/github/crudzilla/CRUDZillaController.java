package io.github.crudzilla;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.crudzilla.persistency.CRUDZillaEntity;
import io.github.crudzilla.persistency.InvalidEntityException;
import io.github.crudzilla.pojo.AutocompleteOption;
import io.github.crudzilla.pojo.MultiselectOption;
import io.github.crudzilla.pojo.SelectOption;
import io.github.crudzilla.querybuilder.QueryResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@SuppressWarnings("ALL")
@RestController
public class CRUDZillaController {

    private final ObjectMapper mapper;
    private final CRUDZilla crudZilla;

    @Autowired
    public CRUDZillaController(ObjectMapper mapper, CRUDZilla crudZilla) {
        this.mapper = mapper;
        this.crudZilla = crudZilla;
    }

    //O XHR do javascript não suporta Body em mensagens GET, então o filtro tem q ir como params.
    //Isso gera uma complicação, pois dps tem q converter esse Params no objeto Filtro
    @ApiOperation(value = "Consulta de Entidades", tags = "Entidades")
    @GetMapping(value = "/api/auth/crudzilla/{key}")
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).SEARCH)")
    public ResponseEntity<?> search(
            @PathVariable String key,
            @RequestParam Map<String,String> params
    ) throws Exception {

        try {
            QueryResult<Object> resposta = crudZilla.search(key, params);
            return ResponseEntity.ok(resposta);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

    }

    @ApiOperation(value = "Salvar Entidade", notes = "Salvar uma <Entidade> nova ou edita uma existente.", tags = "Entidades")
    @PostMapping(value="/api/auth/crudzilla/{key}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).SAVE)")
    public ResponseEntity<?> save(
            @PathVariable String key,
            @RequestBody String formString
    ) {
        try {
            var entity = crudZilla.save(key, formString);
            return ResponseEntity.ok(entity.getId());
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (InvalidEntityException e) {
            return ResponseEntity.badRequest().body(e.getMensagens());
        }
    }

    @ApiOperation(value = "Ativar ou Desativar Entidade", tags = "Entidades")
    @PostMapping(value="/api/auth/crudzilla/{key}/{id}/toggle-active", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).SAVE)")
    public ResponseEntity<?> toggleActive(@PathVariable String key, @PathVariable String id) {
        crudZilla.toggleActive(key, id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Excluir Entidade", notes = "Exclui uma Entidade se possível.", tags = "Entidades")
    @DeleteMapping(value="/api/auth/crudzilla/{key}/{id}")
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).DELETE)")
    public ResponseEntity<?> delete(
            @PathVariable String key, @PathVariable String id
    ) {
        crudZilla.delete(key, id);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Consulta de Entidade por ID", tags = "Entidades")
    @GetMapping(value = "/api/auth/crudzilla/{key}/{idString}")
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).GET_BY_ID)")
    public CRUDZillaEntity getById(@PathVariable String key, @PathVariable String idString) {
        return crudZilla.getById(key, idString);
    }

    @ApiOperation(value = "Obtém todas Entidades", tags = "Entidades")
    @GetMapping(value = "/api/auth/crudzilla/{key}/all")
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).GET_ALL)")
    public Collection<CRUDZillaEntity> getAll(@PathVariable String key) {
        return crudZilla.getAll(key);
    }

    @GetMapping(value = "/api/auth/crudzilla/{key}/autocomplete", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public <E extends CRUDZillaEntity<K>, K> List<AutocompleteOption> autocomplete(@PathVariable String key, @RequestParam String q) {
        return crudZilla.getAutocompleteByTerm(key, q);
    }

    @GetMapping(value = "/api/auth/crudzilla/{key}/autocomplete/active", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public <E extends CRUDZillaEntity<K>, K> List<AutocompleteOption> autocompleteActive(@PathVariable String key, @RequestParam String q) {
        return crudZilla.getAutocompleteByTermActive(key, q);
    }

    @GetMapping(value = "/api/auth/crudzilla/{key}/autocomplete/ids", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public <E extends CRUDZillaEntity<K>, K> List<AutocompleteOption> autocompleteByIds(@PathVariable String key, @RequestParam List<String> ids) {
        return crudZilla.getAutocompleteByIds(key, ids);
    }

    @ApiOperation(value = "Obtém todas Entidades em formato MultiselectOption", tags = "Entidades")
    @GetMapping(value = "/api/auth/crudzilla/{key}/multiselect")
    @PreAuthorize("@crudZillaSecurity.check(#key, T(io.github.crudzilla.CRUDZillaOperations).GET_ALL)")
    public Collection<MultiselectOption> multiselect(@PathVariable String key) {
        return crudZilla.getAllMultiselect(key);
    }

    @GetMapping(value = "/api/auth/crudzilla/{clazzName}/types")
    public <T extends Type> List<SelectOption> types(@PathVariable Class<T> clazzName) {
        return crudZilla.getTypes(clazzName);
    }

    @GetMapping(value = "/api/auth/crudzilla/{clazzName}/types/multiselect")
    public <T extends Type> List<MultiselectOption> typesMultiselect(@PathVariable Class<T> clazzName) {
        return crudZilla.getTypesAsMultiselect(clazzName);
    }

    @GetMapping(value = "/api/auth/crudzilla/{key}/projection/{projection}")
    public Object tiposMultiselect(@PathVariable String key, @PathVariable String projection, @RequestBody(required = false) String params) {
        return crudZilla.getProjection(key, projection, params);
    }

}