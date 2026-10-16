CREATE TABLE IF NOT EXISTS local_justice_areas_legacy (
                                                          local_justice_area_id SMALLINT PRIMARY KEY,
                                                          lja_code VARCHAR(10),
    lja_type VARCHAR(50),

    address_line_1 VARCHAR(35),
    address_line_2 VARCHAR(35),
    address_line_3 VARCHAR(35),
    address_line_4 VARCHAR(35),
    address_line_5 VARCHAR(35),

    postcode VARCHAR(10),

    end_date TIMESTAMP,

    name VARCHAR(100)
    );

INSERT INTO local_justice_areas_legacy (
    local_justice_area_id,
    lja_code,
    lja_type,
    address_line_1,
    address_line_2,
    address_line_3,
    address_line_4,
    address_line_5,
    postcode,
    end_date,
    name
)
VALUES (
           500,
           'TYNE',
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           NULL,
           'Tyne & Wear LJA'
       )ON CONFLICT (local_justice_area_id) DO NOTHING;
