package uk.gov.hmcts.opal.dto.legacy.utils;

import java.util.Objects;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import uk.gov.hmcts.opal.common.legacy.service.GatewayService.Response;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Slf4j(topic = "opal.LegacyValidationUtils")
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

    /* This looks like a candidate for communalization for several legacy services...*/
    public static <T> void checkResponseForError(Response<T> response, String method) {
        if (response.isError()) {
            log.error(":{}: legacy error HTTP {}", method, response.code);
            if (response.isException()) {
                log.error(":{}: exception:", method, response.exception);
            } else if (response.isLegacyFailure()) {
                log.error(":{}: legacy failure body:\n{}", method, response.body);
            } else if (response.code.equals(HttpStatusCode.valueOf(HttpStatus.NOT_FOUND.value()))) {
                log.error(":{}: legacy not found", method);
            }
        } else if (response.isSuccessful()) {
            log.info(":{}: legacy success.", method);
        }
    }
}
