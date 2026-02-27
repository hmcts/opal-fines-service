package uk.gov.hmcts.opal.versioning;

import java.lang.reflect.Method;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class ApiVersionRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

    @Nullable
    private final String defaultVersion;

    public ApiVersionRequestMappingHandlerMapping(RequestVersionResolver versionResolver,
        @Nullable String defaultVersion) {
        this.defaultVersion = defaultVersion;
    }

    @Override
    @Nullable
    protected RequestCondition<?>
        getCustomTypeCondition(@NonNull Class<?> handlerType) {

        ApiVersion apiVersion = AnnotatedElementUtils.findMergedAnnotation(handlerType, ApiVersion.class);
        if (apiVersion != null && apiVersion.value().length > 0) {
            return new ApiVersionRequestCondition(apiVersion.value());
        }

        return null;
    }

    @Override
    @Nullable
    protected RequestCondition<?> getCustomMethodCondition(@NonNull
        Method method) {

        ApiVersion apiVersion = AnnotatedElementUtils.findMergedAnnotation(method, ApiVersion.class);
        if (apiVersion != null && apiVersion.value().length > 0) {
            return new ApiVersionRequestCondition(apiVersion.value());
        }

        if (defaultVersion != null) {
            return new ApiVersionRequestCondition(new String[]{defaultVersion});
        }

        return null;
    }
}
