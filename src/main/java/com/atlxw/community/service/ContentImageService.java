package com.atlxw.community.service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

public interface ContentImageService {
    String handleUploadImage(HttpServletRequest request, MultipartFile file);
}
