/**
* OPAL Program
*
* MODULE      : delete_from_courts_entity_graph.sql
*
* DESCRIPTION : Deletes Courts, Business Units and Local Justice Areas for Court entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 23/04/2026    S WILLIAMS   1.0         PO-2885: Delete rows for court entity graph integration tests
*
**/

DELETE FROM courts WHERE court_id IN (951002, 951001);
DELETE FROM local_justice_areas WHERE local_justice_area_id = 951;
DELETE FROM business_units WHERE business_unit_id = 951;
