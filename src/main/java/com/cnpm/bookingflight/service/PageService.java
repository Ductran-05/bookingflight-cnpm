package com.cnpm.bookingflight.service;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cnpm.bookingflight.domain.Page;
import com.cnpm.bookingflight.dto.request.PageRequest;
import com.cnpm.bookingflight.dto.response.APIResponse;
import com.cnpm.bookingflight.exception.AppException;
import com.cnpm.bookingflight.exception.ErrorCode;
import com.cnpm.bookingflight.mapper.PageMapper;
import com.cnpm.bookingflight.repository.PageRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PageService {
    final PageRepository pageRepository;
    final PageMapper pageMapper;

    public ResponseEntity<APIResponse<List<Page>>> getAllPages() {
        APIResponse<List<Page>> response = APIResponse.<List<Page>>builder()
                .data(pageRepository.findAll())
                .status(200)
                .message("get all pages successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Page>> getPageById(Long id) {
        APIResponse<Page> response = APIResponse.<Page>builder()
                .data(pageRepository.findById(id).orElseThrow(() -> new RuntimeException("Page not found")))
                .status(200)
                .message("get page by id successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Page>> createPage(PageRequest request) {
        pageRepository.findByPageName(request.getPageName())
                .ifPresent(p -> {
                    throw new AppException(ErrorCode.EXISTED);
                });
        APIResponse<Page> response = APIResponse.<Page>builder()
                .data(pageRepository.save(pageMapper.toPage(request)))
                .status(200)
                .message("create page successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Page>> updatePage(Long id, PageRequest request) {
        pageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        pageRepository.findByPageName(request.getPageName())
                .ifPresent(p -> {
                    throw new AppException(ErrorCode.EXISTED);
                });
        Page existingPage = pageMapper.toPage(request);
        existingPage.setId(id);
        pageRepository.save(existingPage);
        APIResponse<Page> response = APIResponse.<Page>builder()
                .data(pageRepository.save(existingPage))
                .status(200)
                .message("update page successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<APIResponse<Void>> deletePage(Long id) {
        pageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        pageRepository.deleteById(id);
        APIResponse<Void> response = APIResponse.<Void>builder()
                .status(204)
                .message("delete page successfully")
                .build();
        return ResponseEntity.ok(response);
    }
}
