package uk.gov.hmcts.opal.dto.legacy;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.legacy.common.CommentsAndNotes;

class LegacyUpdateDefendantAccountRequestTest {

    @Test
    void isExactlyOneUpdateFieldPresent_returnsTrueForSingleGroup() {
        LegacyUpdateDefendantAccountRequest request = LegacyUpdateDefendantAccountRequest.builder()
            .commentAndNotes(CommentsAndNotes.builder().accountComment("note").build())
            .build();

        assertTrue(request.isExactlyOneUpdateFieldPresent());
    }
}
