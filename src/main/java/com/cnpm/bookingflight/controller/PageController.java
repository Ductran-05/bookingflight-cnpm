package com.cnpm.bookingflight.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.dto.request.PageRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.service.PageService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/pages")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageController {
    final PageService pageService;

    @GetMapping()
    public ResponseEntity<APIResponse<List<Page>>> getAllPages() {
        return pageService.getAllPages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<Page>> getPageById(@PathVariable Long id) {
        return pageService.getPageById(id);
    }

    @PostMapping()
    public ResponseEntity<APIResponse<Page>> createPage(@RequestBody PageRequest request) {
        return pageService.createPage(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<Page>> updatePage(@PathVariable Long id, @RequestBody PageRequest request) {
        return pageService.updatePage(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<Void>> deletePage(@PathVariable Long id) {
        return pageService.deletePage(id);
    }
}
