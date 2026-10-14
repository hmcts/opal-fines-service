package uk.gov.hmcts.opal.service.filehandler;

import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserEnum;

@Component
public class FileHandlerSystemUserContext {

    private final ThreadLocal<SystemUserEnum> currentSystemUser = new ThreadLocal<>();

    public Optional<SystemUserEnum> getCurrentSystemUser() {
        return Optional.ofNullable(currentSystemUser.get());
    }

    public SystemUserEnum getRequiredSystemUser() {
        return getCurrentSystemUser().orElseThrow(
            () -> new IllegalStateException("No system user selected for file-handler request")
        );
    }

    public <T> T executeAs(SystemUserEnum systemUser, Supplier<T> operation) {
        SystemUserEnum previousSystemUser = currentSystemUser.get();
        currentSystemUser.set(systemUser);
        try {
            return operation.get();
        } finally {
            if (previousSystemUser == null) {
                currentSystemUser.remove();
            } else {
                currentSystemUser.set(previousSystemUser);
            }
        }
    }
}
