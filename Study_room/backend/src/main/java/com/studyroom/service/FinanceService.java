package com.studyroom.service;

import com.studyroom.common.CampusScope;
import com.studyroom.dto.FinanceReportVO;
import com.studyroom.entity.Campus;
import com.studyroom.entity.Order;
import com.studyroom.repository.CampusRepository;
import com.studyroom.repository.OrderRepository;
import com.studyroom.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FinanceService {

    private final OrderRepository orderRepository;
    private final CampusRepository campusRepository;

    public List<FinanceReportVO> reportByDay(LocalDate startDate, LocalDate endDate, Long campusId) {
        return aggregate(startDate, endDate, campusId, "DAY");
    }

    public List<FinanceReportVO> reportByMonth(String month, Long campusId) {
        YearMonth ym = YearMonth.parse(month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return aggregate(start, end, campusId, "MONTH");
    }

    public List<FinanceReportVO> reportByCampus(LocalDate startDate, LocalDate endDate) {
        if (CampusScope.isEmpty()) {
            return List.of();
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        Map<Long, FinanceReportVO> map = new HashMap<>();
        for (Long id : campusIds) {
            Campus campus = campusRepository.findById(id).orElse(null);
            map.put(id, FinanceReportVO.builder()
                    .campusId(id)
                    .campusName(campus != null ? campus.getName() : "")
                    .totalPaidAmount(BigDecimal.ZERO)
                    .totalConsumedAmount(BigDecimal.ZERO)
                    .totalPendingAmount(BigDecimal.ZERO)
                    .build());
        }

        List<Order> orders = orderRepository.search(campusIds, null, startDate, endDate,
                org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

        for (Order order : orders) {
            FinanceReportVO vo = map.get(order.getCampusId());
            if (vo == null) continue;
            accumulate(vo, order);
        }
        return new ArrayList<>(map.values());
    }

    private List<FinanceReportVO> aggregate(LocalDate startDate, LocalDate endDate, Long campusId, String type) {
        if (CampusScope.isEmpty()) {
            return List.of();
        }
        List<Long> campusIds = CampusScope.currentCampusIds();
        if (campusId != null) {
            SecurityUtils.checkCampusAccess(campusId);
            campusIds = List.of(campusId);
        }

        Map<String, FinanceReportVO> map = new HashMap<>();
        List<Order> orders = orderRepository.search(campusIds, campusId, startDate, endDate,
                org.springframework.data.domain.PageRequest.of(0, 10000)).getContent();

        for (Order order : orders) {
            String key = type.equals("DAY") ? order.getPaymentDate().toString()
                    : order.getPaymentDate().getYear() + "-" + String.format("%02d", order.getPaymentDate().getMonthValue());
            FinanceReportVO vo = map.computeIfAbsent(key, k -> FinanceReportVO.builder()
                    .date(type.equals("DAY") ? order.getPaymentDate() : null)
                    .month(type.equals("MONTH") ? k : null)
                    .campusId(order.getCampusId())
                    .totalPaidAmount(BigDecimal.ZERO)
                    .totalConsumedAmount(BigDecimal.ZERO)
                    .totalPendingAmount(BigDecimal.ZERO)
                    .build());
            accumulate(vo, order);
        }
        return new ArrayList<>(map.values());
    }

    private void accumulate(FinanceReportVO vo, Order order) {
        BigDecimal effectivePaid = order.getPaidAmount().subtract(order.getRefundedAmount());
        vo.setTotalPaidAmount(vo.getTotalPaidAmount().add(effectivePaid));
        vo.setTotalConsumedAmount(vo.getTotalConsumedAmount().add(order.getConsumedAmount()));
        vo.setTotalPendingAmount(vo.getTotalPendingAmount()
                .add(effectivePaid.subtract(order.getConsumedAmount()).max(BigDecimal.ZERO)));
    }
}
