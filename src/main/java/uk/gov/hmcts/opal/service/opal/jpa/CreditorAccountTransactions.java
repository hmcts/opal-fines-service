package uk.gov.hmcts.opal.service.opal.jpa;

import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.opal.entity.PartyEntity;
import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;
import uk.gov.hmcts.opal.repository.CreditorAccountRepository;
import uk.gov.hmcts.opal.repository.CreditorTransactionRepository;
import uk.gov.hmcts.opal.repository.ImpositionRepository;
import uk.gov.hmcts.opal.repository.PartyRepository;
import uk.gov.hmcts.opal.repository.jpa.CreditorTransactionSpecs;
import uk.gov.hmcts.opal.repository.jpa.ImpositionSpecs;

@Service
@Slf4j(topic = "opal.OpalMinorCreditorService")
@RequiredArgsConstructor
public class CreditorAccountTransactions implements CreditorAccountTransactionsProxy {


    private final CreditorAccountRepository creditorAccountRepository;

    private final PartyRepository partyRepository;

    private final CreditorTransactionRepository creditorTransactionRepository;

    private final ImpositionRepository impositionRepository;

    public CreditorAccountEntity.Lite getCreditorAccountById(long minorCreditorId) {
        return creditorAccountRepository.findById(minorCreditorId)
            .orElseThrow(() -> new EntityNotFoundException("Creditor Account not found with id: " + minorCreditorId));
    }

    @Transactional
    public boolean deleteMinorCreditor(long creditorAccId, CreditorAccountTransactionsProxy proxy) {
        // Delete from the various repositories
        Long deleted = creditorTransactionRepository
            .delete(CreditorTransactionSpecs.equalsCreditorAccountId(creditorAccId));
        log.debug(":deleteMinorCreditor:  deleted: {} creditor transactions for credit account id: {}",
                  deleted, creditorAccId);

        deleted = impositionRepository.delete(ImpositionSpecs.equalsCreditorAccountId(creditorAccId));
        log.debug(":deleteMinorCreditor:  deleted: {} impositions for credit account id: {}",
                  deleted, creditorAccId);

        CreditorAccountEntity.Lite creditorAcc = proxy.getCreditorAccountById(creditorAccId);
        creditorAccountRepository.delete(creditorAcc);

        Optional<Long> partyId = Optional.ofNullable(creditorAcc.getMinorCreditorPartyId());
        Optional<PartyEntity> partyEntity = partyId.flatMap(partyRepository::findById);
        partyEntity.ifPresent(partyRepository::delete);

        return true;
    }

}
