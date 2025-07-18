/**
* CGI OPAL Program
*
* MODULE      : add_index_aliases.sql
*
* DESCRIPTION : Add indexes to the IMPOSITIONS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    --------------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Add indexes imp_caid_idx and imp_daid_idx to the IMPOSITIONS table
*
**/
CREATE INDEX imp_caid_idx ON impositions (creditor_account_id);

CREATE INDEX imp_daid_idx ON impositions (defendant_account_id);