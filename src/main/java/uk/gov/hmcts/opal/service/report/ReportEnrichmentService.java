package uk.gov.hmcts.opal.service.report;

import uk.gov.hmcts.opal.entity.debtordetail.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;

import java.util.List;
import java.util.Optional;

public interface ReportEnrichmentService {

    void prefetchForAccounts(List<DefendantAccountEntity> accounts);

    Optional<DebtorDetailEntity> getDebtorForParty(Long partyId);

    Optional<EnforcementEntity.Lite> getLatestEnforcementForAccount(Long accountId);
}