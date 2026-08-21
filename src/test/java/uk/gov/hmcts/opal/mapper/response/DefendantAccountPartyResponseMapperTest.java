package uk.gov.hmcts.opal.mapper.response;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.opal.dto.common.LanguagePreference;
import uk.gov.hmcts.opal.generated.model.LanguagePreferenceCommonStrict;
import uk.gov.hmcts.opal.mapper.AbstractMapperTest;

class DefendantAccountPartyResponseMapperTest extends AbstractMapperTest {

    @Autowired
    private DefendantAccountPartyResponseMapper mapper;

    @Test
    void whenLanguagePreferenceHasNoCode_mapsExplicitNullValues_happyPath() {
        LanguagePreferenceCommonStrict result = mapper.toGeneratedResponse(LanguagePreference.fromCode(null));

        assertAll(
            () -> assertNull(result.getLanguageCode()),
            () -> assertNull(result.getLanguageDisplayName())
        );
    }
}
