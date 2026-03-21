package com.interview.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.interview.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ProductRepository productRepository;

  @BeforeEach
  void setup() {
    productRepository.deleteAll();
  }

  @Test
  void shouldCreateProduct() throws Exception {
    String productJson =
        """
            {
                "name": "Test Product",
                "description": "This is a test product",
                "price": 9.99
            }
        """;

    mockMvc
        .perform(post("/products").contentType("application/json").content(productJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Test Product"))
        .andExpect(jsonPath("$.description").value("This is a test product"))
        .andExpect(jsonPath("$.price").value(9.99));
  }

  @Test
  void shouldGetAllProducts() throws Exception {
    mockMvc.perform(get("/products")).andExpect(status().isOk());
  }
}
