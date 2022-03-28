package sample.domain;

import io.github.crudzilla.CRUDZillaConfig;
import io.github.crudzilla.CRUDZillaEntidadeSecurity;
import io.github.crudzilla.CRUDZillaOperations;
import io.github.crudzilla.persistency.CRUDZillaEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@CRUDZillaConfig(key = "sample", disableGetAll = true, security = {
        @CRUDZillaEntidadeSecurity(operation = CRUDZillaOperations.SAVE, value = "GERENCIAR_USUARIOS"),
        @CRUDZillaEntidadeSecurity(operation = CRUDZillaOperations.DELETE, value = "GERENCIAR_USUARIOS"),
        @CRUDZillaEntidadeSecurity(operation = CRUDZillaOperations.GET_BY_ID, value = "GERENCIAR_USUARIOS"),
        @CRUDZillaEntidadeSecurity(operation = CRUDZillaOperations.SEARCH, value = "GERENCIAR_USUARIOS")
})
public class SampleEntity implements CRUDZillaEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(min = 1, max = 1000)
    private String name;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
