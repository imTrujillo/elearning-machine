package com.learning_engine.controller;

import com.learning_engine.dto.request.CourseRequest;
import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.PagedResponse;
import com.learning_engine.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Catálogo de cursos y sincronización")
public class CourseController {


    private final CourseService courseService;

    @Operation(summary = "Catálogo paginado de cursos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cursos obtenidos"),
            @ApiResponse(responseCode = "204", description = "Sin cursos")
    })
    @GetMapping
    public ResponseEntity<LearningApiResponse<PagedResponse<CourseResponse>>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<CourseResponse> courses = category != null
                ? courseService.findByCategory(category, pageable)
                : courseService.findAll(pageable);

        return ResponseEntity.ok(LearningApiResponse.success("Cursos obtenidos", courses));
    }

    @Operation(summary = "Detalle de un curso")
    @GetMapping("/{id}")
    public ResponseEntity<LearningApiResponse<CourseResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Curso encontrado", courseService.findById(id)));
    }

    @Operation(summary = "Sincronizar cursos desde WordPress y WooCommerce")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sync completado"),
            @ApiResponse(responseCode = "502", description = "WordPress no disponible")
    })
    @PostMapping("/sync")
    public ResponseEntity<LearningApiResponse<PagedResponse<CourseResponse>>> sync() {
        PagedResponse<CourseResponse> synced = courseService.syncFromWordpress();
        return ResponseEntity.ok(
                LearningApiResponse.success("Cursos sincronizados", synced));
    }

    @Operation(summary = "Crear un nuevo curso")
    @PostMapping
    public ResponseEntity<LearningApiResponse<CourseResponse>> createCourse(@RequestBody CourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(LearningApiResponse.success("Curso creado", courseService.createCourse(request)));
    }

    @Operation(summary = "Actualizar curso")
    @PatchMapping("/{id}")
    public ResponseEntity<LearningApiResponse<CourseResponse>> updateCourse(
            @PathVariable Long id, @RequestBody CourseRequest request) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Curso actualizado", courseService.updateCourse(id, request)));
    }

    @Operation(summary = "Asignar categoría a un curso")
    @PatchMapping("/{id}/category/{categorySlug}")
    public ResponseEntity<LearningApiResponse<CourseResponse>> assignCategory(
            @PathVariable Long id,
            @PathVariable String categorySlug) {

        return ResponseEntity.ok(
                LearningApiResponse.success(
                        "Categoría asignada",
                        courseService.assignCategory(id, categorySlug)
                )
        );
    }

    @Operation(summary = "Eliminar curso")
    @DeleteMapping("/{id}")
    public ResponseEntity<LearningApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(
                LearningApiResponse.success("Curso eliminado", null));
    }
}