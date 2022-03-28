package io.github.crudzilla;

import io.github.crudzilla.persistency.CRUDZillaEntity;
import io.github.crudzilla.persistency.RepositoryJpa;
import io.github.crudzilla.querybuilder.CRUDZillaFilter;
import io.github.crudzilla.querybuilder.QueryBuilderJPASQL;
import org.apache.commons.lang3.NotImplementedException;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
@Service
public class EntityReflections {

    private final ApplicationContext applicationContext;
    private final Map<String, Class<? extends CRUDZillaEntity<?>>> entidadesMap;
    private final String domainPackageName;
    private final String appPackageName;

    @Autowired
    @SuppressWarnings({"SuspiciousMethodCalls"})
    public EntityReflections(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        var beansWithAnnotation = applicationContext.getBeansWithAnnotation(CRUDZillaGlobalConfig.class);
        var firstBeanWithAnnotationFound = beansWithAnnotation.get(beansWithAnnotation.keySet().toArray()[0]);
        var globalConfig = firstBeanWithAnnotationFound.getClass().getAnnotation(CRUDZillaGlobalConfig.class);
        var reflections = new Reflections(globalConfig.scanPackage());
        this.domainPackageName = globalConfig.domainPackagePrefix();
        this.appPackageName = globalConfig.appPackagePrefix();
        entidadesMap = createEntitiesMap(reflections);
    }

    private Map<String, Class<? extends CRUDZillaEntity<?>>> createEntitiesMap(Reflections reflections) {
        var map = new HashMap<String, Class<? extends CRUDZillaEntity<?>>>();
        var anotados = reflections.getTypesAnnotatedWith(CRUDZillaConfig.class);
        for (var anotado : anotados) {
            map.put(anotado.getAnnotation(CRUDZillaConfig.class).key(), (Class<? extends CRUDZillaEntity<?>>) anotado);
        }
        return map;
    }

    public CRUDZillaEntity getNewEntity(Class<? extends CRUDZillaEntity> entidadeClass) {
        try {
            var constructor = entidadeClass.getConstructor();
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Error creating new entity " + entidadeClass.getName(), e);
        }
    }

    RepositoryJpa getRepository(Class<?> entidadeClass) {
        return (RepositoryJpa) getBean(entidadeClass.getName().replace(appPackageName + ".",domainPackageName + "."), "Repository", "GenericRepository");
    }

    RepositoryJpa getRepository(String key) {
        return getRepository(getEntityClass(key));
    }

    public EntityBuilder getEntityBuilder(Class<? extends CRUDZillaEntity> entidadeClass) {
        return (EntityBuilder) getBean(entidadeClass.getName().replace(domainPackageName, appPackageName), "EntityBuilder", "CRUDZillaDefaultEntityBuilder");
    }

    private Object getBean(String className, String suffix, String orDefault) {
        try {
            Class<?> beanClass = Class.forName(className + suffix);
            return applicationContext.getBean(beanClass);
        } catch (ClassNotFoundException e) {
            return applicationContext.getBean(orDefault);
        }
    }

    public boolean isGetAllDisabled(Class<? extends CRUDZillaEntity> clazzName) {
        if (clazzName.isEnum()) {
            return false;
        }
        var annotation = clazzName.getAnnotation(CRUDZillaConfig.class);
        if (annotation == null) {
            throw new NotImplementedException("CRUDZilla não habilitado para essa entidade!");
        }
        return annotation.disableGetAll();
    }

    @SuppressWarnings("unchecked")
    public <F extends CRUDZillaFilter, D> QueryBuilderJPASQL<F, D> getQueryBuilder(Class<F> filtroClass) {
        return (QueryBuilderJPASQL<F,D>) getBean(filtroClass.getName().replace("Filter", ""), "QueryBuilder", "GenericQueryBuilder");
    }

    @SuppressWarnings("unchecked")
    public Class<? extends CRUDZillaFilter> getFilterClassFromKey(String key) {
        try {
            return (Class<? extends CRUDZillaFilter>) Class.forName(getEntityClass(key).getName()
                    .replace(domainPackageName, appPackageName)
                    .concat("Filtro"));
        } catch (ClassNotFoundException e) {
            throw new CRUDZillaFilterNotFound();
        }
    }

    Class<? extends CRUDZillaEntity<?>> getEntityClass(String key) {
        Class<? extends CRUDZillaEntity<?>> entidadeClass = entidadesMap.get(key);
        if (entidadeClass == null) {
            throw new CRUDZillaKeyNotFound();
        }
        return entidadeClass;
    }

    public Class<? extends CRUDZillaForm> getFormClass(Class<? extends CRUDZillaEntity<?>> entidadeClass) {
        try {
            return (Class<? extends CRUDZillaForm>) Class.forName(entidadeClass.getName()
                    .replace(domainPackageName, appPackageName)
                    .concat("Form"));
        } catch (ClassNotFoundException e) {
            throw new CRUDZillaFormNotFound();
        }
    }
}
