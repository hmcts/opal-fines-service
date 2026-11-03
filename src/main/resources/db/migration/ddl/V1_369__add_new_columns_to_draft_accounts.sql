/**
* OPAL Program
*
* MODULE      : add_new_columns_to_draft_accounts.sql
*
* DESCRIPTION : Fines - Add EI5 metadata to draft_accounts table
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 05/10/2026    TT         1.0         PO-10392 - create draft account source enum, add columns and index to draft_accounts table
**/

CREATE TYPE t_dra_source_enum AS ENUM ('MAC', 'CPP', 'VPFPO');

ALTER TABLE draft_accounts
    ADD COLUMN source t_dra_source_enum NOT NULL DEFAULT 'MAC',
    ADD COLUMN source_reference BIGINT;

COMMENT ON COLUMN draft_accounts.source IS
    'Identifies where the draft account originated from';

COMMENT ON COLUMN draft_accounts.source_reference IS
    'For VPFPO, stores File Handler vpfpo_registrations.vpfpo_registration_id; otherwise null';

CREATE INDEX dra_source_idx
    ON draft_accounts (source);
