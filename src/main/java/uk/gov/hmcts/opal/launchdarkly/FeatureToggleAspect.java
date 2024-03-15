package uk.gov.hmcts.opal.launchdarkly;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.opal.config.properties.LaunchDarklyProperties;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureToggleAspect {

    private final FeatureToggleApi featureToggleApi;
    private final LaunchDarklyProperties properties;


    @Around("execution(* *(*)) && @annotation(featureToggle)")
    public Object checkFeatureEnabled(ProceedingJoinPoint joinPoint, FeatureToggle featureToggle) throws Throwable {

        if (!properties.getEnabled()) {
            log.info("Launch darkly is disabled:: so feature toggle is ignoring launch darkly flag "
                         + featureToggle.feature());
            return;
        }

        if (featureToggle.value() && featureToggleApi.isFeatureEnabled(
            featureToggle.feature(),
            featureToggle.defaultValue()
        )) {
            return joinPoint.proceed();
        } else if (!featureToggle.value() && !featureToggleApi.isFeatureEnabled(
            featureToggle.feature(),
            featureToggle.defaultValue()
        )) {
            return joinPoint.proceed();
        } else {
            String message = String.format(
                "Feature %s is not enabled for method %s",
                featureToggle.feature(),
                joinPoint.getSignature().getName()
            );
            log.warn(message);
            // Check if an exception is specified in the annotation
            if (featureToggle.throwException() != null) {
                // Throw the specified exception
                throw featureToggle.throwException()
                    .getConstructor(String.class)
                    .newInstance(message);
            }
        }
        return null;
    }
}
