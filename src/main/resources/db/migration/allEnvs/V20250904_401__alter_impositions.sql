/**
* CGI OPAL Program
*
* MODULE      : alter_impositions.sql
*
* DESCRIPTION : Add columns ORIGINATOR_NAME and ORIGINAL_IMPOSITION_ID,
*               including FK to IMPOSITIONS.IMPOSITION_ID, to the IMPOSITIONS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------
* 27/08/2025    TMc         1.0         PO-2095 - Add columns ORIGINATOR_NAME and ORIGINAL_IMPOSITION_ID, 
*                                                 including FK to IMPOSITIONS.IMPOSITION_ID, to the IMPOSITIONS table.
*
**/
-- Add new columns to IMPOSITIONS table
ALTER TABLE impositions 
    ADD COLUMN originator_name          VARCHAR(50),
    ADD COLUMN original_imposition_id   BIGINT;

-- Add comments to new columns
COMMENT ON COLUMN impositions.originator_name IS 'The name of the court or system where the account came from. This will allow a 1:1 relationship between court/area (originator_name) and each imposition.';
COMMENT ON COLUMN impositions.original_imposition_id IS 'Populated when an account becomes a Master account (after consolidation), stores the original imposition ID from the child account(s) when it is recreated on the master account (FK to the original imposition).';

ALTER TABLE impositions
    ADD CONSTRAINT imp_original_imposition_id_fk FOREIGN KEY (original_imposition_id) REFERENCES impositions(imposition_id);