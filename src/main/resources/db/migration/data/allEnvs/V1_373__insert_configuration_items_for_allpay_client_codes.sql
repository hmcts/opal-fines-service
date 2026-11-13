/**
* OPAL Program
*
* MODULE      : insert_configuration_items_for_allpay_client_codes.sql
*
* DESCRIPTION : Insert Allpay client codes into configuration_items for active business units
*
* VERSION HISTORY:
*
* Date          Author         Version     Nature of Change
* ----------    -----------    --------    ----------------------------------------------------------------
* 11/11/2026    TMc            1.0         PO-11166 - Populate CONFIGURATION_ITEMS with Allpay Client Codes
*
**/

INSERT INTO configuration_items (
    configuration_item_id,
    item_name,
    business_unit_id,
    item_value,
    item_values
)
SELECT
    nextval('configuration_item_id_seq'),
    'BUSINESS_UNIT_ALLPAY_CLIENT',
    mapping.business_unit_id,
    mapping.item_value,
    NULL
FROM (
    VALUES
        (5, 'HMCA'),
        (8, 'CMCS'),
        (9, 'CMCT'),
        (10, 'HMDE'),
        (11, 'HMDO'),
        (12, 'CDFC'),
        (14, 'HMGL'),
        (21, 'LMCT'),
        (22, 'LINC'),
        (24, 'MCIP'),
        (26, 'HNEH'),
        (28, 'NYMC'),
        (29, 'HMNO'),
        (30, 'NONO'),
        (31, 'TVOX'),
        (36, 'SWSM'),
        (38, 'HMST'),
        (45, 'GMCO'),
        (47, 'AVSB'),
        (52, 'BMCT'),
        (57, 'DCPD'),
        (60, 'DPMC'),
        (61, 'HMES'),
        (65, 'GLCI'),
        (66, 'GLCW'),
        (73, 'GWLO'),
        (77, 'GLLS'),
        (78, 'GLNE'),
        (80, 'GWEN'),
        (82, 'GMBO'),
        (89, 'HMGW'),
        (92, 'HIWS'),
        (96, 'HUMS'),
        (97, 'HMKC'),
        (99, 'HMKW'),
        (103, 'HMPE'),
        (105, 'HMML'),
        (106, 'NOWA'),
        (107, 'ALNW'),
        (109, 'BERW'),
        (110, 'GTHD'),
        (112, 'NCLE'),
        (113, 'NTMC'),
        (116, 'TYND'),
        (119, 'NOTT'),
        (124, 'SYSH'),
        (125, 'SURR'),
        (126, 'SUES'),
        (128, 'HNWA'),
        (129, 'WMER'),
        (130, 'WMMB'),
        (135, 'WYMB'),
        (138, 'WIMC'),
        (139, 'HMNC')
) AS mapping (business_unit_id, item_value)
WHERE NOT EXISTS (
    SELECT 1
    FROM configuration_items existing_mapping
    WHERE existing_mapping.item_name = 'BUSINESS_UNIT_ALLPAY_CLIENT'
      AND existing_mapping.business_unit_id = mapping.business_unit_id
);
