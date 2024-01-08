package uk.gov.hmcts.opal.repository.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.opal.dto.AccountSearchDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DefendntAccountSpecsTest {

    @Autowired private EntityManager entityManager;

    @Test
    public void codeCoverageTest() {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<?> query = builder.createQuery(DefendantAccountEntity.class);
        Root<DefendantAccountEntity> root = query.from(DefendantAccountEntity.class);

        assertNotNull(DefendantAccountSpecs.equalsImposingCourtId(1L).toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.equalsAccountNumber("test").toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.equalsDateOfBirth(LocalDate.now()).toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.likeAddressLine1("test").toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.likeForename("test").toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.likeNiNumber("test").toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.likeSurname("test").toPredicate(root, query, builder));
        assertNotNull(DefendantAccountSpecs.likeOrganisationName("test").toPredicate(root, query, builder));

        assertNotNull(DefendantAccountSpecs.findByAccountSearch(AccountSearchDto.builder().build()));

    }

}
