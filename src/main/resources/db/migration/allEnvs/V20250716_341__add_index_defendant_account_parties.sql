/**
* CGI OPAL Program
*
* MODULE      : add_index_aliases.sql
*
* DESCRIPTION : Add indexes to the DEFENDANT_ACCOUNT_PARTIES table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Add indexes dap_party_id_idx and dap_daid_at_idx to the DEFENDANT_ACCOUNT_PARTIES table
*
**/
CREATE INDEX dap_party_id_idx ON defendant_account_parties (party_id);

CREATE INDEX dap_daid_at_idx  ON defendant_account_parties (defendant_account_id, association_type);