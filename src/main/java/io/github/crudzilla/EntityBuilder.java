package io.github.crudzilla;

import io.github.crudzilla.persistency.CRUDZillaEntity;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.ElementCollection;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.List;

@Service(value = "CRUDZillaDefaultEntityBuilder")
public class EntityBuilder<F extends CRUDZillaForm<E,K>, E extends CRUDZillaEntity<K>, K> {

    private final Logger logger = LoggerFactory.getLogger(EntityBuilder.class);

    private final ModelMapper modelMapper;
    private final EntityReflections entityReflections;
    private final CRUDZilla crudZilla;

    @Autowired
    public EntityBuilder(ModelMapper modelMapper, EntityReflections entityReflections, CRUDZilla crudZilla) {
        this.modelMapper = modelMapper;
        this.entityReflections = entityReflections;
        this.crudZilla = crudZilla;
    }

    public E buildNew(F form, E entidade) {
        //preenche props simples
        modelMapper.map(form, entidade);

        for (Field entidadeField : entidade.getClass().getDeclaredFields()) {
            try {
                if (entidadeField.isAnnotationPresent(ManyToMany.class)) {
                    processFieldManyToMany(form, entidade, entidadeField);
                } else if (entidadeField.isAnnotationPresent(OneToMany.class)) {
                    processFieldOneToMany(form, entidade, entidadeField);
                } else if (entidadeField.isAnnotationPresent(ManyToOne.class)) {
                    processFieldManyToOne(form, entidade, entidadeField);
                } else if ( entidadeField.isAnnotationPresent(ElementCollection.class)) {
                    processFieldElementCollection(form, entidade, entidadeField);
                }
            } catch (NoSuchFieldException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
                logger.info("Ignorando field " + entidadeField.getName() + " por causa de exception: " + e.getMessage());
            }
        }

        return entidade;
    }

    @SuppressWarnings("unchecked")
    private void processFieldElementCollection(F form, E entidade, Field entidadeField) throws InvocationTargetException, IllegalAccessException {
        //For some reason, Object Mapper doesn't clear the values of List<Enum>, so we have to do it manually
        var list = (Collection) invokeGetter(entidade, entidadeField.getName());
        list.clear();
        list.addAll((Collection) invokeGetter(form, entidadeField.getName()));
    }

    public E buildExisting(F form, E entidade) {
        return buildNew(form, entidade);
    }

    protected void processFieldManyToOne(F form, E entidade, Field entidadeField) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var formField = form.getClass().getDeclaredField(entidadeField.getName());
        var manyClass = entidadeField.getType();
        var idClass = manyClass.getDeclaredMethod("getId").getReturnType();

        if (!idClass.equals(formField.getType())) {
            return;
        }

        var idNoForm = invokeGetter(form, formField.getName());

        if (idNoForm == null) {
            invokeSetter(entidade, entidadeField.getName(), null);
            return;
        }

        var repository = entityReflections.getRepository(manyClass);
        invokeSetter(entidade, entidadeField.getName(), repository.get(idNoForm));

    }

    private void processFieldOneToMany(F form, E entidade, Field entidadeField) throws NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        var formField = form.getClass().getDeclaredField(entidadeField.getName() + "Forms");
        var manyClassName = ((ParameterizedType) entidadeField.getGenericType()).getActualTypeArguments()[0].getTypeName();
        var manyClass = (Class<? extends CRUDZillaEntity<?>>) Class.forName(manyClassName);
        var listaSubForm = (Collection<CRUDZillaForm>) invokeGetter(form, formField.getName());
        var listaNaEntidade = (Collection) invokeGetter(entidade, entidadeField.getName());
        listaNaEntidade.clear();
        var mappedByFieldName = entidadeField.getAnnotation(OneToMany.class).mappedBy();

        for (CRUDZillaForm subForm : listaSubForm) {
            var obj = crudZilla.save(manyClass, subForm, (e)->{
                try {
                    invokeSetter(e, mappedByFieldName, entidade);
                } catch (IllegalAccessException|InvocationTargetException exception) {
                    logger.error("Erro ao setar field mappedBy do outro lado do relacionamento OneToMany no field " + entidadeField.getName(), exception);
                }
            }, true);

            listaNaEntidade.add(obj);
        }
    }

    private void processFieldManyToMany(F form, E entidade, Field entidadeField) throws NoSuchFieldException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        var formField = form.getClass().getDeclaredField("ids" + StringUtils.capitalize(entidadeField.getName()));
        var manyClassName = ((ParameterizedType) entidadeField.getGenericType()).getActualTypeArguments()[0].getTypeName();
        var manyClass = Class.forName(manyClassName);
        var idClass = manyClass.getDeclaredMethod("getId").getReturnType();
        Class<?> tipoDaListaNoForm = Class.forName(((ParameterizedType) formField.getGenericType()).getActualTypeArguments()[0].getTypeName());

        if (!idClass.equals(tipoDaListaNoForm)) {
            return;
        }

        var listaNaEntidade = (Collection) invokeGetter(entidade, entidadeField.getName());
        listaNaEntidade.clear();
        var repository = entityReflections.getRepository(manyClass);
        listaNaEntidade.addAll(
                repository.getByIds(
                        (List) invokeGetter(form, formField.getName())
                )
        );
    }

    private Object invokeGetter(Object object, String fieldName) throws IllegalAccessException, InvocationTargetException {
        return BeanUtils
                .getPropertyDescriptor(object.getClass(), fieldName)
                .getReadMethod()
                .invoke(object);
    }

    private Object invokeSetter(Object object, String fieldName, Object value) throws IllegalAccessException, InvocationTargetException {
        return BeanUtils
                .getPropertyDescriptor(object.getClass(), fieldName)
                .getWriteMethod()
                .invoke(object, value);
    }

}
