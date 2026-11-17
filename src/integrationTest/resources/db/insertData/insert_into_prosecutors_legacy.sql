INSERT INTO prosecutors_legacy (
    prosecutor_id,
    name,
    prosecutor_code,
    address_line_1,
    address_line_2,
    address_line_3,
    address_line_4,
    address_line_5,
    postcode,
    end_date
)
VALUES
    (1111, 'Legacy AA1 Chief Prosecutor', NULL, '9 Legacy Prosecutor Street', 'Prosecutorville', 'Prosecutorton',
     NULL, NULL, 'PR01 2PR', NULL),
    (2222, 'Legacy AA2 Assistant Prosecutor', NULL, '49 Legacy Prosecutor Street', 'Prosecutorville',
     'Prosecutorton', NULL, NULL, 'PR01 4PR', '2099-11-24 10:00:00'),
    (3333, 'Legacy AA3 Junior Prosecutor', NULL, '99 Legacy Prosecutor Street', 'Prosecutorville', 'Prosecutorton',
     NULL, NULL, 'PR01 9PR', '2009-04-16 20:00:00'),
    (9990, 'Legacy AA4 Boundary Prosecutor', NULL,
     '123456789012345678901234567890123456789012345678901234567890',
     'Boundaryville', 'Boundaryton', NULL, NULL, 'PR06 0PR', NULL);
