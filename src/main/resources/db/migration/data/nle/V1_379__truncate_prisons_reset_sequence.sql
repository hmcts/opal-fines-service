/**
* OPAL Program
*
* MODULE      : truncate_prisons_reset_sequence.sql
*
* DESCRIPTION : Prepare Prisons table for seeding from CSDS
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------------------
* 10/11/2026    T McCallion    1.0         PO-11046 - Amend PRISONS table for Ref Data 2
*
**/

TRUNCATE TABLE prisons;

ALTER SEQUENCE prison_id_seq
    START 1
    MINVALUE 1
    RESTART WITH 1
    NO CYCLE
    CACHE 1;