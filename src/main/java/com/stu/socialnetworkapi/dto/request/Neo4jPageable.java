package com.stu.socialnetworkapi.dto.request;

import com.stu.socialnetworkapi.enums.GetType;
import lombok.Getter;

@Getter
public class Neo4jPageable {
    private long skip = 0;
    private long limit = 20;
    private GetType type = GetType.RELEVANT;

    public Neo4jPageable() {
    }

    public Neo4jPageable(long skip, long limit, String type) {
        if (skip >= 0) this.skip = skip;
        if (limit >= 0) this.limit = limit;
        try {
            this.type = GetType.valueOf(type);
        } catch (Exception e) {
            //do nothing
        }
    }

    public void setSkip(long skip) {
        if (skip >= 0) this.skip = skip;
    }

    public void setLimit(long limit) {
        if (limit >= 0) this.limit = limit;
    }

    public void setType(String type) {
        try {
            this.type = GetType.valueOf(type);
        } catch (Exception e) {
            //do nothing
        }
    }

    @Override
    public String toString() {
        return "Neo4jPageable{" +
                "skip=" + skip +
                ", limit=" + limit +
                '}';
    }
}
