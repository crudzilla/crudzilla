package io.github.crudzilla.pojo;

public class MultiselectOption {

    private String id;
    private String value;

    public MultiselectOption() { /* Construtor basico necessário pro JSONMAPPER */ }

    public MultiselectOption(String id, String value) {
        this.id = id;
        this.value = value;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MultiselectOption that = (MultiselectOption) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
