/**
* OPAL Program
*
* MODULE      : update_defendant_account_prosecutor_originators.sql
*
* DESCRIPTION : Replace matched Prosecutor surrogate originator identifiers with Prosecutor codes.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 06/11/2026    TT             1.0         PO-11043 - Convert matched Defendant Account Prosecutor originator identifiers.
*
**/

WITH candidates AS (
    SELECT da.defendant_account_id,
           p.prosecutor_code
      FROM defendant_accounts da
      JOIN prosecutors p
        ON p.prosecutor_id::VARCHAR = da.originator_id
       AND p.name = da.originator_name
)
UPDATE defendant_accounts da
   SET originator_id = c.prosecutor_code
  FROM candidates c
 WHERE da.defendant_account_id = c.defendant_account_id;
