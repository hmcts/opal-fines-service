package uk.gov.hmcts.opal.util;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class AgeUtilTest {

    @Test
    void shouldReturnZeroWhenDateOfBirthIsNull() {
        int age = AgeUtil.calculateAge(null);
        assertThat(age).isEqualTo(0);
    }

    @Test
    void shouldCalculateAgeCorrectly_whenBirthdayAlreadyOccurredThisYear() {
        LocalDate dob = LocalDate.now().minusYears(25).minusDays(1);
        int age = AgeUtil.calculateAge(dob);
        assertThat(age).isEqualTo(25);
    }

    @Test
    void shouldCalculateAgeCorrectly_whenBirthdayIsToday() {
        LocalDate dob = LocalDate.now().minusYears(30);
        int age = AgeUtil.calculateAge(dob);
        assertThat(age).isEqualTo(30);
    }

    @Test
    void shouldCalculateAgeCorrectly_whenBirthdayNotYetOccurredThisYear() {
        LocalDate dob = LocalDate.now().minusYears(40).plusDays(1);
        int age = AgeUtil.calculateAge(dob);
        assertThat(age).isEqualTo(39);
    }

    @Test
    void shouldReturnZeroForTodayBirthDate() {
        LocalDate dob = LocalDate.now();
        int age = AgeUtil.calculateAge(dob);
        assertThat(age).isEqualTo(0);
    }

    @Test
    void shouldHandleLeapYearBirthDate() {
        LocalDate dob = LocalDate.of(2000, 2, 29);
        int age = AgeUtil.calculateAge(dob);
        int expected = java.time.Period.between(dob, LocalDate.now()).getYears();
        assertThat(age).isEqualTo(expected);
    }

}