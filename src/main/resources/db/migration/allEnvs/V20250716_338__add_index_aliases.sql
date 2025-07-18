/**
* CGI OPAL Program
*
* MODULE      : add_index_aliases.sql
*
* DESCRIPTION : Add index to the ALIASES table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    -----------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1586 Add index aliases_party_id_idx to the ALIASES table
*
**/
CREATE INDEX aliases_party_id_idx ON aliases (party_id);