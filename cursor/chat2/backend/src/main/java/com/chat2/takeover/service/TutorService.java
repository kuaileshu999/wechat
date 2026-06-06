package com.chat2.takeover.service;

import com.chat2.takeover.dto.TutorVO;
import com.chat2.takeover.entity.Tutor;
import com.chat2.takeover.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorService(TutorRepository tutorRepository) {
        this.tutorRepository = tutorRepository;
    }

    public List<TutorVO> listAll() {
        return tutorRepository.findAll().stream().map(this::toVO).toList();
    }

    public List<TutorVO> listTakeoverTutors() {
        return tutorRepository.findByTakeoverRoleTrue().stream().map(this::toVO).toList();
    }

    public Tutor require(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("辅导不存在: " + id));
    }

    private TutorVO toVO(Tutor t) {
        return new TutorVO(t.getId(), t.getName(), t.getPhone(), t.getTakeoverRole());
    }
}
