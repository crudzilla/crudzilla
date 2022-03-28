package io.github.crudzilla.persistency.h2;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class CRUDZillaH2Dialect extends H2Dialect {

    public CRUDZillaH2Dialect() {
        super();
        registerFunction("always_null", new SQLFunctionTemplate(StandardBasicTypes.STRING, "cast(null as char)"));
        registerFunction("diffDataCorrenteDataInt", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "(CURRENT_DATE() - parsedatetime(?1, 'yyyyMMdd'))"));
    }
}
