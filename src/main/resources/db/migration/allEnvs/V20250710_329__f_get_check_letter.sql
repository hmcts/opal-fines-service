CREATE OR REPLACE FUNCTION f_get_check_letter(
    pi_account_number IN VARCHAR)
    RETURNS VARCHAR
AS $$
/**
* CGI OPAL Program
*
* MODULE      : f_get_check_letter.sql
*
* DESCRIPTION : Generate a check letter
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ----------------------------------------------------------------------------
* 02/07/2025    Garry C     1.0         PO-899 Generate a check letter
* 02/07/2025    TMc         1.1         PO-899 Prepare code for deployment
*
**/
DECLARE
    n_asc int;
BEGIN
    n_asc := substr(pi_account_number,1,1)::int * 5
        + substr(pi_account_number,2,1)::int*1
        + substr(pi_account_number,3,1)::int*4
        + substr(pi_account_number,4,1)::int*2
        + substr(pi_account_number,5,1)::int*7
        + substr(pi_account_number,6,1)::int*5
        + substr(pi_account_number,7,1)::int*1
        + substr(pi_account_number,8,1)::int*4;
    n_asc := 23-(n_asc-(trunc(n_asc/23)*23));
    n_asc := CASE n_asc WHEN 23 THEN 0 ELSE n_asc END;
    RETURN chr(n_asc+65);
END;
$$ LANGUAGE plpgsql;