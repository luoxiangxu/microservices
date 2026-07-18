package com.programmingtechie.product_service;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;  
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.programmingtechie.product_service.dto.ProductRequest;
import com.programmingtechie.product_service.repository.ProductRepository;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc  
class ProductTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;  

	@Autowired
	private ProductRepository productRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry) {
        dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

	@BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldCreateProduct() throws Exception {
        ProductRequest productRequest = getProductRequest();
        String productRequestString = objectMapper.writeValueAsString(productRequest);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/product")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productRequestString))
                .andExpect(status().isCreated());

		assertEquals(1, productRepository.findAll().size()) ;
    }

    private ProductRequest getProductRequest() {
        return ProductRequest.builder()
                .name("iphone14")
                .description("iphone")
                .price(BigDecimal.valueOf(600))
                .build();
    }

    @Test
    void shouldGetProduct() throws Exception{

        mockMvc.perform(MockMvcRequestBuilders.get("/api/product")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}