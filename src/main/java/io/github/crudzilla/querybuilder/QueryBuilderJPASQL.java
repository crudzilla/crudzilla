package io.github.crudzilla.querybuilder;

import com.querydsl.core.support.QueryBase;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.sql.JPASQLQuery;
import com.querydsl.sql.RelationalPath;
import com.querydsl.sql.SQLTemplates;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class QueryBuilderJPASQL<F extends CRUDZillaFilter, T> {

    public static final String DEFAULT_SORTING = "nenhumaColunaSelecionada";
    public static final StringExpression STRING_NULL_EXPRESSION = Expressions.stringTemplate("CAST(NULL AS VARCHAR(1))");
    public static final NumberExpression<BigDecimal> DECIMAL_NULL_EXPRESSION = Expressions.numberTemplate(BigDecimal.class, "CAST(NULL AS BIGINT)");
    public static final BooleanExpression BOOLEAN_NULL_EXPRESSION = Expressions.booleanTemplate("CAST(NULL AS SMALLINT)");
    public static final DateExpression DATE_NULL_EXPRESSION = Expressions.dateTemplate(LocalDate.class, "CAST (NULL AS DATE)");
    public static final DateTimeExpression DATETIME_NULL_EXPRESSION = Expressions.dateTimeTemplate(Timestamp.class, "CAST (NULL AS TIMESTAMP)");

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected SQLTemplates sqlTemplate;

    public abstract Expression<? extends Comparable> getOrderByExpression(String column);

    public abstract JPASQLQuery<T> createQuery(F filter);

    public QueryResult<T> build(F filtro) {
        JPASQLQuery<T> query = createQuery(filtro);
        long totalRegistros = getCount(query);
        addOrderBy(filtro, query);
        addPaging(filtro, query);
        var queryResult = new QueryResult<>(query.fetch(), totalRegistros);
        postProcessResults(queryResult);
        return queryResult;
    }

    protected void postProcessResults(QueryResult<T> queryResult) {
        //Override this method if you need extra behavior after fetching the results
    }

    protected boolean isColumnSelected(String columnName, List<String> selectedColumns) {
        return selectedColumns != null && selectedColumns.contains(columnName);
    }

    protected <W extends QueryBase<W>> void groupIfColumnSelected(QueryBase<W> query,
                                                                  String columnName,
                                                                  List<String> selectedColumns,
                                                                  Expression<?>... path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            query.groupBy(path);
        }
    }

    protected <W extends QueryBase<W>> void groupIfColumnSelected(QueryBase<W> query,
                                                                  List<String> nomeColunas,
                                                                  List<String> colunasSelecionadas,
                                                                  Expression<?>... caminhoColunaNoBanco)
    {
        boolean temTodasColunas = true;
        for (String coluna : nomeColunas) {
            if (!isColumnSelected(coluna, colunasSelecionadas)) {
                temTodasColunas = false;
                break;
            }
        }
        if (temTodasColunas) {
            query.groupBy(caminhoColunaNoBanco);
        }
    }

    protected void addJoinIfNeeded(JPASQLQuery<T> query, RelationalPath queryDslTable, BooleanExpression join) {
        if (query.getSQL().getSQL().contains((CharSequence) queryDslTable.getMetadata().getElement())) {
            query.innerJoin(queryDslTable).on(join);
        }
    }

    protected void addLeftJoinIfNeeded(JPASQLQuery<T> query, RelationalPath queryDslTable, BooleanExpression join) {
        if (query.getSQL().getSQL().contains((CharSequence) queryDslTable.getMetadata().getElement())) {
            query.leftJoin(queryDslTable).on(join);
        }
    }

    protected static <C> void filterIfNotEmpty(JPASQLQuery query, Collection<C> filter, SimpleExpression<C> path) {
        if (isNotEmpty(filter)) {
            query.where(path.in(filter));
        }
    }

    protected static <C> void filterIfNotEmpty(JPASQLQuery query, Collection<C> filter, Supplier<BooleanExpression> whereLambda) {
        if (isNotEmpty(filter)) {
            query.where(whereLambda.get());
        }
    }

    protected static void filterIfPresent(JPASQLQuery query, String filterItem, Supplier<BooleanExpression> whereLambda) {
        if (isNotBlank(filterItem)) {
            query.where(whereLambda.get());
        }
    }

    protected static void filterIfPresent(JPASQLQuery query, Object filterItem, Supplier<BooleanExpression> whereLambda) {
        if (filterItem != null) {
            query.where(whereLambda.get());
        }
    }

    protected void havingIfPresent(JPASQLQuery query, Object filterItem, Supplier<BooleanExpression> havingLambda) {
        if (filterItem != null) {
            query.having(havingLambda.get());
        }
    }

    protected void addPaging(F filter, JPASQLQuery<T> query) {
        if (filter.getPageSize() >= 0 ) {
            query.limit(filter.getPageSize())
                    .offset(filter.getOffset());
        }
    }

    @SuppressWarnings("unchecked")
    protected void addOrderBy(F filter, JPASQLQuery<T> query) {
        Order dir = filter.getSortOrder() == null ? Order.ASC : filter.getSortOrder();
        String col = filter.getSortColumn() != null ? filter.getSortColumn() : "";
        Expression<? extends Comparable> colunaOrdenacao = getOrderByExpression(col);
        query.orderBy(new OrderSpecifier<>(dir, colunaOrdenacao));
    }

    protected long getCount(JPASQLQuery<T> query) {
        return new JPASQLQuery<T>(entityManager, sqlTemplate)
                .from(query, new BeanPath<Object>(String.class, "count_from_alias"))
                .fetchCount();
    }

    public StringExpression addIfColumnSelected(
            String columnName, List<String> selectedColumns, String alias, StringExpression path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            return path.as(alias);
        }
        return STRING_NULL_EXPRESSION.as(alias);
    }

    public DateExpression addIfColumnSelected(
            String columnName, List<String> selectedColumns, String alias, DateExpression path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            return path.as(alias);
        }
        return DATE_NULL_EXPRESSION.as(alias);
    }

    public DateTimeExpression addIfColumnSelected(
            String columnName, List<String> selectedColumns, String alias, DateTimeExpression path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            return path.as(alias);
        }
        return DATETIME_NULL_EXPRESSION.as(alias);
    }

    @SuppressWarnings("unchecked")
    public <N extends Number & Comparable<?>> NumberExpression<N> addIfColumnSelected(
            String columnName, List<String> selectedColumns, String alias, NumberExpression<N> path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            return path.as(alias);
        }
        return (NumberExpression<N>) DECIMAL_NULL_EXPRESSION.as(alias);
    }

    @SuppressWarnings("unchecked")
    public <N extends Number & Comparable<?>> NumberExpression<N> addIfAnyColumnSelected(
            List<String> columnNames, List<String> selectedColumns, String alias, NumberExpression<N> path)
    {

        for (String coluna :columnNames)
        {
            if (isColumnSelected(coluna, selectedColumns)) {
                return path.as(alias);
            }
        }
        return (NumberExpression<N>) DECIMAL_NULL_EXPRESSION.as(alias);
    }

    public BooleanExpression addIfColumnSelected(
            String columnName, List<String> selectedColumns, String alias, BooleanExpression path)
    {
        if (isColumnSelected(columnName, selectedColumns)) {
            return path.as(alias);
        }
        return BOOLEAN_NULL_EXPRESSION.as(alias);
    }


}