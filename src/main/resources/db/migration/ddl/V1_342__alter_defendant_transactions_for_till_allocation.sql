/**
* OPAL Program
*
* MODULE      : alter_defendant_transactions_for_till_allocation.sql
*
* DESCRIPTION : Add payment source and widen payment reference for till allocation.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 12/10/2026    C Cho          1.0         PO-3394 - Alter DEFENDANT_TRANSACTIONS for till allocation
*
**/

ALTER TABLE public.defendant_transactions
    ADD COLUMN source VARCHAR(30),
    ALTER COLUMN payment_reference TYPE VARCHAR(50);

COMMENT ON COLUMN public.defendant_transactions.source IS 'The party that sent the associated payment.';
