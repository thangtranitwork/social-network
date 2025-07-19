package com.stu.socialnetworkapi.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

@Node
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "text")
public class Keyword {
    @Id
    String text;
    int score;

    public static final int GET_SCORE = 1;
    public static final int LIKE_SCORE = 1;
    public static final int COMMENT_SCORE = 3;
    public static final int SHARE_SCORE = 5;

}
