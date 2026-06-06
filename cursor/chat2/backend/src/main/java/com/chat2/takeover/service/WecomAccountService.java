package com.chat2.takeover.service;

import com.chat2.takeover.dto.TutorVO;
import com.chat2.takeover.dto.WecomAccountVO;
import com.chat2.takeover.entity.Tutor;
import com.chat2.takeover.entity.WecomAccount;
import com.chat2.takeover.repository.WecomAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class WecomAccountService {

    private final WecomAccountRepository wecomAccountRepository;
    private final TutorService tutorService;

    public WecomAccountService(WecomAccountRepository wecomAccountRepository, TutorService tutorService) {
        this.wecomAccountRepository = wecomAccountRepository;
        this.tutorService = tutorService;
    }

    public List<WecomAccountVO> listByTutor(Long tutorId) {
        Tutor tutor = tutorService.require(tutorId);
        return wecomAccountRepository.findByTutorIdOrderByIdAsc(tutorId).stream()
                .map(a -> toVO(a, tutor.getName()))
                .toList();
    }

    public List<WecomAccountVO> listByTutors(Collection<Long> tutorIds, String gradeLevel, String subject) {
        if (tutorIds == null || tutorIds.isEmpty()) {
            return List.of();
        }
        Map<Long, String> tutorNames = tutorService.listAll().stream()
                .collect(Collectors.toMap(TutorVO::id, TutorVO::name));
        return wecomAccountRepository.findByTutorIdInOrderByTutorIdAscIdAsc(tutorIds).stream()
                .filter(a -> matchFilter(a, gradeLevel, subject))
                .map(a -> toVO(a, tutorNames.getOrDefault(a.getTutorId(), "")))
                .toList();
    }

    public List<WecomAccountVO> listAll() {
        Map<Long, String> tutorNames = tutorService.listAll().stream()
                .collect(Collectors.toMap(TutorVO::id, TutorVO::name));
        return wecomAccountRepository.findAll().stream()
                .map(a -> toVO(a, tutorNames.getOrDefault(a.getTutorId(), "")))
                .toList();
    }

    public Map<Long, WecomAccount> requireAllMap() {
        return wecomAccountRepository.findAll().stream()
                .collect(Collectors.toMap(WecomAccount::getId, Function.identity()));
    }

    public WecomAccount require(Long id) {
        return wecomAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("企微账号不存在: " + id));
    }

    private boolean matchFilter(WecomAccount a, String gradeLevel, String subject) {
        if (StringUtils.hasText(gradeLevel) && !gradeLevel.equals(a.getGradeLevel())) {
            return false;
        }
        if (StringUtils.hasText(subject) && !subject.equals(a.getSubject())) {
            return false;
        }
        return true;
    }

    private WecomAccountVO toVO(WecomAccount a, String tutorName) {
        String grade = a.getGradeLevel() != null ? a.getGradeLevel() : "";
        String subj = a.getSubject() != null ? a.getSubject() : "";
        String label = a.getAccountName();
        if (!grade.isEmpty() || !subj.isEmpty()) {
            String extra = grade + ((!grade.isEmpty() && !subj.isEmpty()) ? "·" : "") + subj;
            label = a.getAccountName() + "（" + extra + "）";
        }
        return new WecomAccountVO(
                a.getId(),
                a.getTutorId(),
                tutorName,
                a.getAccountName(),
                a.getWecomUserId(),
                a.getGradeLevel(),
                a.getSubject(),
                label);
    }
}
