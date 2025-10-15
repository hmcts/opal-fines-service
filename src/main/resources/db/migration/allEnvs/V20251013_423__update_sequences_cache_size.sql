/**
* OPAL Program
*
* MODULE      : update_sequences_cache_size.sql
*
* DESCRIPTION : Update database sequences to disable CACHE (set CACHE to 1) for all sequences with CACHE greater than 1
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 09/10/2025    C Cho       1.0         PO-1968 Update database sequences to set CACHE to 1 for all sequences with CACHE greater than 1
*
**/

DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN
        SELECT schemaname, sequencename, cache_size
        FROM pg_catalog.pg_sequences 
        WHERE schemaname = 'public'
          AND cache_size > 1
        ORDER BY sequencename
    LOOP
        EXECUTE 'ALTER SEQUENCE ' || rec.sequencename || ' CACHE 1';
    END LOOP;
END $$;