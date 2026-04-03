package com.learning_engine.controller;

import com.learning_engine.dto.response.CourseResponse;
import com.learning_engine.dto.response.LearningApiResponse;
import com.learning_engine.dto.response.ModuleResponse;
import com.learning_engine.service.CourseService;
import com.learning_engine.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Cursos", description = "Catálogo de cursos y sincronización")
public class CourseController {

    private final CourseService courseService;
    private final ModuleService moduleService;

    @Operation(summary = "Catálogo paginado de cursos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cursos obtenidos"),
            @ApiResponse(responseCode = "204", description = "Sin cursos")
    })
    @GetMapping
    public ResponseEntity<LearningApiResponse<Page<CourseResponse>>> findAll(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CourseResponse> courses = category != null
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
    public ResponseEntity<LearningApiResponse<List<CourseResponse>>> sync() {
        List<CourseResponse> synced = courseService.syncFromWordpress();
        return ResponseEntity.ok(
                LearningApiResponse.success("Sincronizados " + synced.size() + " cursos", synced));
    }

    @Operation(summary = "Módulos de un curso con control de acceso")
    @GetMapping("/{id}/modules")
    public ResponseEntity<LearningApiResponse<List<ModuleResponse>>> getModules(
            @PathVariable Long id,
            @RequestParam Long studentId) {   // en prod vendría del JWT
        return ResponseEntity.ok(
                LearningApiResponse.success("Módulos obtenidos",
                        moduleService.findByCourse(id, studentId)));
    }

    @Operation(summary = "Actualizar una categoría y sincronizar con todos los cursos")
    @PatchMapping("/{id}/category/{categorySlug}")
    public ResponseEntity<LearningApiResponse<CourseResponse>> assignCategory(
            @PathVariable Long id,
            @PathVariable String categorySlug) {
        return ResponseEntity.ok(
                LearningApiResponse.success("Categoría asignada",
                        courseService.assignCategory(id, categorySlug)));
    }
}