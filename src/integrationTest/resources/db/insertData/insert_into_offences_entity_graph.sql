/**
* OPAL Program
*
* MODULE      : insert_into_offences_entity_graph.sql
*
* DESCRIPTION : Inserts Business Unit and Offence rows for Offence entity graph integration tests
*
* VERSION HISTORY:
*
* Date          Author       Version     Nature of Change
* ----------    --------     --------    ----------------------------------------------------------------
* 21/04/2026    Codex        1.0         Add rows for Offence entity graph integration tests
*
**/

INSERT INTO business_units
(
business_unit_id,business_unit_name,business_unit_code,business_unit_type,
account_number_prefix,parent_business_unit_id,opal_domain,welsh_language
)
VALUES
(951,'Offence Graph Business Unit','OGBU','Area','OG',NULL,'Fines',false);

INSERT INTO offences
(
offence_id,business_unit_id,cjs_code,offence_title,offence_title_cy,
offence_oas,offence_oas_cy,date_used_from,date_used_to
)
VALUES
(990001,951,'GRAPH01','Offence Graph Test',NULL,
'Contrary to the Offence graph test provision.',NULL,to_date('2024-01-01','yyyy-mm-dd'),NULL);
