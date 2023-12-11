package uk.gov.hmcts.opal.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class DateDto {
    private Integer dayOfBirth;
    private Integer monthOfBirth;
    private Integer yearOfBirth;

    public static DateDto fromLocalDate(LocalDate local) {
        return DateDto.builder()
            .yearOfBirth(local.getYear())
            .monthOfBirth(local.getMonthValue())
            .dayOfBirth(local.getDayOfMonth())
            .build();
    }
}
