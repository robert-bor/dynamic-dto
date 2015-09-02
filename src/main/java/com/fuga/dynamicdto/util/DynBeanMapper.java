package com.fuga.dynamicdto.util;

import com.fuga.dynamicdto.dto.PersonDto;
import com.fuga.dynamicdto.model.Person;
import io.beanmapper.BeanMapper;
import io.beanmapper.annotations.BeanCollection;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;

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

    public <S> Object map(S source, Class targetClass, List<String> includeFields) throws Exception {
        if (includeFields == null || includeFields.size() == 0) {
            return beanMapper.map(source, targetClass);
        }
        return beanMapper.map(source, getOrCreateGeneratedClass(targetClass, includeFields).generatedClass);
    }

    public <S> Collection map(Collection<S> sourceItems, Class targetClass, List<String> includeFields) throws Exception {
        if (includeFields == null || includeFields.size() == 0) {
            return beanMapper.map(sourceItems, targetClass);
        }
        return beanMapper.map(sourceItems, getOrCreateGeneratedClass(targetClass, includeFields).generatedClass);
    }

    private GeneratedClass getOrCreateGeneratedClass(Class targetClass, List<String> includeFields) throws Exception {
        Node displayFields = Node.createTree(includeFields);
        return getOrCreateGeneratedClass(targetClass.getName(), displayFields);
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
                } else if (field.hasAnnotation(BeanCollection.class)){
                    BeanCollection beanCollection = (BeanCollection)field.getAnnotation(BeanCollection.class);
                    Class elementType = beanCollection.elementType();
                    GeneratedClass elementClass = getOrCreateGeneratedClass(elementType.getName(), fieldNode);

                    elementClass.ctClass.defrost();
                    ConstPool constPool = elementClass.ctClass.getClassFile().getConstPool();
                    AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    Annotation annot = new Annotation("io.beanmapper.annotations.BeanCollection", constPool);
                    annot.addMemberValue("elementType", new ClassMemberValue(elementClass.generatedClass.getName(), constPool));
                    attr.addAnnotation(annot);
                    field.getFieldInfo().addAttribute(attr);
                    elementClass.ctClass.freeze();
                }
                else { // include all
                    // do nothing...
                }
            } else {
                if (node.hasNodes()) { // Only remove fields if there are any fields at all to remove, else assume full showing
                    dynClass.removeField(field);
                }
            }
        }

    }

}
