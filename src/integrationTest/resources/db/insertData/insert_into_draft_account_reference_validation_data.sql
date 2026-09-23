INSERT INTO local_justice_areas (
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
    (
        32001,
        'Draft Account Validation LJA',
        '1 Validation Street',
        NULL,
        NULL,
        'VA1 1AA',
        'V001',
        'LJA',
        NULL,
        NULL,
        NULL
    ),
    (
        32002,
        'Draft Account Validation Crown Court',
        '2 Validation Street',
        NULL,
        NULL,
        'VA2 2BB',
        'V002',
        'CRWCRT',
        NULL,
        NULL,
        NULL
    ),
    (
        32003,
        'Draft Account Validation SJ Court',
        '3 Validation Street',
        NULL,
        NULL,
        'VA3 3CC',
        'V003',
        'SJCRT',
        NULL,
        NULL,
        NULL
    );

INSERT INTO prosecutors (
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
VALUES (
    32010,
    'Draft Account Validation Prosecutor',
    'VP01',
    '4 Validation Street',
    NULL,
    NULL,
    NULL,
    NULL,
    'VP1 1VP',
    NULL
);
