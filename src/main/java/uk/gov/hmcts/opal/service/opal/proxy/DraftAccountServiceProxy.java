package uk.gov.hmcts.opal.service.opal.proxy;

import uk.gov.hmcts.opal.entity.DraftAccountEntity;

/**
 * This interface is used to avoid DraftAccountService directly referencing its own JPA proxied methods.
 * Anywhere where DraftAccountService might need to call one of its own Transaction methods from within another
 * Transactional method, then that can be problematic.  Direct method calls should be avoided, but instead
 * routed through a proxy object that captures all the JPA Transactional 'overhead'.
 */
public interface DraftAccountServiceProxy {
    DraftAccountEntity getDraftAccount(long draftAccountId);
}
