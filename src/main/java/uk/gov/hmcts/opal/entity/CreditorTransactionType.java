package uk.gov.hmcts.opal.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CreditorTransactionType {
    PAYMNT,
    XFER,
    MADJ,
    REPSUS,
    CHEQUE,
    CANCHQ,
    RICHEQ,
    BACS,
    RTBACS,
    RIBACS,
    REPAYC,
    REPAYF,
    REPAYM,
    REPAYP,
    REPAYV,
    REPAYW,
    WO611B,
    CFEES,
    LIFEES,
    REPLIC,
    PAID,
    FINES,
    FIXPEN,
    FDCOST,
    FO,
    FCOST,
    FCPC,
    FCOMP,
    FVS,
    FCC,
    FEES,
    FCUEX,
    FNIA,
    FOPR1,
    FLAID,
    FVEA,
    FVEBD,
    FWEC,
    FDCON,
    FFR,
    FCMP,
    FCST,
    FINE;

    @JsonValue
    public String getValue() {
        return name();
    }
}
