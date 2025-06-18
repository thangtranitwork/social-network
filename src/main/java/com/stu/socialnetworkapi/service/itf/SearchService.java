package com.stu.socialnetworkapi.service.itf;

public interface SearchService {
    Object search(String query, SearchType searchType, long skip, long limit);

    enum SearchType {
        NOT_SET,
        USER,
        POST
    }
}
