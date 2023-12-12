package uk.gov.hmcts.opal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DefendantAccountRepository extends JpaRepository<DefendantAccountEntity, Long> {

    DefendantAccountEntity findByBusinessUnitIdAndAccountNumber(Short businessUnitId, String accountNumber);

    List<DefendantAccountEntity> findAllByBusinessUnitId(Short businessUnitId);

}


