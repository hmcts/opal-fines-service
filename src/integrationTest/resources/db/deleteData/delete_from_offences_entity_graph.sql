/**
* OPAL Program
*
* MODULE      : delete_from_offences_entity_graph.sql
*
* DESCRIPTION : Deletes Business Unit and Offence rows for Offence entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 21/04/2026    Codex        1.0         Delete rows for Offence entity graph integration tests
*
**/

DELETE FROM offences WHERE offence_id = 990001;
DELETE FROM business_units WHERE business_unit_id = 951;
