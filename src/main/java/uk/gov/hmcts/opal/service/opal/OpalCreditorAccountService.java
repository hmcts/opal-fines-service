package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import uk.gov.hmcts.opal.service.UserStateService;
import uk.gov.hmcts.opal.service.opal.jpa.CreditorAccountTransactions;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.CreditorAccountService")
@Qualifier("creditorAccountService")
public class OpalCreditorAccountService {

    public static final String CREDITOR_ACCOUNT_DELETED_MESSAGE_FORMAT = """
        { "message": "Creditor Account '%s' deleted"}""";

    private final CreditorAccountTransactions creditorAccountTransactions;

    private final UserStateService userStateService;

    public String deleteCreditorAccount(long minorCreditorId, boolean checkExisted, String authHeaderValue) {
        userStateService.checkForAuthorisedUser(authHeaderValue);

        try {
            boolean deleted =  creditorAccountTransactions
                .deleteMinorCreditor(minorCreditorId, creditorAccountTransactions);
            if (deleted) {
                log.debug(":deleteMinorCreditor: Deleted Draft Account: {}", minorCreditorId);
            }
        } catch (UnexpectedRollbackException | EntityNotFoundException ure) {
            if (checkExisted) {
                throw ure;
            }
        }
        return String.format(CREDITOR_ACCOUNT_DELETED_MESSAGE_FORMAT, minorCreditorId);
    }
}
