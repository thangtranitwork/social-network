package com.stu.socialnetworkapi.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Neo4jPageable {
    private long skip = 0;
    private long limit = 20;

    public Neo4jPageable() {
    }

    public Neo4jPageable(long skip, long limit) {
        if (skip >= 0) this.skip = skip;
        if (limit >= 0) this.limit = limit;
    }

    @Override
    public String toString() {
        return "Neo4jPageable{" +
                "skip=" + skip +
                ", limit=" + limit +
                '}';
    }
}
