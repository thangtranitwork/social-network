package com.stu.socialnetworkapi.entity;

import com.stu.socialnetworkapi.enums.FilePrivacy;
import com.stu.socialnetworkapi.enums.FileType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    FileType type;
    FilePrivacy privacy;

    @Relationship(type = "UPLOAD_FILE", direction = Relationship.Direction.INCOMING)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    User uploader;

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
}
