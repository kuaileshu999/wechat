package com.wechat.hosting.service;

import com.wechat.hosting.entity.TeachingGroup;
import com.wechat.hosting.repository.TeachingGroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachingGroupService {

    private final TeachingGroupRepository teachingGroupRepository;

    public TeachingGroupService(TeachingGroupRepository teachingGroupRepository) {
        this.teachingGroupRepository = teachingGroupRepository;
    }

    public List<TeachingGroup> listActive() {
        return teachingGroupRepository.findByStatusOrderBySortOrderAsc(1);
    }
}
