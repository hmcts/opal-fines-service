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
    private Integer dayOfMonth;
    private Integer monthOfYear;
    private Integer year;

    public static DateDto fromLocalDate(LocalDate local) {
        return DateDto.builder()
            .year(local.getYear())
            .monthOfYear(local.getMonthValue())
            .dayOfMonth(local.getDayOfMonth())
            .build();
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(year, monthOfYear, dayOfMonth);
    }
}
