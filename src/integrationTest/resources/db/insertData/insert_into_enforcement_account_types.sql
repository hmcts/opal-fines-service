-- Insert some test data
DELETE FROM enforcement_account_types
WHERE enforcement_account_type_id IN (1, 2, 3);

INSERT INTO enforcement_account_types (enforcement_account_type_id, account_type, minimum_balance,
                                       enforcement_account_type, account_type_path, version_number)
VALUES
  (1, 'COL', 100, 'COLH',
        'L', 1),
  (2, 'COL', 200, 'COLL',
   'L', 1),
(3, 'A', 300, 'AH',
  'H', 1);
