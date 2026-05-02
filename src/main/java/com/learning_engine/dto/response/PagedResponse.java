package com.learning_engine.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private Long totalItems;
    private int totalPages;
    private Boolean first;
    private Boolean last;
    private Boolean empty;

    public static<T> PagedResponse<T> of (Page<T> page){
        PagedResponse<T> response = new PagedResponse<T>();

        var currentPage = page.getNumber();
        var content = page.getContent();

        response.setContent(content);
        response.setPage(currentPage);
        response.setSize(page.getSize());
        response.setTotalItems(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(currentPage == 0);
        response.setLast(currentPage > page.getTotalElements() - 1);
        response.setEmpty(content.isEmpty());

        return response;
    }

    public static <T> PagedResponse<T> empty() {
        PagedResponse<T> r = new PagedResponse<>();
        r.setContent(List.of());
        r.setPage(0); r.setSize(0); r.setTotalItems(0L);
        r.setTotalPages(0); r.setFirst(true); r.setLast(true); r.setEmpty(true);
        return r;
    }

    public static <T> PagedResponse<T> of(List<T> list) {
        PagedResponse<T> r = new PagedResponse<>();
        r.setContent(list);
        r.setPage(0); r.setSize(list.size()); r.setTotalItems((long) list.size());
        r.setTotalPages(1); r.setFirst(true); r.setLast(true); r.setEmpty(list.isEmpty());
        return r;
    }
}
