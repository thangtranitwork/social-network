package com.stu.socialnetworkapi.service.itf;

import java.util.List;

public interface KeywordExtractorService {
    List<String> extract(String content);
}
