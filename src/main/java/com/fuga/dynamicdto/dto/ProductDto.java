package com.fuga.dynamicdto.dto;

import java.util.List;

public class ProductDto {

    public Long id;

    public String name;

    public String upc;

    public List<AssetDto> assets;

    public List<ArtistDto> artists;

    public OrganizationDto organization;

}
