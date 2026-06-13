package com.studyroom.security;

import com.studyroom.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser loginUser)) {
            throw new BusinessException("未登录");
        }
        return loginUser;
    }

    public static Long getCurrentUserId() {
        Long userId = getCurrentUser().getUserId();
        if (userId == null) {
            throw new BusinessException("登录信息无效，请重新登录");
        }
        return userId;
    }

    public static List<Long> getCurrentCampusIds() {
        return getCurrentUser().getCampusIds();
    }

    public static void checkCampusAccess(Long campusId) {
        if (campusId == null || !getCurrentCampusIds().contains(campusId)) {
            throw new BusinessException("无权访问该校区数据");
        }
    }
}
