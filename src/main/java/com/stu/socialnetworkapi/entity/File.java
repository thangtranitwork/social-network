package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.FilePrivacy;
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
    FilePrivacy privacy;

    @Relationship(type = "UPLOAD_FILE", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    User uploader;

    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Chat chat;

    @Relationship(type = "ATTACH_FILES", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Post post;

    @Relationship(type = "ATTACH_FILE", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    Comment comment;

    public static String getPath(File file) {
        if (file == null) return null;

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/files/")
                .path(file.getId())
                .toUriString();
    }

    public static String getPath(String id) {
        if (id == null) return null;

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/v1/files/")
                .path(id)
                .toUriString();
    }

    public static List<String> getPath(List<File> attachedFiles) {
        if (attachedFiles == null) return Collections.emptyList();
        return attachedFiles.stream()
                .map(File::getPath)
                .toList();
    }

    public static String getId(String path) {
        if (path == null) return null;
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
