package io.github.crudzilla;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

@Service
public class OperationReflections {
    private final ApplicationContext applicationContext;

    @Autowired
    public OperationReflections(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public boolean executeSpelOperation(CRUDZillaEntidadeSecurity securityOperation) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setBeanResolver(new BeanFactoryResolver(applicationContext));
            Expression expression = parser.parseExpression(securityOperation.value());

            return Boolean.TRUE.equals(expression.getValue(context, boolean.class));
        } catch (ParseException ex) {
            throw new CRUDZillaInvalidOperation();
        }
    }
}
