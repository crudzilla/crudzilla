package io.github.crudzilla.pojo;

public class SelectOption {

    private String text;
    private String value;

    public SelectOption() { /* Construtor basico necessário pro JSONMAPPER */ }

    public SelectOption(String value, String text) {
        this.text = text;
        this.value = value;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SelectOption that = (SelectOption) o;

        return text.equals(that.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
