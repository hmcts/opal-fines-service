/**
* OPAL Program
*
* MODULE      : drop_user_permissions_related_tables.sql
*
* DESCRIPTION : Drop user permissions related tables
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 15/10/2025    C Cho       1.0         PO-541 DB – Drop specified permissions tables from opal-fines DB
*
**/

-- Drop target tables in dependency-safe order:
DROP TABLE IF EXISTS public.user_entitlements;
DROP TABLE IF EXISTS public.template_mappings;
DROP TABLE IF EXISTS public.business_unit_users;
DROP TABLE IF EXISTS public.templates;
DROP TABLE IF EXISTS public.application_functions;
DROP TABLE IF EXISTS public.users;