/**
* OPAL Program
*
* MODULE      : alter_suspense_transactions_text.sql
*
* DESCRIPTION : Change suspense_transactions.text from VARCHAR(50) to TEXT to accommodate Additional Information.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------
* 07/10/2026    C Cho       1.0         PO-10928 - Increase suspense transaction text capacity for Additional Information
*
**/
ALTER TABLE suspense_transactions
    ALTER COLUMN text TYPE TEXT;
