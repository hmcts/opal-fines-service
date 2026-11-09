/**
* OPAL Program
*
* MODULE      : alter_prosecutors_business_key.sql
*
* DESCRIPTION : Promote the Prosecutor code to the primary key and remove the surrogate identifier.
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 06/11/2026    TT             1.0         PO-11043 - Re-key Prosecutors by numeric Prosecutor code.
*
**/

ALTER TABLE prosecutors
    DROP CONSTRAINT IF EXISTS prosecutors_pk;

ALTER TABLE prosecutors
    ALTER COLUMN prosecutor_code TYPE SMALLINT
        USING prosecutor_code::SMALLINT,
    ALTER COLUMN prosecutor_code SET NOT NULL,
    ADD CONSTRAINT prosecutors_pk PRIMARY KEY (prosecutor_code),
    DROP COLUMN IF EXISTS prosecutor_id;

CREATE INDEX pro_name_idx ON prosecutors USING BTREE (name);
