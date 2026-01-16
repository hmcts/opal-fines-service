package uk.gov.hmcts.opal.service.iface;

public interface DocumentServiceInterface {
    /**
     * Create a document instance record for a payment terms change letter.
     */
    public void createDocumentInstance(Long defAccountId, short businessUnitId);
}
