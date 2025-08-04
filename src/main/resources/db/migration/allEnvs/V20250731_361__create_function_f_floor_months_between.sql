/**
* CGI OPAL Program
*
* MODULE      : create_function_f_floor_months_between.sql
*
* DESCRIPTION : Capita's function to calculate the full number of months between two dates
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ------------------------------------------------------------------------------------
* 25/07/2025    C Cho       1.0         PO-1641 - Capita's function to calculate the full number of months between two dates
*
**/
CREATE OR REPLACE FUNCTION f_floor_months_between(
    p1 DATE,
    p2 DATE)
RETURNS INTEGER
AS $$
DECLARE
    m1 INTEGER;
    m2 INTEGER;
BEGIN
    m1 = date_part('month',p1) + (12 * date_part('year',p1));
    m2 = date_part('month',p2) + (12 * date_part('year',p2));
    RETURN
        CASE
            WHEN p1 > p2 THEN CASE WHEN date_part('day',p1) >= date_part('day',p2) THEN m1-m2 ELSE m1-m2-1 END
            ELSE CASE WHEN date_part('day',p2) >= date_part('day',p1) THEN m1-m2 ELSE m1-m2-1 END
        END;
END;
$$ LANGUAGE plpgsql;