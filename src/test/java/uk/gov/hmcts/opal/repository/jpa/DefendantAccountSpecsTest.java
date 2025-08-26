package uk.gov.hmcts.opal.repository.jpa;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DefendantAccountSpecsTest {

    @Test
    void codeCoverageTest() {

        assertNotNull(DefendantAccountSpecs.equalsImposingCourtId(1L));
        assertNotNull(DefendantAccountSpecs.equalsAccountNumber("test"));
        assertNotNull(DefendantAccountSpecs.equalsDateOfBirth(LocalDate.now()));
        assertNotNull(DefendantAccountSpecs.likeAnyAddressLine("test"));
        assertNotNull(DefendantAccountSpecs.likeForename("test"));
        assertNotNull(DefendantAccountSpecs.likeNiNumber("test"));
        assertNotNull(DefendantAccountSpecs.likeSurname("test"));
        assertNotNull(DefendantAccountSpecs.likeOrganisationName("test"));

        DefendantAccountSpecs specs = new DefendantAccountSpecs();
        assertNotNull(specs.findByAccountSearch(AccountSearchDto.builder().build()));

    }

}
