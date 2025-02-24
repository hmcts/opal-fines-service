/**
* OPAL Program
*
* MODULE      : update_document_instances.sql
*
* DESCRIPTION : Update rows of test data in the DOCUMENT_INSTANCES table so that so that data in document_instances.business_unit_id would matchh their corresponding primary keys in BUSINESS_UNITS table.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 22/03/2024    A Dennis    1.0         PO-248 Update rows of test data in the DOCUMENT_INSTANCES table so that so that data in document_instances.business_unit_id would matchh their corresponding primary keys in BUSINESS_UNITS table.
**/
UPDATE document_instances
SET    business_unit_id  = 69
WHERE  business_unit_id  = 500;

UPDATE document_instances
SET    business_unit_id  = 94
WHERE  business_unit_id  = 501;

UPDATE document_instances
SET    business_unit_id  = 95
WHERE  business_unit_id  = 502;

UPDATE document_instances
SET    business_unit_id  = 96
WHERE  business_unit_id  = 503;

UPDATE document_instances
SET    business_unit_id  = 13
WHERE  business_unit_id  = 504;

UPDATE document_instances
SET    business_unit_id  = 16
WHERE  business_unit_id  = 505;

UPDATE document_instances
SET    business_unit_id  = 17
WHERE  business_unit_id  = 506;

UPDATE document_instances
SET    business_unit_id  = 19
WHERE  business_unit_id  = 507;

UPDATE document_instances
SET    business_unit_id  = 32
WHERE  business_unit_id  = 508;

UPDATE document_instances
SET    business_unit_id  = 64
WHERE  business_unit_id  = 509;
