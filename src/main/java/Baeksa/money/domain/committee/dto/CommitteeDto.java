package Baeksa.money.domain.committee.dto;

import Baeksa.money.domain.ledger.enums.Semester;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommitteeDto {

    private String studentId;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LedgerReqDto{
        private String theme;           //name_year_semester
        private int year;
        private Semester semester;
        private Long amount;
        private String description;
        private String documentURL;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class approveDto {
        private String requestId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class rejectDto {
        private String requestId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class testDto {
        private String requestId;
        private String approverId;

    }

}
