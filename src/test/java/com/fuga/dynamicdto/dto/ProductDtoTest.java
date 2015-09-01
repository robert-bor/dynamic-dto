package com.fuga.dynamicdto.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fuga.dynamicdto.model.Artist;
import com.fuga.dynamicdto.model.Asset;
import com.fuga.dynamicdto.model.Organization;
import com.fuga.dynamicdto.model.Product;
import com.fuga.dynamicdto.util.DynBeanMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ProductDtoTest {

    @Test
    public void mapToDynamicProductDtoOrgOnlyIdName() throws Exception {
        Product product = createProduct(false);
        Object productDto = new DynBeanMapper().map(
                product,
                ProductDto.class,
                Arrays.asList( "id", "name", "organization.id", "organization.name" ));
        String json = new ObjectMapper().writeValueAsString(productDto);
        assertEquals("{\"id\":42,\"name\":\"Aller menscher\",\"organization\":{\"id\":1143,\"name\":\"My Org\"}}", json);
    }

    @Test
    public void mapToDynamicProductDtoWithLists() throws Exception {
        Product product = createProduct(true);
        Object productDto = new DynBeanMapper().map(
                product,
                ProductDto.class,
                Arrays.asList( "id", "name", "assets.id", "assets.name", "artists" ));
        String json = new ObjectMapper().writeValueAsString(productDto);
        assertEquals("{\"assets\":[],\"id\":42,\"name\":\"Aller menscher\"}", json);
    }

    private Product createProduct(boolean includeLists) {
        Product product = new Product();
        product.setId(42L);
        product.setName("Aller menscher");
        product.setUpc("12345678901");
        product.setInternalMemo("Secret message, not to be let out");

        if (includeLists) {
            List<Asset> assets = new ArrayList<>();
            assets.add(createAsset(1138L, "Track 1", "NL-123-ABCDEFGH"));
            assets.add(createAsset(1139L, "Track 2", "NL-123-ABCDEFGI"));
            assets.add(createAsset(1140L, "Track 3", "NL-123-ABCDEFGJ"));
            product.setAssets(assets);

            List<Artist> artists = new ArrayList<>();
            artists.add(createArtist(1141L, "Artist 1"));
            artists.add(createArtist(1142L, "Artist 2"));
            product.setArtists(artists);
        }

        Organization organization = new Organization();
        organization.setId(1143L);
        organization.setName("My Org");
        organization.setContact("Henk");
        product.setOrganization(organization);

        return product;
    }

    private Asset createAsset(Long id, String name, String isrc) {
        Asset asset = new Asset();
        asset.setId(id);
        asset.setName(name);
        asset.setIsrc(isrc);
        return asset;
    }

    private Artist createArtist(long id, String name) {
        Artist artist = new Artist();
        artist.setId(id);
        artist.setName(name);
        return artist;
    }

}
