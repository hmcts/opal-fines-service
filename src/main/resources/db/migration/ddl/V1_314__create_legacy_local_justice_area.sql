CREATE TABLE public.legacy_local_justice_area (
    local_justice_area_id smallint NOT NULL,
    name character varying(100) NOT NULL,
    address_line_1 character varying(35) NOT NULL,
    address_line_2 character varying(35),
    address_line_3 character varying(35),
    postcode character varying(8),
    lja_code character varying(4),
    address_line_4 character varying(35),
    address_line_5 character varying(35),
    end_date timestamp without time zone,
    lja_type public.t_lja_type_enum
);

ALTER TABLE ONLY public.legacy_local_justice_area
    ADD CONSTRAINT legacy_local_justice_area_id_pk PRIMARY KEY (local_justice_area_id);

COMMENT ON COLUMN public.legacy_local_justice_area.local_justice_area_id IS 'Unique ID of this record';
COMMENT ON COLUMN public.legacy_local_justice_area.name IS 'LJA name';
COMMENT ON COLUMN public.legacy_local_justice_area.address_line_1 IS 'LJA address line 1';
COMMENT ON COLUMN public.legacy_local_justice_area.address_line_2 IS 'LJA address line 2';
COMMENT ON COLUMN public.legacy_local_justice_area.address_line_3 IS 'LJA address line 3';
COMMENT ON COLUMN public.legacy_local_justice_area.postcode IS 'LJA postcode';
COMMENT ON COLUMN public.legacy_local_justice_area.lja_code IS 'LJA Code';
COMMENT ON COLUMN public.legacy_local_justice_area.address_line_4 IS 'LJA Address line 4';
COMMENT ON COLUMN public.legacy_local_justice_area.address_line_5 IS 'LJA Address line 5';
COMMENT ON COLUMN public.legacy_local_justice_area.end_date IS 'The end date of the record';
COMMENT ON COLUMN public.legacy_local_justice_area.lja_type IS 'The LJA type. Specific values can be found in the DB LLD on Confluence';

INSERT INTO public.legacy_local_justice_area (
    local_justice_area_id,
    name,
    address_line_1,
    address_line_2,
    address_line_3,
    postcode,
    lja_code,
    address_line_4,
    address_line_5,
    end_date,
    lja_type
)
SELECT
    local_justice_area_id,
    name,
    address_line_1,
    address_line_2,
    address_line_3,
    postcode,
    lja_code,
    address_line_4,
    address_line_5,
    end_date,
    lja_type
FROM public.local_justice_areas;
