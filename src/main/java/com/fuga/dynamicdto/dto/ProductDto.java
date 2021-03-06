package com.fuga.dynamicdto.dto;

import io.beanmapper.annotations.BeanCollection;

import java.util.List;

public class ProductDto {

    public Long id;

    public String name;

    public String upc;

    @BeanCollection(elementType = AssetDto.class)
    public List<AssetDto> assets;

    @BeanCollection(elementType = ArtistDto.class)
    public List<ArtistDto> artists;

    public OrganizationDto organization;

}
