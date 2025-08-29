/**
* CGI OPAL Program
*
* MODULE      : alter_payment_terms_add_reason_for_extension.sql
*
* DESCRIPTION : Add a reason_for_extension column to the payment_terms table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------------------------------------
* 26/08/2025    C Larkin    1.0         PO-1853 - Add a reason_for_extension column to the payment_terms table
*
**/
ALTER TABLE payment_terms 
    ADD COLUMN reason_for_extension TEXT;

COMMENT ON COLUMN payment_terms.reason_for_extension IS 'User entered value when extending payment terms';