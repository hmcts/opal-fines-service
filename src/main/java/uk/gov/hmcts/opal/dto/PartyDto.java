package uk.gov.hmcts.opal.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class PartyDto implements ToJsonString {
    private Long partyId;
    private boolean organisation;
    private String organisationName;
    private String surname;
    private String forenames;
    private String initials;
    private String title;
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String addressLine4;
    private String addressLine5;
    private String postcode;
    private String accountType;
    private LocalDate dateOfBirth;
    private Short age;
    private String niNumber;
    private LocalDateTime lastChangedDate;
}
