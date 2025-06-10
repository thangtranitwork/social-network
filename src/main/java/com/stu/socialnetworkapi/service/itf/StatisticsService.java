package com.stu.socialnetworkapi.service.itf;

import com.stu.socialnetworkapi.dto.response.PostStatisticsResponse;
import com.stu.socialnetworkapi.dto.response.UserStatisticsResponse;

public interface StatisticsService {
    UserStatisticsResponse generateUserStatistics();
    PostStatisticsResponse generatePostStatistics();
}
