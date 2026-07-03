package uk.gov.hmcts.opal.service.proxy;

import uk.gov.hmcts.opal.service.opal.DynamicConfigService;

/** A marker interface to help identify code that is to be removed at a future date. */
public interface ProxyInterface {

    default boolean isLegacyMode(DynamicConfigService dynamicConfigService) {
        return dynamicConfigService.isLegacyMode();
    }
}
