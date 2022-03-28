package io.github.crudzilla.pojo;

public class AutocompleteOption {

    private String id;
    private String name;

    public AutocompleteOption() { /* Construtor basico necessário pro JSONMAPPER */ }

    public AutocompleteOption(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AutocompleteOption that = (AutocompleteOption) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
