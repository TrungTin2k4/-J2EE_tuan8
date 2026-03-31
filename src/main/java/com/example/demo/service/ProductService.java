package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getProducts(String keyword, Integer categoryId, String sortOption, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), size, buildSort(sortOption));
        Specification<Product> specification = null;

        if (StringUtils.hasText(keyword)) {
            String normalizedKeyword = keyword.trim().toLowerCase();
            specification = (root, query, criteriaBuilder) -> criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), "%" + normalizedKeyword + "%");
        }

        if (categoryId != null) {
            Specification<Product> categorySpecification = (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                    root.get("category").get("id"), categoryId);
            specification = specification == null ? categorySpecification : specification.and(categorySpecification);
        }

        if (specification == null) {
            return productRepository.findAll(pageable);
        }

        return productRepository.findAll(specification, pageable);
    }

    public void saveProduct(Product product) {
        if (product.getCategory() == null || product.getCategory().getId() <= 0) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }

        product.setCategory(categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại")));
        productRepository.save(product);
    }

    public Product getProductById(long id) {
        return productRepository.findById(id).orElse(null);
    }

    public void deleteProduct(long id) {
        productRepository.deleteById(id);
    }

    private Sort buildSort(String sortOption) {
        if ("price_asc".equalsIgnoreCase(sortOption)) {
            return Sort.by(Sort.Direction.ASC, "price", "id");
        }
        if ("price_desc".equalsIgnoreCase(sortOption)) {
            return Sort.by(Sort.Direction.DESC, "price", "id");
        }
        return Sort.by(Sort.Direction.ASC, "id");
    }
}
