package uk.gov.hmcts.opal.launchdarkly;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The <code>FeatureToggle</code> annotation is used to define the conditional execution of the business method.
 * The condition is defined by the launchdarkly feature toggle.
 * For example:
 * To execute a business method only when feature toggle is enabled (using the default value=true).
 * <pre>
 * &#064;FeatureToggle(feature=&quot;my-new-feature&quot;)
 * public void businessMethod(Object param);
 *
 * To execute a business method only when feature toggle is disabled.
 * &#064;FeatureToggle(feature=&quot;my-new-feature&quot;, value=false)
 *  * public void businessMethod(Object param);
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FeatureToggle {

    /**
     * The name of the feature toggle to be checked.
     *
     * @return String The feature name
     */
    String feature();

    /**
     * Indicates the boolean value of feature toggle, for which to invoke the execution of the business method.
     *
     * @return boolean value
     */
    boolean value() default true;

    Class<? extends Throwable> throwException() default FeatureDisabledException.class;

}
