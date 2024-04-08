/**
* CGI OPAL Program
*
* MODULE      : correct_id_names.sql
*
* DESCRIPTION : Typo - Rename the columns impositions.impositions_id to impositions.imposition_id and creditor_transactions_id to creditor_transaction_id for the Fines model
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ------------------------------------------------------------------------------------------------------------------------------------------------------------
* 08/04/2024    A Dennis    1.0         Typo - Rename the columns impositions.impositions_id to impositions.imposition_id and creditor_transactions_id to creditor_transaction_id for the Fines model
*
**/

ALTER TABLE IF EXISTS impositions
RENAME COLUMN impositions_id TO imposition_id;

ALTER TABLE IF EXISTS creditor_transactions
RENAME COLUMN creditor_transactions_id TO creditor_transaction_id;