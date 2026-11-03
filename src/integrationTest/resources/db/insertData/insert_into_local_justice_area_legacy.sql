INSERT INTO local_justice_areas_legacy (
    local_justice_area_id,
    name,
    address_line_1,
    address_line_2,
    address_line_3,
    postcode,
    lja_code,
    lja_type,
    address_line_4,
    address_line_5,
    end_date
)
VALUES
    (1, 'AAAA Trial Court', 'Alpha Trial Courts', 'Court Quarter', '666 Trial Street',
     'TR12 1TR', '0007', 'LJA', 'Trialton', 'Trialshire', NULL),
    (10, 'BBBB Trial Court', 'Beta Trial Courts', 'Court Quarter', '666 Trial Street',
     'TR12 1TR', '0008', 'SJCRT', 'Trialton', 'Trialshire', NULL),
    (20, 'CCCC Trial Court', 'Charlie Trial Courts', 'Court Quarter', '666 Trial Street',
     'TR12 1TR', '0009', 'SJCRT', 'Trialton', 'Trialshire', NULL)
ON CONFLICT (local_justice_area_id) DO UPDATE SET
    name = EXCLUDED.name,
    address_line_1 = EXCLUDED.address_line_1,
    address_line_2 = EXCLUDED.address_line_2,
    address_line_3 = EXCLUDED.address_line_3,
    postcode = EXCLUDED.postcode,
    lja_code = EXCLUDED.lja_code,
    lja_type = EXCLUDED.lja_type,
    address_line_4 = EXCLUDED.address_line_4,
    address_line_5 = EXCLUDED.address_line_5,
    end_date = EXCLUDED.end_date;
