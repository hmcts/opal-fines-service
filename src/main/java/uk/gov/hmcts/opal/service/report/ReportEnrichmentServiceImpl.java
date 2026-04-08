package uk.gov.hmcts.opal.service.report;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.entity.DebtorDetailEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountEntity;
import uk.gov.hmcts.opal.entity.defendantaccount.DefendantAccountPartiesEntity;
import uk.gov.hmcts.opal.entity.enforcement.EnforcementEntity;
import uk.gov.hmcts.opal.repository.DebtorDetailRepository;
import uk.gov.hmcts.opal.repository.EnforcementRepository;

@Service
@RequiredArgsConstructor
public class ReportEnrichmentServiceImpl implements ReportEnrichmentService {

    private final DebtorDetailRepository debtorDetailRepository;
    private final EnforcementRepository enforcementRepository;

    private final Map<Long, DebtorDetailEntity> debtorByParty = new HashMap<>();
    private final Map<Long, EnforcementEntity.Lite> latestEnfByAccount = new HashMap<>();

    @Override
    public void prefetchForAccounts(List<DefendantAccountEntity> accounts) {
        debtorByParty.clear();
        latestEnfByAccount.clear();

        if (accounts == null || accounts.isEmpty()) {
            return;
        }

        Set<Long> partyIds = new HashSet<>();
        Set<Long> accountIds = new HashSet<>();

        for (DefendantAccountEntity a : accounts) {
            if (a == null) {
                continue;
            }
            accountIds.add(a.getDefendantAccountId());
            if (a.getParties() != null) {
                for (DefendantAccountPartiesEntity link : a.getParties()) {
                    if (link != null && link.getParty() != null && link.getParty().getPartyId() != null) {
                        partyIds.add(link.getParty().getPartyId());
                    }
                }
            }
        }

        if (!partyIds.isEmpty()) {
            List<DebtorDetailEntity> debtors = debtorDetailRepository.findByPartyIdIn(partyIds);
            for (DebtorDetailEntity d : debtors) {
                debtorByParty.put(d.getPartyId(), d);
            }
        }

        if (!accountIds.isEmpty()) {
            List<EnforcementEntity.Lite> allEnfs = enforcementRepository.findByDefendantAccountIdIn(accountIds);
            Map<Long, EnforcementEntity.Lite> latestMap = new HashMap<>();
            for (EnforcementEntity.Lite e : allEnfs) {
                Long aid = e.getDefendantAccountId();
                if (aid == null) {
                    continue;
                }
                EnforcementEntity.Lite existing = latestMap.get(aid);
                if (existing == null || (e.getPostedDate() != null
                    && e.getPostedDate().isAfter(existing.getPostedDate()))) {
                    latestMap.put(aid, e);
                }
            }
            latestEnfByAccount.putAll(latestMap);
        }
    }

    @Override
    public Optional<DebtorDetailEntity> getDebtorForParty(Long partyId) {
        return Optional.ofNullable(debtorByParty.get(partyId));
    }

    @Override
    public Optional<EnforcementEntity.Lite> getLatestEnforcementForAccount(Long accountId) {
        return Optional.ofNullable(latestEnfByAccount.get(accountId));
    }
}