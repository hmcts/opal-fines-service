package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum CreditorTransactionType {
    PAYMNT("Payments"),
    XFER("Transfer to suspense"),
    MADJ("Manual Adjustment"),
    REPSUS("Repayment from Suspense"),
    CHEQUE("Cheque"),
    CANCHQ("Cancelled cheque"),
    RICHEQ("Reissued cheque"),
    BACS("BACS payments"),
    RTBACS("Returned BACS"),
    RIBACS("Reissued BACS"),
    REPAYC("Repay Courts Charge"),
    REPAYF("Repay Fines"),
    REPAYM("Repay Maintenance (historic only)"),
    REPAYP("Repay Fixed Penalty (historic only)"),
    REPAYV("Repay victim surcharge"),
    REPAYW("Repay witness expenses"),
    WO611B("Amount allocated to Section 611B write-off"),
    CFEES("Amount allocated to Court Fees"),
    LIFEES("Amount allocated to Licensing Fees"),
    REPLIC("Repay licensing fees (historic only)"),
    PAID("Amount due to be paid"),
    FINES("Amount allocated to Fines"),
    FIXPEN("Amount allocated to Fixed Penalty"),
    FDCOST("Amount allocated to Form D Costs"),
    FO("Amount allocated to Fine Only"),
    FCOST("Amount allocated to Costs"),
    FCPC("Amount allocated to Crown Prosecution Costs"),
    FCOMP("Amount allocated to Compensation"),
    FVS("Amount allocated to Victim Surcharge"),
    FCC("Amount allocated to Fines Court Charge"),
    FEES("Amount allocated to Fees"),
    FCUEX("Amount allocated to Customs and Excise"),
    FNIA("Amount allocated to National Insurance Arrears"),
    FOPR1("Amount allocated to Fine 1st Priority"),
    FLAID("Amount allocated to Legal Aid Contribution"),
    FVEA("Amount allocated to Vehicle Excise Arrears"),
    FVEBD("Amount allocated to Vehicle Excise Back Duty"),
    FWEC("Amount allocated to Witness Expenses"),
    FDCON("Amount allocated to Contribution to Form D Costs"),
    FFR("Amount allocated to Forfeited Recognizance"),
    FCMP("Amount allocated to Compensation from Scotland/NI"),
    FCST("Amount allocated to Costs from Scotland/NI"),
    FINE("Amount allocated to Fines from Scotland/NI");

    private final String displayName;

    CreditorTransactionType(String displayName) {
        this.displayName = displayName;
    }

    @JsonValue
    public String getValue() {
        return name();
    }
}
