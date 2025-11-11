package uk.gov.hmcts.opal.dto.legacy.utils;

import java.util.Objects;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ValidationUtils {

    public static boolean hasExactlyOneNonNull(Object... fields) {
        int count = 0;
        for (Object f : fields) {
            if (Objects.nonNull(f)) {
                count++;
            }
        }
        return count == 1;
    }
}
