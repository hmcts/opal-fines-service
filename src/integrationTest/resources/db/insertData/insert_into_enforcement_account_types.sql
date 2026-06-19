/****
  Created by Jordan Muscott
  This is here as a temporary solution to allow integration tests to be written
  TODO - Delete this file once PO-2456 is merged
*/
INSERT INTO public.enforcement_account_types (enforcement_account_type_id, account_type, enforcement_account_type, account_type_path, version_number, minimum_balance) VALUES
    (1, 'COL', 'COLH', 'H', 1, null),
	(2, 'COL', 'COLL', 'L', 1, null),
	(3, 'A', 'AH', 'H', 1, null),
	(4, 'A', 'AL', 'L', 1, null),
	(5, 'CO', 'COH', 'H', 1, null),
	(6, 'CO', 'COL', 'L', 1, null),
	(7, 'Y', 'YH', 'H', 1, null),
	(8, 'Y', 'YL', 'L', 1, null);

/*
|*enforcement_account_type_id*|*account_type*|*enforcement_account_type*|*account_type_path*|*version_n
| |COL|COLH|H|1|NULL|
| |COL|COLL|L|1|NULL|
| |A|AH|H|1|NULL|
| |A|AL|L|1|NULL|
| |CO|COH|H|1|NULL|
| |CO|COL|L|1|NULL|
| |Y|YH|H|1|NULL|
| |Y|YL|L|1|NULL|
*/
