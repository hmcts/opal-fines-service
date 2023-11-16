package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.AppMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicConfigServiceTest {
    @Test
    void shouldReturnOpal_whenInvokedWithDefaultValue() {
        DynamicConfigService configService = new DynamicConfigService("legacy");
        AppMode mode = configService.getAppMode();
        assertEquals("legacy", mode.getMode());
    }

    @Test
    void shouldUpdateAppMode_whenProvidedNewValue() {
        DynamicConfigService configService = new DynamicConfigService("opal");

        AppMode newMode = AppMode.builder().mode("legacy").build();
        configService.updateAppMode(newMode);

        AppMode updated = configService.getAppMode();
        assertEquals("legacy", updated.getMode());
    }
}
