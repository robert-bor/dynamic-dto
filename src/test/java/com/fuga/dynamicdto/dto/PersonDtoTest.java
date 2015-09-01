package com.fuga.dynamicdto.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuga.dynamicdto.model.Person;
import io.beanmapper.BeanMapper;
import javassist.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class PersonDtoTest {

    @Test
    public void mapToStatic() {
        Person person = createPerson();

        PersonDto personDto = new BeanMapper().map(person, PersonDto.class);

        assertEquals((Long)42L, personDto.id);
        assertEquals("Henk", personDto.name);
        assertEquals("Koraalrood", personDto.street);
        assertEquals("11f", personDto.houseNumber);
        assertEquals("Zoetermeer", personDto.city);
    }

    @Test
    public void mapToDynamic() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass dynClass = cp.get("com.fuga.dynamicdto.dto.PersonDto");
        dynClass.setName("com.fuga.dynamicdto.dto.DynPersonDto");

        includeOnlyFields(dynClass, Arrays.asList("id", "name"));

        Class dynPersonDtoClass = dynClass.toClass();
        Person person = createPerson();
        Object dynPersonDto = new BeanMapper().map(person, dynPersonDtoClass);
        String json = new ObjectMapper().writeValueAsString(dynPersonDto);
        assertEquals("{\"id\":42,\"name\":\"Henk\"}", json);
    }

    private void includeOnlyFields(CtClass dynClass, List<String> includeFields) throws NotFoundException {
        for (CtField field : dynClass.getDeclaredFields()) {
            if (!includeFields.contains(field.getName())) {
                dynClass.removeField(field);
            }
        }
    }

    private Person createPerson() {
        Person person = new Person();
        person.setId(42L);
        person.setName("Henk");
        person.setStreet("Koraalrood");
        person.setHouseNumber("11f");
        person.setCity("Zoetermeer");
        person.setBankAccount("NLABN123998877665544");
        return person;
    }

}
