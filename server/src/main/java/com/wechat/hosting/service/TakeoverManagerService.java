package com.wechat.hosting.service;

import com.wechat.hosting.dto.TakeoverManagerVO;
import com.wechat.hosting.entity.SysUser;
import com.wechat.hosting.entity.TakeoverManager;
import com.wechat.hosting.repository.HostingAssignmentRepository;
import com.wechat.hosting.repository.SysUserRepository;
import com.wechat.hosting.repository.TakeoverManagerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TakeoverManagerService {

    private final TakeoverManagerRepository takeoverManagerRepository;
    private final SysUserRepository sysUserRepository;
    private final HostingAssignmentRepository hostingAssignmentRepository;

    public TakeoverManagerService(TakeoverManagerRepository takeoverManagerRepository,
                                  SysUserRepository sysUserRepository,
                                  HostingAssignmentRepository hostingAssignmentRepository) {
        this.takeoverManagerRepository = takeoverManagerRepository;
        this.sysUserRepository = sysUserRepository;
        this.hostingAssignmentRepository = hostingAssignmentRepository;
    }

    public List<TakeoverManagerVO> listAll() {
        List<TakeoverManager> managers = takeoverManagerRepository.findByStatus(1);
        if (managers.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> userMap = sysUserRepository.findByIdIn(
                managers.stream().map(TakeoverManager::getUserId).toList())
                .stream().collect(Collectors.toMap(SysUser::getId, u -> u));
        return managers.stream().map(m -> {
            SysUser user = userMap.get(m.getUserId());
            int activeCount = hostingAssignmentRepository
                    .findByTakeoverManagerIdAndStatus(m.getId(), 1).size();
            return new TakeoverManagerVO(
                    m.getId(), m.getUserId(), user != null ? user.getRealName() : "",
                    m.getMaxTutorCount(), activeCount, m.getStatus());
        }).toList();
    }
}
