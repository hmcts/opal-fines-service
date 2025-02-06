/**
* OPAL Program
*
* MODULE      : column_version_number.sql
*
* DESCRIPTION : Add the VERSION_NUMBER to the DRAFT_ACCOUNTS table to help use JPA to investigate the most appropriate locking mechanism to adopt
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 06/02/2025    A Dennis    1.0         PO-1148 Add the VERSION_NUMBER to the DRAFT_ACCOIUNTS table to help use JPA to investigate the most appropriate locking mechanism to adopt
*
**/
ALTER TABLE DRAFT_ACCOUNTS ADD VERSION_NUMBER bigint;
