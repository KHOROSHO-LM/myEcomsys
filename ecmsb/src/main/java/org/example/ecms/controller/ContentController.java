package org.example.ecms.controller;

import org.example.ecms.common.Result;
import org.example.ecms.entity.Banner;
import org.example.ecms.entity.Notice;
import org.example.ecms.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/banners")
    public Result<List<Banner>> banners() {
        return Result.success(contentService.listBanners());
    }

    @GetMapping("/notices")
    public Result<List<Notice>> notices() {
        return Result.success(contentService.listNotices());
    }
}
