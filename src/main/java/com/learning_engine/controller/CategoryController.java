package com.learning_engine.controller;

import com.learning_engine.dto.request.CategoryRequest;
import com.learning_engine.dto.response.CategoryResponse;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías de cursos")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Listar todas las categorías")
    @GetMapping
    public ResponseEntity<LearningApiResponse<PagedResponse<CategoryResponse>>> findAll(@RequestParam(defaultValue = "0") int page,
                                                                                        @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                LearningApiResponse.success("Categorías obtenidas", categoryService.findAll(pageable)));
    }

    @Operation(summary = "Buscar categoría por slug")
    @GetMapping("/{slug}")
    public ResponseEntity<LearningApiResponse<CategoryResponse>> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Categoría encontrada", categoryService.findBySlug(slug)));
    }

    @Operation(summary = "Crear categoría")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoría creada"),
            @ApiResponse(responseCode = "400", description = "Slug duplicado"),
            @ApiResponse(responseCode = "422", description = "Validación fallida")
    })
    @PostMapping
    public ResponseEntity<LearningApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LearningApiResponse.success("Categoría creada", categoryService.create(request)));
    }

    @Operation(summary = "Actualizar categoría")
    @PutMapping("/{id}")
    public ResponseEntity<LearningApiResponse<CategoryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Categoría actualizada", categoryService.update(id, request)));
    }

    @Operation(summary = "Eliminar categoría")
    @DeleteMapping("/{id}")
    public ResponseEntity<LearningApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(LearningApiResponse.success("Categoría eliminada", null));
    }
}