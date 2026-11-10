INSERT INTO prosecutors (
    prosecutor_code,
    name,
    address_line_1,
    address_line_2,
    address_line_3,
    address_line_4,
    address_line_5,
    postcode,
    end_date
)
VALUES
    (1111, 'AA1 Chief Prosecutor', '9 Prosecutor Street', 'Prosecutorville', 'Prosecutorton',
     NULL, NULL, 'PR01 2PR', NULL),
    (2222, 'AA2 Assistant Prosecutor', '49 Prosecutor Street', 'Prosecutorville', 'Prosecutorton',
     NULL, NULL, 'PR01 4PR', '2099-11-24 10:00:00'),
    (3333, 'AA3 Junior Prosecutor', '99 Prosecutor Street', 'Prosecutorville', 'Prosecutorton',
     NULL, NULL, 'PR01 9PR', '2009-04-16 20:00:00'),
    (9990, 'AA4 Boundary Prosecutor',
     '123456789012345678901234567890123456789012345678901234567890',
     'Boundaryville', 'Boundaryton', NULL, NULL, 'PR06 0PR', NULL);
