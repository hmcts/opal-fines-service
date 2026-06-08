package uk.gov.hmcts.opal.util;

import java.time.LocalDate;
import java.time.Period;

public class AgeUtil {

    public static final int ADULT_AGE = 18;

    public static int calculateAge(LocalDate dateOfBirth) {
        return dateOfBirth == null ? 0 : Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

}
