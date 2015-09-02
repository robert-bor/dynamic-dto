package com.fuga.dynamicdto.dto;

import com.fuga.dynamicdto.util.DynBeanMapper;

public class AbstractDtoTest {

    protected static DynBeanMapper dynBeanMapper = new DynBeanMapper();

    static {
        dynBeanMapper.addPackagePrefix(ProductDto.class);
    }

}
