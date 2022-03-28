package sample.app;

import io.github.crudzilla.CRUDZillaForm;
import sample.domain.SampleEntity;

public class SampleEntityForm implements CRUDZillaForm<SampleEntity, Integer> {
    private Integer id;
    private String name;

    public Integer getId() {
        return id;
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
