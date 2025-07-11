/**
* CGI OPAL Program
*
* MODULE      : update_posted_by_name_comments.sql
*
* DESCRIPTION : Update comments on POSTED_BY_NAME columns
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -------------------------------------------------------------------------------------------------------------
* 07/07/2025    C Cho       1.0         PO-1925 Update comments on POSTED_BY_NAME columns
*
**/

--defendant_transactions
COMMENT ON COLUMN defendant_transactions.posted_by_name IS 'The name of the user that posted the transaction';

--payment_terms
COMMENT ON COLUMN payment_terms.posted_by_name IS 'The name of the user that posted the payment term';

--enforcements
COMMENT ON COLUMN enforcements.posted_by_name IS 'The name of the user that posted the enforcement';

--creditor_transactions
COMMENT ON COLUMN creditor_transactions.posted_by_name IS 'The name of the user that posted the transaction';

--impositions
COMMENT ON COLUMN impositions.posted_by_name IS 'The name of the user that posted the imposition';

--notes
COMMENT ON COLUMN notes.posted_by_name IS 'The name of the user that posted the note';

--suspense_transactions
COMMENT ON COLUMN suspense_transactions.posted_by_name IS 'The name of the user that posted the transaction';