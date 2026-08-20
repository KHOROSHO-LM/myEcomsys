package org.example.ecms.service;

import org.example.ecms.entity.Banner;
import org.example.ecms.entity.Notice;
import org.example.ecms.mapper.BannerMapper;
import org.example.ecms.mapper.NoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {

    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private NoticeMapper noticeMapper;

    public List<Banner> listBanners() {
        return bannerMapper.selectAll();
    }

    public List<Notice> listNotices() {
        return noticeMapper.selectAll();
    }
}
