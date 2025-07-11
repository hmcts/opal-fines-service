/**
* CGI OPAL Program
*
* MODULE      : V20250627_326__update_comments_draft_accounts.sql
*
* DESCRIPTION : Update column comments on DRAFT_ACCOUNTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------
* 27/06/2025    TMc         1.0         PO-1568 Update column comments on the DRAFT_ACCOUNTS table
*
**/

COMMENT ON COLUMN draft_accounts.account_status IS 'One of Submitted, Resubmitted, Rejected, Deleted, Approved, Publishing Pending, Published, Publishing Failed';

COMMENT ON COLUMN draft_accounts.account_id IS 'Account ID created on validation. In Opal mode it will hold the defendant_accounts.defendant_account_id value';