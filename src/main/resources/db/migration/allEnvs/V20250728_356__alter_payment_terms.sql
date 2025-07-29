/**
* CGI OPAL Program
*
* MODULE      : alter_payment_terms.sql
*
* DESCRIPTION : Add column ACTIVE to the PAYMENT_TERMS table and create unique index pt_def_acc_id_active_udx
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 25/07/2025    TMc         1.0         PO-1101 - Add column ACTIVE to the PAYMENT_TERMS table and create unique index pt_def_acc_id_active_udx.
*
**/
ALTER TABLE payment_terms 
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN payment_terms.active IS 'Indicates the active payment term for the defendant account';

CREATE UNIQUE INDEX pt_def_acc_id_active_udx ON payment_terms (defendant_account_id) 
    WHERE active = TRUE;