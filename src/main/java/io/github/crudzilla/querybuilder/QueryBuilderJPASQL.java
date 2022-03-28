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

    public static final String NENHUMA_COLUNA_FOI_SELECIONADA_USE_A_ORDENACAO_PADRAO = "nenhumaColunaSelecionada";
    public static final StringExpression STRING_NULL_EXPRESSION = Expressions.stringTemplate("CAST(NULL AS VARCHAR(1))");
    public static final NumberExpression<BigDecimal> DECIMAL_NULL_EXPRESSION = Expressions.numberTemplate(BigDecimal.class, "CAST(NULL AS BIGINT)");
    public static final BooleanExpression BOOLEAN_NULL_EXPRESSION = Expressions.booleanTemplate("CAST(NULL AS SMALLINT)");
    public static final DateExpression DATE_NULL_EXPRESSION = Expressions.dateTemplate(LocalDate.class, "CAST (NULL AS DATE)");
    public static final DateTimeExpression DATETIME_NULL_EXPRESSION = Expressions.dateTimeTemplate(Timestamp.class, "CAST (NULL AS TIMESTAMP)");

    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected SQLTemplates sqlTemplate;

    public abstract Expression<? extends Comparable> getOrderByExpression(String coluna);

    public abstract JPASQLQuery<T> gerarQuery(F filtro);

    public QueryResult<T> build(F filtro) {
        JPASQLQuery<T> query = gerarQuery(filtro);
        long totalRegistros = buscarTotalRegistros(query);
        adicionarOrderBy(filtro, query);
        adicionarPaginacao(filtro, query);
        var queryResult = new QueryResult<>(query.fetch(), totalRegistros);
        postProcessResults(queryResult);
        return queryResult;
    }

    protected void postProcessResults(QueryResult<T> queryResult) {
        //Override this method if you need extra behavior after fetching the results
    }

    protected boolean isColunaSelecionada(String nomeColuna, List<String> colunasSelecionadas) {
        return colunasSelecionadas != null && colunasSelecionadas.contains(nomeColuna);
    }

    protected <W extends QueryBase<W>> void agruparSeColunaFoiSelecionada(QueryBase<W> query,
                                                                          String nomeColuna,
                                                                          List<String> colunasSelecionadas,
                                                                          Expression<?>... caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            query.groupBy(caminhoColunaNoBanco);
        }
    }

    protected <W extends QueryBase<W>> void agruparSeColunaFoiSelecionada(QueryBase<W> query,
                                                                          List<String> nomeColunas,
                                                                          List<String> colunasSelecionadas,
                                                                          Expression<?>... caminhoColunaNoBanco)
    {
        boolean temTodasColunas = true;
        for (String coluna : nomeColunas) {
            if (!isColunaSelecionada(coluna, colunasSelecionadas)) {
                temTodasColunas = false;
                break;
            }
        }
        if (temTodasColunas) {
            query.groupBy(caminhoColunaNoBanco);
        }
    }

    protected void adicionarJoinSeNecessario(JPASQLQuery<T> query, RelationalPath tabela, BooleanExpression join) {
        if (query.getSQL().getSQL().contains((CharSequence) tabela.getMetadata().getElement())) {
            query.innerJoin(tabela).on(join);
        }
    }

    protected void adicionarLeftJoinSeNecessario(JPASQLQuery<T> query, RelationalPath tabela, BooleanExpression join) {
        if (query.getSQL().getSQL().contains((CharSequence) tabela.getMetadata().getElement())) {
            query.leftJoin(tabela).on(join);
        }
    }

    protected static <C> void filtrarSeNaoForVazio(JPASQLQuery query, Collection<C> filtro, SimpleExpression<C> path) {
        if (isNotEmpty(filtro)) {
            query.where(path.in(filtro));
        }
    }

    protected static <C> void filtrarSeNaoForVazio(JPASQLQuery query, Collection<C> filtro, Supplier<BooleanExpression> whereLambda) {
        if (isNotEmpty(filtro)) {
            query.where(whereLambda.get());
        }
    }

    protected static void filtrarSePreenchido(JPASQLQuery query, String itemFiltro, Supplier<BooleanExpression> whereLambda) {
        if (isNotBlank(itemFiltro)) {
            query.where(whereLambda.get());
        }
    }

    protected static void filtrarSePreenchido(JPASQLQuery query, Object itemFiltro, Supplier<BooleanExpression> whereLambda) {
        if (itemFiltro != null) {
            query.where(whereLambda.get());
        }
    }

    protected void havingSePreenchido(JPASQLQuery query, Object itemFiltro, Supplier<BooleanExpression> havingLambda) {
        if (itemFiltro != null) {
            query.having(havingLambda.get());
        }
    }

    protected void adicionarPaginacao(F filtro, JPASQLQuery<T> query) {
        if (filtro.getTamanhoPagina() >= 0 ) {
            query.limit(filtro.getTamanhoPagina())
                    .offset(filtro.getOffset());
        }
    }

    @SuppressWarnings("unchecked")
    protected void adicionarOrderBy(F filtro, JPASQLQuery<T> query) {
        Order direcaoOrdenacao = filtro.getDirecaoOrdenacao() == null ? Order.ASC : filtro.getDirecaoOrdenacao();
        String colunaDeOrdenacao = filtro.getColunaOrdenacao() != null ? filtro.getColunaOrdenacao() : "";
        Expression<? extends Comparable> colunaOrdenacao = getOrderByExpression(colunaDeOrdenacao);
        query.orderBy(new OrderSpecifier<>(direcaoOrdenacao, colunaOrdenacao));
    }

    protected long buscarTotalRegistros(JPASQLQuery<T> query) {
        return new JPASQLQuery<T>(entityManager, sqlTemplate)
                .from(query, new BeanPath<Object>(String.class, "count_from_alias"))
                .fetchCount();
    }

    public StringExpression inserirSeColunaFoiSelecionada(
            String nomeColuna, List<String> colunasSelecionadas, String alias, StringExpression caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            return caminhoColunaNoBanco.as(alias);
        }
        return STRING_NULL_EXPRESSION.as(alias);
    }

    public DateExpression inserirSeColunaFoiSelecionada(
            String nomeColuna, List<String> colunasSelecionadas, String alias, DateExpression caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            return caminhoColunaNoBanco.as(alias);
        }
        return DATE_NULL_EXPRESSION.as(alias);
    }

    public DateTimeExpression inserirSeColunaFoiSelecionada(
            String nomeColuna, List<String> colunasSelecionadas, String alias, DateTimeExpression caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            return caminhoColunaNoBanco.as(alias);
        }
        return DATETIME_NULL_EXPRESSION.as(alias);
    }

    @SuppressWarnings("unchecked")
    public <N extends Number & Comparable<?>> NumberExpression<N> inserirSeColunaFoiSelecionada(
            String nomeColuna, List<String> colunasSelecionadas, String alias, NumberExpression<N> caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            return caminhoColunaNoBanco.as(alias);
        }
        return (NumberExpression<N>) DECIMAL_NULL_EXPRESSION.as(alias);
    }

    @SuppressWarnings("unchecked")
    public <N extends Number & Comparable<?>> NumberExpression<N> inserirSeAlgumaColunaFoiSelecionada(
            List<String> nomeColunasVerificarSeForamSelecionadas, List<String> colunasSelecionadas, String alias, NumberExpression<N> caminhoColunaNoBanco)
    {

        for (String coluna :nomeColunasVerificarSeForamSelecionadas)
        {
            if (isColunaSelecionada(coluna, colunasSelecionadas)) {
                return caminhoColunaNoBanco.as(alias);
            }
        }
        return (NumberExpression<N>) DECIMAL_NULL_EXPRESSION.as(alias);
    }

    public BooleanExpression inserirSeColunaFoiSelecionada(
            String nomeColuna, List<String> colunasSelecionadas, String alias, BooleanExpression caminhoColunaNoBanco)
    {
        if (isColunaSelecionada(nomeColuna, colunasSelecionadas)) {
            return caminhoColunaNoBanco.as(alias);
        }
        return BOOLEAN_NULL_EXPRESSION.as(alias);
    }


}