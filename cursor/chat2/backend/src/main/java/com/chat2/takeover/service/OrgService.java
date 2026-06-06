package com.chat2.takeover.service;

import com.chat2.takeover.dto.OrgTreeNodeVO;
import com.chat2.takeover.dto.TutorOrgVO;
import com.chat2.takeover.entity.OrgDepartment;
import com.chat2.takeover.entity.Tutor;
import com.chat2.takeover.repository.OrgDepartmentRepository;
import com.chat2.takeover.repository.TutorRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrgService {

    private final OrgDepartmentRepository orgDepartmentRepository;
    private final TutorRepository tutorRepository;

    public OrgService(OrgDepartmentRepository orgDepartmentRepository, TutorRepository tutorRepository) {
        this.orgDepartmentRepository = orgDepartmentRepository;
        this.tutorRepository = tutorRepository;
    }

    public List<OrgTreeNodeVO> getOrgTree() {
        List<OrgDepartment> departments = orgDepartmentRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, String> deptNameMap = departments.stream()
                .collect(Collectors.toMap(OrgDepartment::getId, OrgDepartment::getName));

        List<Tutor> sourceTutors = tutorRepository.findByTakeoverRoleFalseOrderByIdAsc();
        Map<Long, List<TutorOrgVO>> tutorsByDept = sourceTutors.stream()
                .filter(t -> t.getOrgDepartmentId() != null)
                .collect(Collectors.groupingBy(
                        Tutor::getOrgDepartmentId,
                        Collectors.mapping(t -> toTutorOrgVO(t, deptNameMap, departments), Collectors.toList())));

        Map<Long, List<OrgDepartment>> childrenMap = departments.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.groupingBy(OrgDepartment::getParentId));

        return departments.stream()
                .filter(d -> d.getParentId() == null)
                .sorted(Comparator.comparing(OrgDepartment::getSortOrder).thenComparing(OrgDepartment::getId))
                .map(d -> buildNode(d, childrenMap, tutorsByDept))
                .toList();
    }

    public Set<Long> collectDeptIdsIncludingDescendants(Collection<Long> deptIds) {
        if (deptIds == null || deptIds.isEmpty()) {
            return Set.of();
        }
        List<OrgDepartment> all = orgDepartmentRepository.findAllByOrderBySortOrderAscIdAsc();
        Map<Long, List<Long>> childrenMap = all.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.groupingBy(
                        OrgDepartment::getParentId,
                        Collectors.mapping(OrgDepartment::getId, Collectors.toList())));

        Set<Long> result = new HashSet<>();
        for (Long id : deptIds) {
            collectDescendants(id, childrenMap, result);
        }
        return result;
    }

    private void collectDescendants(Long id, Map<Long, List<Long>> childrenMap, Set<Long> result) {
        result.add(id);
        for (Long child : childrenMap.getOrDefault(id, List.of())) {
            collectDescendants(child, childrenMap, result);
        }
    }

    private OrgTreeNodeVO buildNode(
            OrgDepartment dept,
            Map<Long, List<OrgDepartment>> childrenMap,
            Map<Long, List<TutorOrgVO>> tutorsByDept) {
        List<OrgTreeNodeVO> children = childrenMap.getOrDefault(dept.getId(), List.of()).stream()
                .sorted(Comparator.comparing(OrgDepartment::getSortOrder).thenComparing(OrgDepartment::getId))
                .map(child -> buildNode(child, childrenMap, tutorsByDept))
                .toList();
        List<TutorOrgVO> tutors = tutorsByDept.getOrDefault(dept.getId(), List.of());
        return new OrgTreeNodeVO(dept.getId(), dept.getName(), dept.getDeptType(), children, tutors);
    }

    private TutorOrgVO toTutorOrgVO(Tutor t, Map<Long, String> deptNameMap, List<OrgDepartment> departments) {
        String path = buildOrgPath(t.getOrgDepartmentId(), departments, deptNameMap);
        return new TutorOrgVO(t.getId(), t.getName(), t.getOrgDepartmentId(), path);
    }

    private String buildOrgPath(Long deptId, List<OrgDepartment> departments, Map<Long, String> nameMap) {
        if (deptId == null) {
            return "";
        }
        Map<Long, Long> parentMap = departments.stream()
                .collect(Collectors.toMap(OrgDepartment::getId, d -> d.getParentId() != null ? d.getParentId() : -1L));
        LinkedList<String> parts = new LinkedList<>();
        Long current = deptId;
        while (current != null && current > 0) {
            parts.addFirst(nameMap.getOrDefault(current, ""));
            Long parent = parentMap.get(current);
            current = (parent != null && parent > 0) ? parent : null;
        }
        return String.join(" / ", parts);
    }
}
