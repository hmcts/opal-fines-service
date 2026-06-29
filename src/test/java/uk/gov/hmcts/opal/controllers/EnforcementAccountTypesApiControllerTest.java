package uk.gov.hmcts.opal.controllers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountType200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypesService;

@ExtendWith(MockitoExtension.class)
public class EnforcementAccountTypesApiControllerTest {
    @Mock
    private EnforcementAccountTypesService service;

    @InjectMocks
    private EnforcementAccountTypesApiController controller;

    @Test
    void updateEnforcementAccountTypes_Success() {
        List<EnforcementAccountTypeCommon> enfAccountTypes = List.of(
            mock(EnforcementAccountTypeCommon.class)
        );
        when(service.updateEnforcementAccountType(any())).thenReturn(enfAccountTypes);

        ResponseEntity<PatchEnforcementAccountType200Response> response = controller
            .patchEnforcementAccountType(List.of(mock(PatchEnforcementAccountTypeRequestInner.class)));

        verify(service).updateEnforcementAccountType(any());
        assertAll(
            () -> assertEquals(HttpStatus.OK, response.getStatusCode()),
            () -> assertEquals(enfAccountTypes, response.getBody().getEnforcementAccountTypes())
        );
    }
}