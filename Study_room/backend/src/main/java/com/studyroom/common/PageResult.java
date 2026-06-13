package com.studyroom.common;

import lombok.Data;

@Data
public class PageResult<T> {

    private java.util.List<T> list;
    private long total;
    private int page;
    private int size;

    public PageResult(java.util.List<T> list, long total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
