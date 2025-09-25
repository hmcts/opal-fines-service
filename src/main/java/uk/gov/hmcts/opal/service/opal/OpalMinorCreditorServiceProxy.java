package uk.gov.hmcts.opal.service.opal;

import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;

import org.springframework.transaction.annotation.Transactional;

/**
 * This interface is used to avoid OpalMinorCreditorService directly referencing its own JPA proxied methods.
 * Anywhere where OpalMinorCreditorService might need to call one of its own Transaction methods from within another
 * Transactional method, then that can be problematic.  Direct method calls should be avoided, but instead
 * routed through a proxy object that captures all the JPA Transactional 'overhead'.
 */
public interface OpalMinorCreditorServiceProxy {
    @Transactional(readOnly = true)
    MinorCreditorEntity getMinorCreditorById(long minorCreditorId);
}
