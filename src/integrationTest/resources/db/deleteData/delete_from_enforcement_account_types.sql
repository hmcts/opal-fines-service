-- Reset the test data
UPDATE enforcement_account_types
SET account_type='COL', minimum_balance=NULL, enforcement_account_type='COLH',
    account_type_path='L', version_number=1
WHERE enforcement_account_type_id=1;

UPDATE enforcement_account_types
SET account_type='COL', minimum_balance=NULL, enforcement_account_type='COLL',
    account_type_path='L', version_number=1
WHERE enforcement_account_type_id=2;

UPDATE enforcement_account_types
SET account_type='A', minimum_balance=NULL, enforcement_account_type='AH',
    account_type_path='H', version_number=1
WHERE enforcement_account_type_id=3;
