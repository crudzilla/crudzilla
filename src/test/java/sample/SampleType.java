package sample;

import io.github.crudzilla.Type;

public enum SampleType implements Type {
    TYPE_1(1, "Hello"),
    TYPE_2(2, "Hello 2");

    private final int id;
    private final String description;

    SampleType(int id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public String getId() {
        return id + "";
    }

    @Override
    public String getDescricao() {
        return description;
    }
}
