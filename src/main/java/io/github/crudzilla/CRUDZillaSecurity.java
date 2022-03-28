package io.github.crudzilla;

import io.github.crudzilla.persistency.CRUDZillaEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;

import static org.apache.commons.lang3.ArrayUtils.isEmpty;

@Service(value = "crudZillaSecurity")
public class CRUDZillaSecurity {
    private final EntityReflections entityReflections;
    private final OperationReflections operationReflections;

    @Autowired
    public CRUDZillaSecurity(EntityReflections entityReflections, OperationReflections operationReflections) {
        this.entityReflections = entityReflections;
        this.operationReflections = operationReflections;
    }

    public boolean check(String key, CRUDZillaOperations operation) {
        Class<? extends CRUDZillaEntity<?>> entidadeClass = entityReflections.getEntityClass(key);
        return check(entidadeClass, operation);
    }

    public boolean check(Class<? extends CRUDZillaEntity> clazzName, CRUDZillaOperations operation) {
        if (clazzName.isEnum()){
            return true;
        }
        var annotation = clazzName.getAnnotation(CRUDZillaConfig.class);
        if (annotation == null) {
            throw new NotImplementedException("CRUDZilla não habilitado nessa entidade!");
        }

        var security = annotation.security();
        if (isEmpty(security)) {
            return true;
        }

        var securityOperation = Arrays.stream(security)
                .filter(x -> operation.equals(x.operation()))
                .findFirst();

        if (securityOperation.isEmpty()) {
            return true;
        }

        if (securityOperation.get().value().equals("deny")) {
            return false;
        }

        if(securityOperation.get().value().startsWith("@")) {
            return operationReflections.executeSpelOperation(securityOperation.get());
        }

        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
        if (CollectionUtils.isEmpty(authorities)) {
            return false;
        }

        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(x-> x.equals(securityOperation.get().value()));
    }
}
