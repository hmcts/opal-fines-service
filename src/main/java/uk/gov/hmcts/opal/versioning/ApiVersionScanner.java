package uk.gov.hmcts.opal.versioning;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.hmcts.opal.versioning.ApiVersionRequestCondition.VersionExpression;

@Component
public class ApiVersionScanner implements ApplicationListener<ContextRefreshedEvent> {

    @Nullable
    private final String defaultVersion;
    private final RequestMappingHandlerMapping handlerMapping;
    private final Set<String> rawVersions = new LinkedHashSet<>();
    private final AtomicBoolean scanned = new AtomicBoolean(false);

    @Autowired
    public ApiVersionScanner(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping,
        @Value("${version.default:}") String defaultVersion) {
        this.handlerMapping = handlerMapping;
        this.defaultVersion = (defaultVersion == null || defaultVersion.isBlank()) ? null : defaultVersion.trim();
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (!scanned.compareAndSet(false, true)) {
            return;
        }

        Map<?, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        for (HandlerMethod hm : handlerMethods.values()) {
            ApiVersion methodAnn = AnnotatedElementUtils.findMergedAnnotation(hm.getMethod(), ApiVersion.class);
            if (methodAnn != null) {
                for (String v : methodAnn.value()) {
                    if (v != null && !v.isBlank()) {
                        rawVersions.add(v.trim());
                    }
                }
            }
            ApiVersion typeAnn = AnnotatedElementUtils.findMergedAnnotation(hm.getBeanType(), ApiVersion.class);
            if (typeAnn != null) {
                for (String v : typeAnn.value()) {
                    if (v != null && !v.isBlank()) {
                        rawVersions.add(v.trim());
                    }
                }
            }
        }

        if (this.defaultVersion != null) {
            rawVersions.add(this.defaultVersion);
        }

        System.out.println("\n Version Scan: \n \n" + rawVersions + "\n\n");
    }

    public List<String> getRawVersions() {
        return List.copyOf(rawVersions);
    }

    public List<VersionExpression> getVersionExpressions() {
        return rawVersions.stream()
            .map(VersionExpression::parse)
            .toList();
    }

}
