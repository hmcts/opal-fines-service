package uk.gov.hmcts.opal.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class DateDto {
    @JsonProperty("day_of_month")
    private Integer dayOfMonth;
    @JsonProperty("month_of_year")
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
        if (year != null) {
            Integer month = Optional.ofNullable(monthOfYear).filter(inRange(1, 12)).orElse(null);
            if (month != null) {
                int high = switch (month) {
                    case 2 -> 29;
                    case 4, 6, 9, 11 -> 30;
                    default -> 31;
                };
                Integer day = Optional.ofNullable(dayOfMonth).filter(inRange(1, high)).orElse(null);
                if (day != null) {
                    return LocalDate.of(year, month, day);
                }
            }
        }
        return null;
    }

    private Predicate<Integer> inRange(int low, int high) {
        return i -> (i >= low && i <= high);
    }
}
