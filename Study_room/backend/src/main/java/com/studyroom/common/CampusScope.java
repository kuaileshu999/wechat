package com.studyroom.common;

import com.studyroom.security.SecurityUtils;

import java.util.List;

public final class CampusScope {

    private CampusScope() {
    }

    public static List<Long> currentCampusIds() {
        List<Long> campusIds = SecurityUtils.getCurrentCampusIds();
        return campusIds == null ? List.of() : campusIds;
    }

    public static boolean isEmpty() {
        return currentCampusIds().isEmpty();
    }

    public static <T> PageResult<T> emptyPage(int page, int size) {
        return new PageResult<>(List.of(), 0, page, size);
    }
}
