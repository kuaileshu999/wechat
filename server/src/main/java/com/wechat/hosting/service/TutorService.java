package com.wechat.hosting.service;

import com.wechat.hosting.dto.TutorAccountVO;
import com.wechat.hosting.dto.TutorVO;
import com.wechat.hosting.entity.SysUser;
import com.wechat.hosting.entity.TeachingGroup;
import com.wechat.hosting.entity.Tutor;
import com.wechat.hosting.entity.TutorWechatAccount;
import com.wechat.hosting.repository.SysUserRepository;
import com.wechat.hosting.repository.TeachingGroupRepository;
import com.wechat.hosting.repository.TutorRepository;
import com.wechat.hosting.repository.TutorWechatAccountRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;
    private final TutorWechatAccountRepository accountRepository;
    private final SysUserRepository sysUserRepository;
    private final TeachingGroupRepository teachingGroupRepository;

    public TutorService(TutorRepository tutorRepository,
                        TutorWechatAccountRepository accountRepository,
                        SysUserRepository sysUserRepository,
                        TeachingGroupRepository teachingGroupRepository) {
        this.tutorRepository = tutorRepository;
        this.accountRepository = accountRepository;
        this.sysUserRepository = sysUserRepository;
        this.teachingGroupRepository = teachingGroupRepository;
    }

    public List<TutorVO> listAll(Long teachingGroupId) {
        List<Tutor> tutors = teachingGroupId == null
                ? tutorRepository.findByStatus(1)
                : tutorRepository.findByTeachingGroupIdAndStatus(teachingGroupId, 1);
        if (tutors.isEmpty()) {
            return List.of();
        }
        Map<Long, SysUser> userMap = loadUsers(tutors);
        Map<Long, TeachingGroup> groupMap = loadGroups(tutors);
        Map<Long, List<TutorWechatAccount>> accountMap = accountRepository
                .findByTutorIdInAndStatus(tutors.stream().map(Tutor::getId).toList(), 1)
                .stream()
                .collect(Collectors.groupingBy(TutorWechatAccount::getTutorId));
        return tutors.stream().map(t -> toVO(t, userMap, groupMap, accountMap)).toList();
    }

    public TutorVO getById(Long tutorId) {
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new IllegalArgumentException("辅导老师不存在"));
        Map<Long, SysUser> userMap = loadUsers(List.of(tutor));
        Map<Long, TeachingGroup> groupMap = loadGroups(List.of(tutor));
        Map<Long, List<TutorWechatAccount>> accountMap = Map.of(
                tutor.getId(), accountRepository.findByTutorIdAndStatus(tutor.getId(), 1));
        return toVO(tutor, userMap, groupMap, accountMap);
    }

    private Map<Long, SysUser> loadUsers(List<Tutor> tutors) {
        List<Long> userIds = tutors.stream().map(Tutor::getUserId).toList();
        return sysUserRepository.findByIdIn(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
    }

    private Map<Long, TeachingGroup> loadGroups(List<Tutor> tutors) {
        Set<Long> groupIds = tutors.stream().map(Tutor::getTeachingGroupId).collect(Collectors.toSet());
        return teachingGroupRepository.findAllById(groupIds).stream()
                .collect(Collectors.toMap(TeachingGroup::getId, g -> g));
    }

    private TutorVO toVO(Tutor tutor, Map<Long, SysUser> userMap, Map<Long, TeachingGroup> groupMap,
                         Map<Long, List<TutorWechatAccount>> accountMap) {
        SysUser user = userMap.get(tutor.getUserId());
        TeachingGroup group = groupMap.get(tutor.getTeachingGroupId());
        List<TutorWechatAccount> accounts = accountMap.getOrDefault(tutor.getId(), List.of());
        List<TutorAccountVO> accountVOs = accounts.stream()
                .map(a -> new TutorAccountVO(a.getId(), a.getAccountName(), a.getSubject(), a.getGrade(),
                        a.getStudentCount(), a.getStatus()))
                .toList();
        return new TutorVO(
                tutor.getId(),
                tutor.getUserId(),
                user != null ? user.getRealName() : "",
                tutor.getTeachingGroupId(),
                group != null ? group.getName() : "",
                accounts.size(),
                accountVOs
        );
    }
}
