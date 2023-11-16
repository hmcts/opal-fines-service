package uk.gov.hmcts.opal.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.dto.AppMode;
import uk.gov.hmcts.opal.service.DynamicConfigService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {TestingSupportController.class, DynamicConfigService.class})
class TestingSupportControllerTest {

    @Autowired
    private TestingSupportController controller;

    @MockBean
    private DynamicConfigService configService;

    @Test
    void getAppMode() {
        AppMode mode = AppMode.builder().mode("opal").build();
        when(configService.getAppMode()).thenReturn(mode);

        ResponseEntity<AppMode> response = controller.getAppMode();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("opal", response.getBody().getMode());
    }

    @Test
    void updateMode() {
        AppMode mode = AppMode.builder().mode("legacy").build();
        when(configService.updateAppMode(any())).thenReturn(mode);

        ResponseEntity<AppMode> response = controller.updateMode(mode);

        assertEquals(HttpStatus.ACCEPTED, response.getStatusCode());
        assertEquals("legacy", response.getBody().getMode());

    }

}
