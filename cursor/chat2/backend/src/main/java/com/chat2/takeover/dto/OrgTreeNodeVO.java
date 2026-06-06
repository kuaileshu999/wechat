package com.chat2.takeover.dto;

import java.util.List;

public record OrgTreeNodeVO(
        Long id,
        String name,
        String deptType,
        List<OrgTreeNodeVO> children,
        List<TutorOrgVO> tutors) {
}
