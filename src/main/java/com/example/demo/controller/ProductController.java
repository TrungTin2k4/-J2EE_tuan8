package com.example.demo.controller;

import com.example.demo.model.Category;
import com.example.demo.model.Product;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final int PAGE_SIZE = 5;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String listProducts(@RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        Page<Product> productPage = productService.getProducts(keyword, categoryId, sort, page, PAGE_SIZE);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("productPage", productPage);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sort == null ? "" : sort);
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("displayTotalPages", Math.max(productPage.getTotalPages(), 1));
        return "product/list";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        prepareProductForm(model, new Product());
        return "product/add";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product, BindingResult result,
            @RequestParam("imageProduct") MultipartFile imageProduct,
            Model model) {
        validateCategory(product, result);
        if (result.hasErrors()) {
            prepareProductForm(model, product);
            return "product/add";
        }

        if (imageProduct != null && !imageProduct.isEmpty()) {
            try {
                product.setImage(storeImage(imageProduct));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            prepareProductForm(model, product);
            return "product/edit";
        }
        return "redirect:/products";
    }

    @PostMapping("/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam(value = "imageProduct", required = false) MultipartFile imageProduct,
            Model model) {
        validateCategory(product, result);
        if (result.hasErrors()) {
            prepareProductForm(model, product);
            return "product/edit";
        }

        if (imageProduct != null && !imageProduct.isEmpty()) {
            try {
                product.setImage(storeImage(imageProduct));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Keep the old image if not updated
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                product.setImage(existingProduct.getImage());
            }
        }

        product.setId(id);
        productService.saveProduct(product);
        return "redirect:/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProductGet(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String deleteProductPost(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/products";
    }

    private void prepareProductForm(Model model, Product product) {
        if (product.getCategory() == null) {
            product.setCategory(new Category());
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
    }

    private void validateCategory(Product product, BindingResult result) {
        if (product.getCategory() == null || product.getCategory().getId() <= 0) {
            result.rejectValue("category.id", "category.invalid", "Danh mục không được để trống");
        }
    }

    private String storeImage(MultipartFile imageProduct) throws Exception {
        Path uploadDir = Paths.get("src/main/resources/static/images");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFileName = StringUtils.cleanPath(imageProduct.getOriginalFilename());
        String fileExtension = "";
        int extensionIndex = originalFileName.lastIndexOf('.');
        if (extensionIndex >= 0) {
            fileExtension = originalFileName.substring(extensionIndex);
        }

        String storedFileName = UUID.randomUUID() + fileExtension;
        Path path = uploadDir.resolve(storedFileName);
        Files.copy(imageProduct.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        return "/images/" + storedFileName;
    }
}
