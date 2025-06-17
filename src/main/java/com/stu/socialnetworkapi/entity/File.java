package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class File {
    @Id
    String id;
    String name;
    String contentType;

    @Relationship(type = "UPLOAD_FILE", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    User uploader;

    @Setter
    private static String selfOrigin;

    public static String getPath(File file) {
        if (file == null) return null;

        return getPath(file.id);
    }

    public static String getPath(String id) {
        if (id == null) return null;

        try {
            return ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/v1/files/")
                    .path(id)
                    .toUriString();
        } catch (IllegalStateException e) {
            return selfOrigin + "/v1/files/" + id;
        }
    }

    public static List<String> getPath(List<File> attachedFiles) {
        if (attachedFiles == null) return Collections.emptyList();
        return attachedFiles.stream()
                .map(File::getPath)
                .toList();
    }

    public static List<String> getPathByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return ids.stream()
                .map(File::getPath)
                .toList();
    }

    public static String getId(String path) {
        if (path == null) return null;
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
