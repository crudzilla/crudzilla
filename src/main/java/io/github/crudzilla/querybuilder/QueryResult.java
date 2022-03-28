package io.github.crudzilla.querybuilder;

import java.util.List;

public class QueryResult<T> {

    private final List<T> data;
    private final Long count;

    public QueryResult(List<T> data, Long count) {
        this.data = data;
        this.count = count;
    }

    public List<T> getData() {
        return data;
    }

    public Long getCount() {
        return count;
    }

}