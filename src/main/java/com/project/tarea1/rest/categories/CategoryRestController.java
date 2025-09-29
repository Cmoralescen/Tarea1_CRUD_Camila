package com.project.tarea1.rest.categories;

import com.project.tarea1.logic.entity.categories.Categories;
import com.project.tarea1.logic.entity.categories.CategoriesRepository;
import com.project.tarea1.logic.entity.http.GlobalResponseHandler;
import com.project.tarea1.logic.entity.http.Meta;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {
    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Categories> categoriesPage = categoriesRepository.findAll(pageable);

        Meta meta = new Meta(request.getMethod(), request.getRequestURL().toString());
        meta.setTotalPages(categoriesPage.getTotalPages());
        meta.setTotalElements(categoriesPage.getTotalElements());
        meta.setPageNumber(categoriesPage.getNumber() + 1);
        meta.setPageSize(categoriesPage.getSize());

        return new GlobalResponseHandler().handleResponse("Categories retrieved successfully",
                categoriesPage.getContent(), HttpStatus.OK, meta);
    }

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> addCategory(@RequestBody Categories category, HttpServletRequest request) {
        categoriesRepository.save(category);
        return new GlobalResponseHandler().handleResponse("Category created successfully",
                category, HttpStatus.CREATED, request);
    }

    @PutMapping("/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> updateCategory(@PathVariable Integer categoryId,
                                            @RequestBody Categories category,
                                            HttpServletRequest request) {
        Optional<Categories> foundCategory = categoriesRepository.findById(Long.valueOf(categoryId));
        if (foundCategory.isEmpty()) {
            return new GlobalResponseHandler().handleResponse(
                    "Category id " + categoryId + " not found",
                    HttpStatus.NOT_FOUND,
                    request
            );
        }
        Categories existingCategory = foundCategory.get();
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());

        categoriesRepository.save(existingCategory);

        return new GlobalResponseHandler().handleResponse(
                "Category updated successfully",
                existingCategory,
                HttpStatus.OK,
                request
        );
    }


    @DeleteMapping("/{categoryId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Integer categoryId, HttpServletRequest request) {
        Optional<Categories> foundCategory = categoriesRepository.findById(Long.valueOf(categoryId));
        if (foundCategory.isPresent()) {
            categoriesRepository.deleteById(Long.valueOf(categoryId));
            return new GlobalResponseHandler().handleResponse("Category deleted successfully",
                    foundCategory.get(), HttpStatus.OK, request);
        } else {
            return new GlobalResponseHandler().handleResponse("Category id " + categoryId + " not found",
                    HttpStatus.NOT_FOUND, request);
        }
    }
}
