package io.github.crudzilla.querybuilder;

import com.querydsl.core.types.Order;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class CRUDZillaFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDZillaFilter.class);

    @ApiParam(value = "Sorting order", defaultValue = "ASC")
    private Order sortOrder = Order.ASC;

    @ApiParam(value = "Column for sorting")
    private String sortColumn = "";

    @ApiParam(value = "Result page size", defaultValue = "15")
    private int pageSize = 15;

    @ApiParam(value = "Offset for the results", defaultValue = "0")
    private int offset = 0;

    public Order getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Order sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        if (pageSize <= 100) {
            this.pageSize = pageSize;
        } else {
            this.pageSize = 100;
        }
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Isso é necessário para poder gerar a Key do Cache corretamente.
     */
    @Override
    public int hashCode() {
        StringBuilder hash = new StringBuilder("");
        for (Field field : this.getClass().getDeclaredFields() ){
            field.setAccessible(true);
            Object value;
            try {
                value = field.get(this);
            } catch (IllegalAccessException e) {
                LOGGER.error(String.format("Error creating hash for filter %s at field %s", this.getClass().getName(), field.getName()), e);
                throw new RuntimeException(e);
            }
            if (value != null) {
                hash.append(value.hashCode());
            }
            field.setAccessible(false);
        }

        return hash.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && this.getClass().equals(o.getClass())
                && EqualsBuilder.reflectionEquals(this, o, true);
    }
}
