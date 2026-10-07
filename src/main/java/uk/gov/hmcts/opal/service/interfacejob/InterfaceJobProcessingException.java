package uk.gov.hmcts.opal.service.interfacejob;

public class InterfaceJobProcessingException extends IllegalStateException {

    public InterfaceJobProcessingException(Long interfaceJobId, Throwable cause) {
        super("Failed to process interface job " + interfaceJobId, cause);
    }
}
