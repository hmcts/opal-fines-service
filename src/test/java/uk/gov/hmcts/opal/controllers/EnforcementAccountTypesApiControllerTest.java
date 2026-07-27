package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementAccountTypes200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountType200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypeService;

@ExtendWith(MockitoExtension.class)
public class EnforcementAccountTypesApiControllerTest {
    @Mock
    private EnforcementAccountTypeService service;

    @InjectMocks
    private EnforcementAccountTypesApiController controller;

    @Test
    void getEnforcementAccountTypes_Success() {
        List<EnforcementAccountTypeCommon> enfAccountTypes = List.of(
            mock(EnforcementAccountTypeCommon.class)
        );
        when(service.getAllEnforcementAccountTypes()).thenReturn(enfAccountTypes);

        ResponseEntity<GetEnforcementAccountTypes200Response> response = controller.getEnforcementAccountTypes();

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(enfAccountTypes, response.getBody().getEnforcementAccountTypes())
        );
    }

    @Test
    void getEnforcementAccountTypes_ReturnsOkWithEmptyCollection() {
        when(service.getAllEnforcementAccountTypes()).thenReturn(Collections.emptyList());

        ResponseEntity<GetEnforcementAccountTypes200Response> response = controller.getEnforcementAccountTypes();

        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(0, response.getBody().getEnforcementAccountTypes().size())
        );
    }

    @Test
    void updateEnforcementAccountTypes_Success() {
        List<EnforcementAccountTypeCommon> enfAccountTypes = List.of(
            mock(EnforcementAccountTypeCommon.class)
        );
        List<PatchEnforcementAccountTypeRequestInner> request = List.of(
            mock(PatchEnforcementAccountTypeRequestInner.class)
        );

        when(service.updateEnforcementAccountType(eq(request))).thenReturn(enfAccountTypes);

        ResponseEntity<PatchEnforcementAccountType200Response> response = controller
            .patchEnforcementAccountType(request);

        verify(service).updateEnforcementAccountType(eq(request));
        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(enfAccountTypes, response.getBody().getEnforcementAccountTypes())
        );
    }
}