package io.github.crudzilla.persistency;

import com.querydsl.sql.*;

public final class SQLTemplatesFactory {

    public enum Banco {
        TERADATA,
        SQLSERVER,
        SQLSERVER_2005,
        SQLSERVER_2008

    }

    private SQLTemplatesFactory() {/* Hide do construtor de casse utilitária */ }

    public static SQLTemplates build(Banco banco) {
        String springProfile = System.getProperty("spring.profiles.active");
        if ("teste".equals(springProfile)) {
            return H2Templates.builder().printSchema().build();
        } else {
            switch (banco) {
                case TERADATA:
                    return TeradataTemplates.builder().printSchema().build();
                case SQLSERVER_2005:
                    return SQLServer2005Templates.builder().printSchema().build();
                case SQLSERVER_2008:
                    return SQLServer2008Templates.builder().printSchema().build();
                default:
                    return SQLServer2012Templates.builder().printSchema().build();
            }
        }
    }
}
