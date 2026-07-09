CREATE SEQUENCE defendant_account_publish_sp_failure_seq
    START WITH 1
    INCREMENT BY 1;
@@

CREATE OR REPLACE FUNCTION fail_first_defendant_account_insert()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW.prosecutor_case_reference = 'PUBLISH-RETRY-IT'
       AND nextval('defendant_account_publish_sp_failure_seq') = 1 THEN
        RAISE EXCEPTION 'Test-only failure during stored procedure defendant account insert';
END IF;

RETURN NEW;
END;
$$;
@@

CREATE TRIGGER fail_first_defendant_account_insert
BEFORE INSERT ON defendant_accounts
FOR EACH ROW
EXECUTE FUNCTION fail_first_defendant_account_insert();
@@