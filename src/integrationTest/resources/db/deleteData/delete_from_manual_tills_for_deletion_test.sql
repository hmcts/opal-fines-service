DELETE FROM payments_in
WHERE till_id IN (
                  99000000836300,
                  99000000836301,
                  99000000836302
    );

DELETE FROM tills
WHERE till_id IN (
                  99000000836300,
                  99000000836301,
                  99000000836302
    );

DELETE FROM business_units
WHERE business_unit_id = 8363;