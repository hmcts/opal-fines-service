package uk.gov.hmcts.opal.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.service.legacy.LegacyDraftAccountReferenceValidationService;
import uk.gov.hmcts.opal.service.opal.DynamicConfigService;
import uk.gov.hmcts.opal.service.opal.OpalDraftAccountReferenceValidationService;

@ExtendWith(MockitoExtension.class)
public class DraftAccountReferenceValidationServiceLocatorTest {

    @Mock
    private OpalDraftAccountReferenceValidationService opalService;

    @Mock
    private LegacyDraftAccountReferenceValidationService legacyService;

    @Mock
    private DynamicConfigService dynamicConfigService;

    @InjectMocks
    private DraftAccountReferenceValidationServiceLocator serviceLocator;

    @Test
    void testGetService_opalMode() {
        // Arrange
        when(dynamicConfigService.isLegacyMode()).thenReturn(false);

        // Act
        DraftAccountReferenceValidationService service = serviceLocator.getService();

        // Assert
        assertThat(service).isSameAs(opalService);
    }

    @Test
    void testGetService_LegacyMode() {
        // Arrange
        when(dynamicConfigService.isLegacyMode()).thenReturn(true);

        // Act
        DraftAccountReferenceValidationService service = serviceLocator.getService();

        // Assert
        assertThat(service).isSameAs(legacyService);
    }
}
