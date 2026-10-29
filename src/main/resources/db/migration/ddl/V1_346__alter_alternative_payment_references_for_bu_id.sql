/**
* OPAL Program
*
* MODULE      : alter_alternative_payment_references_for_bu_id.sql
*
* DESCRIPTION : Alter alternate_payment_references, drop business_unit_code, add business_unit_id
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    --------------------------------------------------
* 06/10/2026    T McCallion    1.0         PO-10809 - Alter tables for BU ID
*
**/

ALTER TABLE alternate_payment_references 
    DROP COLUMN business_unit_code,
    ADD COLUMN business_unit_id SMALLINT NOT NULL,
    ADD CONSTRAINT apr_bu_id_fk FOREIGN KEY (business_unit_id) REFERENCES business_units(business_unit_id);

COMMENT ON COLUMN alternate_payment_references.business_unit_id IS 'The business unit at time of creation';

CREATE INDEX apr_bu_id_idx ON alternate_payment_references (business_unit_id);