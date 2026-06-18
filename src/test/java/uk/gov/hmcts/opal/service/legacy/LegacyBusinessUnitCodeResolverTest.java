package uk.gov.hmcts.opal.service.legacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.opal.entity.businessunit.BusinessUnitEntity;
import uk.gov.hmcts.opal.repository.BusinessUnitRepository;

@ExtendWith(MockitoExtension.class)
class LegacyBusinessUnitCodeResolverTest {

    @Mock
    private BusinessUnitRepository businessUnitRepository;

    @Test
    void resolve_whenLegacyCodeIsPresentAndDifferentFromId_returnsLegacyCode() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);

        String result = resolver.resolve("78", "NE");

        assertEquals("NE", result);
        verifyNoInteractions(businessUnitRepository);
    }

    @Test
    void resolve_whenLegacyCodeEchoesId_returnsCodeFromBusinessUnit() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);
        when(businessUnitRepository.findById((short) 78)).thenReturn(
            Optional.of(BusinessUnitEntity.builder().businessUnitCode("NE").build())
        );

        String result = resolver.resolve("78", "78");

        assertEquals("NE", result);
    }

    @Test
    void resolve_whenLegacyCodeIsMissing_returnsCodeFromBusinessUnit() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);
        when(businessUnitRepository.findById((short) 77)).thenReturn(
            Optional.of(BusinessUnitEntity.builder().businessUnitCode("0046").build())
        );

        String result = resolver.resolve("77", null);

        assertEquals("0046", result);
    }

    @Test
    void resolve_whenBusinessUnitIdIsNotNumeric_returnsLegacyCode() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);

        String result = resolver.resolve("ABC", "ABC");

        assertEquals("ABC", result);
        verifyNoInteractions(businessUnitRepository);
    }

    @Test
    void resolve_whenBusinessUnitIsNotFound_returnsLegacyCode() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);
        when(businessUnitRepository.findById((short) 78)).thenReturn(Optional.empty());

        String result = resolver.resolve("78", "78");

        assertEquals("78", result);
    }

    @Test
    void resolve_whenBusinessUnitIdAndLegacyCodeAreMissing_returnsNull() {
        LegacyBusinessUnitCodeResolver resolver = new LegacyBusinessUnitCodeResolver(businessUnitRepository);

        String result = resolver.resolve(null, null);

        assertNull(result);
        verifyNoInteractions(businessUnitRepository);
    }
}
