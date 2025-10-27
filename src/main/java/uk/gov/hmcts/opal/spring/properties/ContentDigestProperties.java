package uk.gov.hmcts.opal.spring.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "opal.content-digest")
@Getter
@Validated
@AllArgsConstructor(onConstructor_ = @ConstructorBinding)
public class ContentDigestProperties {

    @Valid
    @NotNull
    private final Request request;

    @Valid
    @NotNull
    private final Response response;

    @Getter
    @AllArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class Request {

        @NotNull
        private final Boolean enforce;

        @NotEmpty
        private final Map<String, String> supportedAlgorithms;

        public boolean isEnforced() {
            return enforce != null && enforce;
        }
    }

    @Getter
    @AllArgsConstructor(onConstructor_ = @ConstructorBinding)
    public static class Response {

        @NotNull
        private final Boolean autoGenerate;

        @NotNull
        private final String algorithm;
    }
}