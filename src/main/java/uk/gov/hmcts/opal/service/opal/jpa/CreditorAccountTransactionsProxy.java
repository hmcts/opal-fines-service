package uk.gov.hmcts.opal.service.opal.jpa;

import uk.gov.hmcts.opal.entity.creditoraccount.CreditorAccountEntity;

import org.springframework.transaction.annotation.Transactional;

/**
 * This interface is used to avoid CreditorAccountService directly referencing its own JPA proxied methods.
 * Anywhere where CreditorAccountService might need to call one of its own Transaction methods from within another
 * Transactional method, then that can be problematic.  Direct method calls should be avoided, but instead
 * routed through a proxy object that captures all the JPA Transactional 'overhead'.
 */
public interface CreditorAccountTransactionsProxy {
    @Transactional(readOnly = true)
    CreditorAccountEntity.Lite getCreditorAccountById(long creditorAccountId);
}
