/**
* OPAL Program
*
* MODULE      : alter_payments_in_receipt_allocated_not_null.sql
*
* DESCRIPTION : Amend PAYMENTS_IN.RECEIPT and PAYMENTS_IN.ALLOCATED to be NOT NULL.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -----------------------------------------------------------------------------------------------------------------
* 12/06/2026    C Cho       1.0         PO-1018 Amend PAYMENTS_IN.RECEIPT and PAYMENTS_IN.ALLOCATED to be NOT NULL.
*
**/

UPDATE payments_in
   SET receipt = FALSE
 WHERE receipt IS NULL;

UPDATE payments_in
   SET allocated = FALSE
 WHERE allocated IS NULL;

ALTER TABLE public.payments_in
    ALTER COLUMN receipt SET NOT NULL,
    ALTER COLUMN allocated SET NOT NULL;
