package com.interview.demo;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.interview.demo.entity.Category;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.CategoryRepository;
import com.interview.demo.repository.OrderRepository;
import com.interview.demo.repository.ProductRepository;
import com.interview.demo.support.PostgresContainerTestBase;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryApiIT extends PostgresContainerTestBase {
  @Autowired private MockMvc mockMvc;
  @Autowired private CategoryRepository categoryRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private OrderRepository orderRepository;

  @BeforeEach
  void clean() {
    orderRepository.deleteAll();
    productRepository.deleteAll();
    categoryRepository.deleteAll();
  }

  @Test
  void shouldCreateCategoryAndRejectDuplicateCategoryName() throws Exception {
    mockMvc
        .perform(post("/categories").contentType("application/json").content("{\"name\":\"Electronics\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.name").value("Electronics"))
        .andExpect(jsonPath("$.products").doesNotExist());

    mockMvc
        .perform(post("/categories").contentType("application/json").content("{\"name\":\"Electronics\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("Category already exists: Electronics"));
  }

  @Test
  void shouldGetAllCategories() throws Exception {
    categoryRepository.save(new Category(null, "Electronics"));
    categoryRepository.save(new Category(null, "Books"));

    mockMvc
        .perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].name").exists())
        .andExpect(jsonPath("$[0].products").doesNotExist());
  }

  @Test
  void shouldCreateProductAndFindByCategoryName() throws Exception {
    Category category = categoryRepository.save(new Category(null, "Electronics"));

    mockMvc
        .perform(
            post("/products")
                .contentType("application/json")
                .content(
                    """
                    {
                      "name": "Laptop Pro",
                      "description": "Powerful laptop",
                      "price": 1999.99,
                      "categoryId": %d
                    }
                    """
                        .formatted(category.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Laptop Pro"));

    mockMvc
        .perform(get("/products/by-category/Electronics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Laptop Pro"));
  }

  @Test
  void shouldFilterProductsByNameAndPriceRange() throws Exception {
    productRepository.save(product("Gaming Laptop", new BigDecimal("1500.00")));
    productRepository.save(product("Office Laptop", new BigDecimal("900.00")));
    productRepository.save(product("Gaming Mouse", new BigDecimal("100.00")));

    mockMvc
        .perform(get("/products/search").param("name", "laptop").param("minPrice", "1000").param("maxPrice", "1700"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].name").value("Gaming Laptop"));
  }

  @Test
  void shouldReturnProductSummariesProjection() throws Exception {
    productRepository.save(product("Monitor", new BigDecimal("299.99")));

    mockMvc
        .perform(get("/products/summaries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].name").value("Monitor"))
        .andExpect(jsonPath("$[0].price").value(299.99))
        .andExpect(jsonPath("$[0].description").doesNotExist());
  }

  @Test
  void shouldCreateOrderWithMultipleItems() throws Exception {
    Product keyboard = productRepository.save(product("Keyboard", new BigDecimal("100.00")));
    Product mouse = productRepository.save(product("Mouse", new BigDecimal("50.00")));

    mockMvc
        .perform(
            post("/orders")
                .contentType("application/json")
                .content(
                    """
                    {
                      "customerName": "Alice",
                      "items": [
                        {"productId": %d, "quantity": 2},
                        {"productId": %d, "quantity": 1}
                      ]
                    }
                    """
                        .formatted(keyboard.getId(), mouse.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.customerName").value("Alice"))
        .andExpect(jsonPath("$.status").value("CREATED"))
        .andExpect(jsonPath("$.totalAmount").value(250.00))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.createdAt").value(org.hamcrest.Matchers.matchesRegex("\\d{4}-\\d{2}-\\d{2}")))
        .andExpect(jsonPath("$.items.length()").value(2))
        .andExpect(jsonPath("$.items[0].order").doesNotExist());
  }

  @Test
  void shouldGetOrderItemsByOrderId() throws Exception {
    Product keyboard = productRepository.save(product("Keyboard", new BigDecimal("100.00")));
    Product mouse = productRepository.save(product("Mouse", new BigDecimal("50.00")));

    String createResponse =
        mockMvc
            .perform(
                post("/orders")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "customerName": "Alice",
                          "items": [
                            {"productId": %d, "quantity": 2},
                            {"productId": %d, "quantity": 1}
                          ]
                        }
                        """
                            .formatted(keyboard.getId(), mouse.getId())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long orderId = Long.parseLong(JsonPath.read(createResponse, "$.id").toString());

    mockMvc
        .perform(get("/orders/{id}/items", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].id").exists())
        .andExpect(jsonPath("$[0].productId").exists())
        .andExpect(jsonPath("$[0].productName").exists())
        .andExpect(jsonPath("$[0].quantity").exists())
        .andExpect(jsonPath("$[0].unitPrice").exists())
        .andExpect(jsonPath("$[0].lineTotal").exists());
  }

  @Test
  void shouldReturnNotFoundWhenGettingOrderItemsForMissingOrder() throws Exception {
    mockMvc
        .perform(get("/orders/{id}/items", 999999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order not found: 999999"));
  }

  @Test
  void shouldReturnNotFoundForMissingCategoryWhenCreatingProduct() throws Exception {
    mockMvc
        .perform(
            post("/products")
                .contentType("application/json")
                .content(
                    """
                    {
                      "name": "Laptop Pro",
                      "description": "Powerful laptop",
                      "price": 1999.99,
                      "categoryId": 999999
                    }
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Category not found: 999999"));
  }

  @Test
  void shouldReturnNotFoundForMissingProductWhenCreatingOrder() throws Exception {
    mockMvc
        .perform(
            post("/orders")
                .contentType("application/json")
                .content(
                    """
                    {
                      "customerName": "Alice",
                      "items": [
                        {"productId": 999999, "quantity": 1}
                      ]
                    }
                    """))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Product not found: 999999"));
  }

  @Test
  void shouldReturnBadRequestForInvalidCategoryPayload() throws Exception {
    mockMvc
        .perform(post("/categories").contentType("application/json").content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Category name is required"));
  }

  @Test
  void shouldReturnBadRequestForInvalidProductPayload() throws Exception {
    mockMvc
        .perform(
            post("/products")
                .contentType("application/json")
                .content(
                    """
                    {
                      "name": "Laptop Pro",
                      "description": "Powerful laptop",
                      "categoryId": 1
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Product price is required"));
  }

  @Test
  void shouldReturnBadRequestForInvalidOrderPayload() throws Exception {
    mockMvc
        .perform(
            post("/orders")
                .contentType("application/json")
                .content(
                    """
                    {
                      "customerName": "Alice",
                      "items": []
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Order must contain at least one item"));
  }

  @Test
  void shouldUpdateCategory() throws Exception {
    Category category = categoryRepository.save(new Category(null, "Electronics"));

    mockMvc
        .perform(
            put("/categories/{id}", category.getId())
                .contentType("application/json")
                .content("{\"name\":\"Audio\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(category.getId()))
        .andExpect(jsonPath("$.name").value("Audio"));
  }

  @Test
  void shouldDeleteCategory() throws Exception {
    Category category = categoryRepository.save(new Category(null, "Electronics"));

    mockMvc
        .perform(delete("/categories/{id}", category.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Category deleted successfully"));

    mockMvc
        .perform(get("/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(0));
  }

  @Test
  void shouldReturnConflictWhenDeletingCategoryInUse() throws Exception {
    Category category = categoryRepository.save(new Category(null, "Electronics"));
    Product product = product("Laptop", new BigDecimal("1000.00"));
    product.setCategory(category);
    productRepository.save(product);

    mockMvc
        .perform(delete("/categories/{id}", category.getId()))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.error")
                .value("Category is in use and cannot be deleted: " + category.getId()));
  }

  @Test
  void shouldUpdateProduct() throws Exception {
    Category oldCategory = categoryRepository.save(new Category(null, "Electronics"));
    Category newCategory = categoryRepository.save(new Category(null, "Accessories"));

    Product product = product("Mouse", new BigDecimal("20.00"));
    product.setCategory(oldCategory);
    product = productRepository.save(product);

    mockMvc
        .perform(
            put("/products/{id}", product.getId())
                .contentType("application/json")
                .content(
                    """
                    {
                      "name": "Gaming Mouse",
                      "description": "High DPI gaming mouse",
                      "price": 49.99,
                      "categoryId": %d
                    }
                    """
                        .formatted(newCategory.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(product.getId()))
        .andExpect(jsonPath("$.name").value("Gaming Mouse"))
        .andExpect(jsonPath("$.price").value(49.99))
        .andExpect(jsonPath("$.categoryId").value(newCategory.getId()))
        .andExpect(jsonPath("$.categoryName").value("Accessories"));
  }

  @Test
  void shouldDeleteProduct() throws Exception {
    Product product = productRepository.save(product("Mouse", new BigDecimal("20.00")));

    mockMvc
        .perform(delete("/products/{id}", product.getId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Product deleted successfully"));
  }

  @Test
  void shouldReturnConflictWhenDeletingProductInUseByOrder() throws Exception {
    Product product = productRepository.save(product("Keyboard", new BigDecimal("100.00")));

    mockMvc
        .perform(
            post("/orders")
                .contentType("application/json")
                .content(
                    """
                    {
                      "customerName": "Alice",
                      "items": [
                        {"productId": %d, "quantity": 1}
                      ]
                    }
                    """
                        .formatted(product.getId())))
        .andExpect(status().isOk());

    mockMvc
        .perform(delete("/products/{id}", product.getId()))
        .andExpect(status().isConflict())
        .andExpect(
            jsonPath("$.error").value("Product is in use and cannot be deleted: " + product.getId()));
  }

  @Test
  void shouldUpdateOrderStatus() throws Exception {
    Product product = productRepository.save(product("Keyboard", new BigDecimal("100.00")));

    String createResponse =
        mockMvc
            .perform(
                post("/orders")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "customerName": "Alice",
                          "items": [
                            {"productId": %d, "quantity": 2}
                          ]
                        }
                        """
                            .formatted(product.getId())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long orderId = Long.parseLong(JsonPath.read(createResponse, "$.id").toString());

    mockMvc
        .perform(
            put("/orders/{id}", orderId)
                .contentType("application/json")
                .content("{\"status\":\"CONFIRMED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId))
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.totalAmount").value(200.00));
  }

  @Test
  void shouldDeleteOrder() throws Exception {
    Product product = productRepository.save(product("Keyboard", new BigDecimal("100.00")));

    String createResponse =
        mockMvc
            .perform(
                post("/orders")
                    .contentType("application/json")
                    .content(
                        """
                        {
                          "customerName": "Alice",
                          "items": [
                            {"productId": %d, "quantity": 1}
                          ]
                        }
                        """
                            .formatted(product.getId())))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

    long orderId = Long.parseLong(JsonPath.read(createResponse, "$.id").toString());

    mockMvc
        .perform(delete("/orders/{id}", orderId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Order deleted successfully"));

    mockMvc
        .perform(get("/orders/{id}/items", orderId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Order not found: " + orderId));
  }

  @Test
  void shouldReturnNotFoundWhenDeletingMissingProduct() throws Exception {
    mockMvc
        .perform(delete("/products/{id}", 999999L))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.error").value("Product not found: 999999"));
  }

  private Product product(String name, BigDecimal price) {
    Product product = new Product();
    product.setName(name);
    product.setDescription(name + " description");
    product.setPrice(price);
    return product;
  }
}
