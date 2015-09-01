package com.fuga.dynamicdto.util;

import com.fuga.dynamicdto.dto.PersonDto;
import com.fuga.dynamicdto.model.Person;
import io.beanmapper.BeanMapper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.util.List;

public class DynBeanMapper {

    private BeanMapper beanMapper;
    ClassPool classPool;


    public DynBeanMapper() {
        beanMapper = new BeanMapper();
        beanMapper.addPackagePrefix(Person.class);
        beanMapper.addPackagePrefix(PersonDto.class);
        classPool = ClassPool.getDefault();
    }

    public Object map(Object object, Class clazz, List<String> includeFields) throws Exception {
        if (includeFields == null || includeFields.size() == 0) {
            return beanMapper.map(object, clazz);
        }

        Node node = Node.createTree(includeFields);

        CtClass dynClass = classPool.get(clazz.getName());
        dynClass.setName(clazz.getName() + "Dyn");

        processClassTree(node, dynClass);

        Class dynPersonDtoClass = dynClass.toClass();

        return beanMapper.map(object, dynPersonDtoClass);
    }

    private void processClassTree(Node node, CtClass dynClass) throws Exception {

        dynClass.defrost();
        for (CtField field : dynClass.getDeclaredFields()) {
            if (node.getKeys().contains(field.getName())) {
                Node fieldNode = node.getNode(field.getName());
                if (fieldNode.hasNodes()) { // apply include filter, aka generate new dynamic class
                    String nestedClassName = field.getType().toClass().getName();
                    System.out.println(nestedClassName);
                    CtClass nestedClass = classPool.get(nestedClassName );
                    nestedClass.defrost();
                    nestedClass.setName(nestedClassName + "Dyn");
                    nestedClass.freeze();

                    field.setType(nestedClass);
                    processClassTree(fieldNode, nestedClass);
                    nestedClass.toClass();
                }
                else { // include all
                    // do nothing...
                }
            } else {
                dynClass.removeField(field);
            }
        }
        dynClass.freeze();

    }

}
