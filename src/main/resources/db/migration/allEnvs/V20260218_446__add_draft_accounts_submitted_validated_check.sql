/**
* OPAL Program
*
* MODULE      : add_draft_accounts_submitted_validated_check.sql
*
* DESCRIPTION : Add check constraint to ensure submitted_by and validated_by are different when both are present.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------
* 17/02/2026    C Cho       1.0         PO-2806 Add check constraint to DRAFT_ACCOUNTS preventing submitted_by and validated_by being equal.
*
**/

ALTER TABLE draft_accounts
    ADD CONSTRAINT dra_submitted_validated_different
    CHECK (submitted_by IS NULL OR validated_by IS NULL OR submitted_by <> validated_by);
