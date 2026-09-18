/**
* CGI OPAL Program
*
* MODULE      : V1_320__droped_fk_keys_on_courts.sql
*
* DESCRIPTION : Drop foreign key constraints on courts table to allow for court hierarchy changes
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 18/09/2026    BE / AD      1.0         Drop foreign key constraints on courts table to allow for court hierarchy changes
**/

ALTER TABLE courts
    DROP CONSTRAINT crt_local_justice_area_id_fk;
ALTER TABLE courts
    DROP CONSTRAINT crt_parent_court_id_fk;