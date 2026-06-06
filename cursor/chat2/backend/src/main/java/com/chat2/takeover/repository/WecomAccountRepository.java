package com.chat2.takeover.repository;

import com.chat2.takeover.entity.WecomAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WecomAccountRepository extends JpaRepository<WecomAccount, Long> {

    List<WecomAccount> findByTutorIdOrderByIdAsc(Long tutorId);

    List<WecomAccount> findByTutorIdInOrderByTutorIdAscIdAsc(Collection<Long> tutorIds);
}
