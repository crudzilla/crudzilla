package io.github.crudzilla.querybuilder;

import com.querydsl.core.types.Order;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public abstract class CRUDZillaFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRUDZillaFilter.class);

    @ApiParam(value = "Indica qual direção da ordenação do resultado", defaultValue = "ASC")
    private Order direcaoOrdenacao = Order.ASC;

    @ApiParam(value = "Indica qual coluna da ordenação do resultado. As colunas disponíveis são os campos do DTO de resultado da consulta.")
    private String colunaOrdenacao = "";

    @ApiParam(value = "Quantos resultados serão retornados. Limite máximo: 100.", defaultValue = "15")
    private int tamanhoPagina = 15;

    @ApiParam(value = "Quantos registros devem ser pulados.", defaultValue = "0")
    private int offset = 0;

    public Order getDirecaoOrdenacao() {
        return direcaoOrdenacao;
    }

    public void setDirecaoOrdenacao(Order direcaoOrdenacao) {
        this.direcaoOrdenacao = direcaoOrdenacao;
    }

    public String getColunaOrdenacao() {
        return colunaOrdenacao;
    }

    public void setColunaOrdenacao(String colunaOrdenacao) {
        this.colunaOrdenacao = colunaOrdenacao;
    }

    public int getTamanhoPagina() {
        return tamanhoPagina;
    }

    public void setTamanhoPagina(int tamanhoPagina) {
        if (tamanhoPagina <= 100) {
            this.tamanhoPagina = tamanhoPagina;
        } else {
            this.tamanhoPagina = 100;
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
                LOGGER.error(String.format("Erro ao gerar Hash do filtro %s no field %s", this.getClass().getName(), field.getName()), e);
                throw new RuntimeException(e);
            }
            if (value != null) {
                hash.append(Integer.toString(value.hashCode()));
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
