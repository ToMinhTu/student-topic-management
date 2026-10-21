package com.example.student_topic_management.assessment.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class GradePolicy {

    public BigDecimal average(List<BigDecimal> scores) {
        if (scores == null || scores.isEmpty()) {
            throw new IllegalArgumentException("Chưa có điểm để tính trung bình");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal score : scores) {
            if (score == null
                    || score.compareTo(BigDecimal.ZERO) < 0
                    || score.compareTo(BigDecimal.TEN) > 0) {
                throw new IllegalArgumentException("Điểm thành phần phải nằm trong khoảng 0 đến 10");
            }
            total = total.add(score);
        }

        return total.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
    }
}
