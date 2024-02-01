package uk.gov.hmcts.opal.service.proxy;

import uk.gov.hmcts.opal.service.DynamicConfigService;

/** A marker interface to help identify code that is to be removed at a future date. */
public interface ProxyInterface {

    public static final String LEGACY_MODE = "LEGACY";

    default boolean isLegacyMode(DynamicConfigService dynamicConfigService) {
        return LEGACY_MODE.equalsIgnoreCase(dynamicConfigService.getAppMode().getMode());
    }
}
