package uk.gov.hmcts.opal.dto.legacy.utils;

import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j(topic = "opal.ValidationUtils")
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
