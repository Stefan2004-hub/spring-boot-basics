package com.interview.demo.service;

import com.interview.demo.dto.ProductDTO;
import com.interview.demo.entity.Product;
import com.interview.demo.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Optional<Product> getProductById(Long id) {
    return productRepository.findById(id);
  }

  public Product saveProduct(ProductDTO product) {
    Product newProduct = new Product();
    newProduct.setName(product.name());
    newProduct.setDescription(product.description());
    newProduct.setPrice(product.price());
    return productRepository.save(newProduct);
  }

  public void deleteProduct(Long id) {
    productRepository.deleteById(id);
  }
}
