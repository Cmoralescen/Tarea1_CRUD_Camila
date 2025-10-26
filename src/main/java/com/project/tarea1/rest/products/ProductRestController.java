package com.project.tarea1.rest.products;

import com.project.tarea1.logic.entity.products.Products;
import com.project.tarea1.logic.entity.products.ProductsRepository;
import com.project.tarea1.logic.entity.categories.CategoriesRepository;
import com.project.tarea1.logic.entity.categories.Categories;
import com.project.tarea1.logic.entity.http.GlobalResponseHandler;
import com.project.tarea1.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

import java.util.Optional;

@RestController
@RequestMapping("/products")
public class ProductRestController {
    @Autowired
    private ProductsRepository productRepository;
    @Autowired
    private CategoriesRepository CategoriesRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Products> productsPage = productRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(productsPage.getTotalPages());
        meta.setTotalElements(productsPage.getTotalElements());
        meta.setPageNumber(productsPage.getNumber() + 1);
        meta.setPageSize(productsPage.getSize());

        return new GlobalResponseHandler().handleResponse("Products retrieved successfully",
                productsPage.getContent(), HttpStatus.OK, meta);
    }
    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getProductById(@PathVariable Integer productId, HttpServletRequest request) {
        Optional<Products> foundProduct = productRepository.findById(Long.valueOf(productId));

        if (foundProduct.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Products product = foundProduct.get();
        Map<String, Object> productData = new HashMap<>();
        productData.put("id", product.getId());
        productData.put("name", product.getName());
        productData.put("description", product.getDescription());
        productData.put("price", product.getPrice());
        productData.put("stock", product.getStock());
        productData.put("categoryId", product.getCategory() != null ? product.getCategory().getId() : null);
        productData.put("categoryName", product.getCategory() != null ? product.getCategory().getName() : null);

        return new GlobalResponseHandler().handleResponse(
                "Product retrieved successfully",
                productData,
                HttpStatus.OK,
                request
        );
    }
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId, HttpServletRequest request) {
        Optional<Products> foundProduct = productRepository.findById(Long.valueOf(productId));
        if (foundProduct.isPresent()) {
            productRepository.deleteById(Long.valueOf(productId));
            return new GlobalResponseHandler().handleResponse("Product deleted successfully",
                    foundProduct.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addProduct(@RequestBody Products product,
                                        @RequestParam Integer categoryId,
                                        HttpServletRequest request) {


        Optional<Categories> optionalCategory = CategoriesRepository.findById(Long.valueOf(categoryId));
        if (optionalCategory.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoryId + " not found",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        product.setCategory(optionalCategory.get());
        productRepository.save(product);

        return new GlobalResponseHandler().handleResponse(
                "Product created successfully",
                product,
                HttpStatus.CREATED,
                request
        );
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable Integer productId,
                                           @RequestBody Products product,
                                           @RequestParam Integer categoryId,
                                           HttpServletRequest request) {

        Optional<Products> foundProduct = productRepository.findById(Long.valueOf(productId));
        if (foundProduct.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Product id " + productId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }

        Optional<Categories> optionalCategory = CategoriesRepository.findById(Long.valueOf(categoryId));
        if (optionalCategory.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoryId + " not found",
                    HttpStatus.BAD_REQUEST,
                    request
            );
        }

        product.setId(productId);
        product.setCategory(optionalCategory.get());
        productRepository.save(product);

        return new GlobalResponseHandler().handleResponse(
                "Product updated successfully",
                product,
                HttpStatus.OK,
                request
        );
    }

}
