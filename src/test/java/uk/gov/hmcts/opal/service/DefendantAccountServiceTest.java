package uk.gov.hmcts.opal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.controllers.util.UserStateUtil;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.service.opal.UserStateService;
import uk.gov.hmcts.opal.service.proxy.DefendantAccountServiceProxy;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefendantAccountServiceTest {

    @Mock
    private DefendantAccountServiceProxy defendantAccountServiceProxy;

    @Mock
    private UserStateService userStateService;

    @InjectMocks
    private DefendantAccountService defendantAccountService;

    @Test
    void testGetHeaderSummary() {
        // Arrange
        DefendantAccountHeaderSummary headerSummary = DefendantAccountHeaderSummary.builder().build();

        when(defendantAccountServiceProxy.getHeaderSummary(anyLong())).thenReturn(headerSummary);

        when(userStateService.checkForAuthorisedUser(any())).thenReturn(UserStateUtil.allPermissionsUser());
        // Act
        DefendantAccountHeaderSummary result = defendantAccountService.getHeaderSummary(1L, "authHeaderValue");

        // Assert
        assertNotNull(result);
    }

}
