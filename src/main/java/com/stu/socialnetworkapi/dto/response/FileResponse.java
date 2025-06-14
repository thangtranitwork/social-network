package com.stu.socialnetworkapi.dto.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class FileResponse {
    String name;
    String contentType;
    Resource resource;
}
