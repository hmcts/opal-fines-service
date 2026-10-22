/**
* CGI OPAL Program
*
* MODULE      : migrate_report_location_to_uuid.sql
*
* DESCRIPTION : Dev-only synthetic test data being updated to support the correct location
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ------------------------------------------------------------------------------------
* 22/00/2026    BE             0.0         Support changes in /Users/benedwards/Desktop/Projects/opal/github/opal-fines-service/src/main/resources/db/migration/ddl/V0_347__change_report_instances_location_to_uuid.sql

*
**/

update public.report_instances
set location = '00000000-0000-0000-0000-000000000000'
where location is not null;