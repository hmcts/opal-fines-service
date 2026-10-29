/**
* OPAL Program
*
* MODULE      : populate_r1a_r1b_legacy_ref_tables.sql
*
* DESCRIPTION : Populate R1A/R1B _legacy reference tables from their original tables.
*               COURTS              --> COURTS_LEGACY
*               ENFORCERS           --> ENFORCERS_LEGACY
*               LOCAL_JUSTICE_AREAS --> LOCAL_JUSTICE_AREAS_LEGACY
*               MAJOR_CREDITORS     --> MAJOR_CREDITORS_LEGACY
*               OFFENCES            --> OFFENCES_LEGACY
*               PRISONS             --> PRISONS_LEGACY
*               PROSECUTORS         --> PROSECUTORS_LEGACY
*               RESULTS             --> RESULTS_LEGACY
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ---------------------------------------------------
* 19/10/2026    T McCallion    1.0         PO-10770 - Create _legacy R1A reference data tables
*
**/

INSERT INTO courts_legacy 
    SELECT * FROM courts;

SELECT setval('legacy_court_id_seq', greatest(start_value, last_value), CASE WHEN LAST_VALUE IS NULL THEN FALSE ELSE TRUE END)
  FROM pg_sequences
 WHERE sequencename = 'court_id_seq';
 

INSERT INTO enforcers_legacy
    SELECT * FROM enforcers;

SELECT setval('legacy_enforcer_id_seq', greatest(start_value, last_value), CASE WHEN LAST_VALUE IS NULL THEN FALSE ELSE TRUE END)
  FROM pg_sequences
 WHERE sequencename = 'enforcer_id_seq';
 

INSERT INTO local_justice_areas_legacy
    SELECT * FROM local_justice_areas;


INSERT INTO major_creditors_legacy
    SELECT * FROM major_creditors;

SELECT setval('legacy_major_creditor_id_seq', greatest(start_value, last_value), CASE WHEN LAST_VALUE IS NULL THEN FALSE ELSE TRUE END)
  FROM pg_sequences
 WHERE sequencename = 'major_creditor_id_seq';
 

INSERT INTO offences_legacy
    SELECT * FROM offences;


INSERT INTO prisons_legacy
    SELECT * FROM prisons;

SELECT setval('legacy_prison_id_seq', greatest(start_value, last_value), CASE WHEN LAST_VALUE IS NULL THEN FALSE ELSE TRUE END)
  FROM pg_sequences
 WHERE sequencename = 'prison_id_seq';
 
 
INSERT INTO prosecutors_legacy
    SELECT * FROM prosecutors;

INSERT INTO results_legacy
    SELECT * FROM results;