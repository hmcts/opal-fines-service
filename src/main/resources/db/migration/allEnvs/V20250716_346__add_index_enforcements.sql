/**
* CGI OPAL Program
*
* MODULE      : add_index_enforcements.sql
*
* DESCRIPTION : Add index to the ENFORCEMENTS table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------
* 16/07/2025    TMc         1.0         PO-1561 Add index enf_case_reference_idx to the ENFORCEMENTS table
*
**/
CREATE INDEX enf_case_reference_idx ON enforcements(case_reference);