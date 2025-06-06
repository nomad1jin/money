package Baeksa.money.domain.streams.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class StreamReqDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StreamTestDto{
        private String userId;
        private String theme;
        private String amount;
        private String description;
        private String documentURL;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterUserDto{
        private String userId;
        private String name;
        private String orgType;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipApprovalDto{
        private String requestId;
        private String approverId;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MembershipRejectionDto{
        private String requestId;
        private String rejectorId;
    }


    /// ////////////////////////////////////////////////

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class streamApproveDto {
        private String approverId;
        private String userName;
        private String requestId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class streamRejectDto {
        private String rejectorId;
        private String userName;
        private String requestId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class streamLedgerDto {
        private String userId;
        private String theme;   //name_year_semester
        private Long amount;
        private String description;
        private String documentURL;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class streamLedgerApproveDto {
        private String approverId;
        private String ledgerEntryId;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class streamLedgerRejectDto {
        private String rejectorId;
        private String ledgerEntryId;
    }
}
