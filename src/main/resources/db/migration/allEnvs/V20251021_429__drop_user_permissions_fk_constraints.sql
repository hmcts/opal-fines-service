/**
* OPAL Program
*
* MODULE      : drop_user_permissions_fk_constraints.sql
*
* DESCRIPTION : Drop foreign key constraints related to user permissions tables
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 15/10/2025    C Cho       1.0         PO-541 DB – Drop FK constraints before dropping permissions tables from opal-fines DB
*
**/

-- Drop the only external FK that points at a target table:
ALTER TABLE public.log_audit_details DROP CONSTRAINT lad_user_id_fk;