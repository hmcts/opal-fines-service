package uk.gov.hmcts.opal.service.opal.jpa;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.entity.imposition.ImpositionEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.CreditorTransactionSpecs;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class CreditorAccountTransactional implements CreditorAccountTransactionalProxy {


    private final CreditorAccountRepository creditorAccountRepository;

    private final PartyRepository partyRepository;

    private final CreditorTransactionRepository creditorTransactionRepository;

    private final ImpositionRepository impositionRepository;

    @Override
    public CreditorAccountEntity.Lite getCreditorAccountById(long minorCreditorId) {
        return creditorAccountRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException("Creditor Account not found with id: " + minorCreditorId));
    }

    @Transactional
    public boolean deleteAllByDefendantAccountId(long defendantAccountId, CreditorAccountTransactionalProxy proxy) {
        List<ImpositionEntity.Lite> impositions = impositionRepository.findAllByDefendantAccountId(defendantAccountId);
        // First delete all the minor creditors and associated data
        impositions.stream().map(ImpositionEntity::getCreditorAccountId)
            .peek(creditAccId -> log.debug(":deleteAllByDefendantAccountId: creditor account id: {}", creditAccId))
            .forEach(creditAccId -> proxy.deleteMinorCreditorAccountAndRelatedData(
                creditAccId, proxy));
        // Now delete all non-minor impositions for the defendant account.
        impositions = impositionRepository.findAllByDefendantAccountId(defendantAccountId);
        impositionRepository.deleteAll(impositions);
        return true;
    }

    @Transactional
    @Override
    public boolean deleteMinorCreditorAccountAndRelatedData(long creditorAccId,
                                                            CreditorAccountTransactionalProxy proxy) {

        // Delete from the various repositories
        CreditorAccountEntity.Lite creditorAcc = proxy.getCreditorAccountById(creditorAccId);
        if (creditorAcc.getCreditorAccountType().isMinorCreditor()) {
            log.debug(":deleteMinorCreditorAccountAndRelatedData: minor creditor account id: {}", creditorAccId);
            Long deleted = creditorTransactionRepository
                .delete(CreditorTransactionSpecs.equalsCreditorAccountId(creditorAccId));
            log.debug(
                ":deleteCreditorAccountAndRelatedData:  deleted: {} creditor transactions for credit account id: {}",
                deleted, creditorAccId
            );

            impositionRepository.delete(ImpositionSpecs.equalsCreditorAccountId(creditorAccId));
            creditorAccountRepository.delete(creditorAcc);

            // A creditor account for a minor creditor will have an associated party that only exists within
            // the context of that creditor account - so needs to be deleted as well.
            Optional<Long> partyId = Optional.ofNullable(creditorAcc.getMinorCreditorPartyId());
            partyId.flatMap(partyRepository::findById).ifPresent(partyEntity -> {
                try {
                    partyRepository.delete(partyEntity);
                } catch (DataIntegrityViolationException ex) {
                    log.debug(":deleteMinorCreditorAccountAndRelatedData: party {} still referenced, skipping delete",
                        partyEntity.getPartyId());
                }
            });
        }

        return true;
    }

}
