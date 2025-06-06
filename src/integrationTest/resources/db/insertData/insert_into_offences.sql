/**
* OPAL Program
*
* MODULE      : insert_into_offences.sql
*
* DESCRIPTION : Insert rows of data into OFFENCES table for the Integration Tests
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 05/06/2025    R DODD      1.0         PO-1047 Insert rows of data into OFFENCES table for the Integration Tests
*
**/
INSERT INTO offences
(
offence_id, cjs_code
, offence_title, offence_title_cy
, offence_oas, offence_oas_cy
, date_used_from, date_used_to
)
VALUES
(
00001, 'AAAA01'
, 'Obstruct person acting in the execution of these Regulations', NULL
, 'Contrary to regulations 32(a) & 34(3) of the Animals and Animal Products (Import and Export) (England) Regulations 2006.', NULL
, to_date('2006-10-30','yyyy-mm-dd'), to_date('2024-11-24','yyyy-mm-dd')
);
