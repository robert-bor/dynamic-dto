package com.fuga.dynamicdto.util;

import com.fuga.dynamicdto.dto.PersonDto;
import com.fuga.dynamicdto.model.Person;
import io.beanmapper.BeanMapper;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;

import java.util.*;

public class DynBeanMapper {

    private BeanMapper beanMapper;
    ClassPool classPool;
    private static Map<String, Map<String, GeneratedClass>> cachedGeneratedClasses = new TreeMap<>();
    private static Integer generatedClassSuffix = 0;
    private Set<String> packages = new TreeSet<>();

    public class GeneratedClass {
        public CtClass ctClass;
        public Class generatedClass;
        public GeneratedClass(CtClass ctClass) throws CannotCompileException {
            this.ctClass = ctClass;
            this.generatedClass = ctClass.toClass();
        }
    }

    public DynBeanMapper() {
        beanMapper = new BeanMapper();
        beanMapper.addPackagePrefix(Person.class);
        beanMapper.addPackagePrefix(PersonDto.class);
        classPool = ClassPool.getDefault();
    }

    public void addPackagePrefix(String packagePrefix) {
        packages.add(packagePrefix);
    }

    public void addPackagePrefix(Class packagePrefixClass) {
        addPackagePrefix(packagePrefixClass.getPackage().getName());
    }

    public Object map(Object object, Class clazz, List<String> includeFields) throws Exception {
        if (includeFields == null || includeFields.size() == 0) {
            return beanMapper.map(object, clazz);
        }

        Node displayFields = Node.createTree(includeFields);
        GeneratedClass dynPersonDtoClass = getOrCreateGeneratedClass(clazz.getName(), displayFields);

        return beanMapper.map(object, dynPersonDtoClass.generatedClass);
    }

    protected GeneratedClass getOrCreateGeneratedClass(String classInPackage, Node displayFields) throws Exception {
        Map<String, GeneratedClass> generatedClassesForClass = cachedGeneratedClasses.get(classInPackage);
        if (generatedClassesForClass == null) {
            generatedClassesForClass = new TreeMap<>();
            cachedGeneratedClasses.put(classInPackage, generatedClassesForClass);
        }
        GeneratedClass generatedClass = generatedClassesForClass.get(displayFields.getKey());
        if (generatedClass == null) {
            CtClass dynamicClass = classPool.get(classInPackage);
            dynamicClass.setName(classInPackage + "Dyn" + ++generatedClassSuffix);
            processClassTree(dynamicClass, displayFields);
            generatedClass = new GeneratedClass(dynamicClass);
            generatedClassesForClass.put(displayFields.getKey(), generatedClass);
        }
        return generatedClass;
    }

    private boolean isInPackage(CtClass clazz) {
        String currentPackageName = clazz.getPackageName();
        for (String allowedPackageName : packages) {
            if (currentPackageName.startsWith(allowedPackageName)) {
                return true;
            }
        }
        return false;
    }

    private void processClassTree(CtClass dynClass, Node node) throws Exception {

        for (CtField field : dynClass.getDeclaredFields()) {
            if (node.getFields().contains(field.getName())) {
                Node fieldNode = node.getNode(field.getName());
                // apply include filter, aka generate new dynamic class
                if (fieldNode.hasNodes() && isInPackage(field.getType())) {
                    GeneratedClass nestedClass = getOrCreateGeneratedClass(field.getType().getName(), fieldNode);
                    field.setType(nestedClass.ctClass);
                }
                else { // include all
                    // do nothing...
                }
            } else {
                dynClass.removeField(field);
            }
        }

    }

}
