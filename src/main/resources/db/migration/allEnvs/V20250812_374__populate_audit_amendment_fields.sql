/**
* CGI OPAL Program
*
* MODULE      : populate_audit_amendment_fields.sql
*
* DESCRIPTION : Populate the AUDIT_AMENDMENT_FIELDS table in the OPAL Fines database.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    --------    --------    ---------------------------------------------------------------------------------------------------
* 12/08/2025    C Cho       1.0         PO-1646 Populate the AUDIT_AMENDMENT_FIELDS table in the OPAL Fines database.
*
**/

-- Insert data into AUDIT_AMENDMENT_FIELDS table
INSERT INTO audit_amendment_fields (field_code, data_item) VALUES
(1, 'Major Creditor Code'),
(2, 'Name'),
(3, 'Date of Birth'),
(4, 'Age'),
(5, 'Address Line 1'),
(6, 'Address Line 2'),
(7, 'Address Line 3'),
(8, 'Postcode'),
(9, 'Cheque Clearance Period'),
(10, 'Cheque Hold'),
(11, 'Credit Transfer Clearance Period'),
(12, 'Comment'),
(13, 'Inhibit Write Off'),
(14, 'Override Enforcer'),
(15, 'Enforcement Override'),
(16, 'TFOOUT LJA Code'),
(17, 'Parent Name'),
(18, 'Parent Address Line 1'),
(19, 'Parent Address Line 2'),
(20, 'Parent Address Line 3'),
(21, 'AKA Name 1'),
(22, 'AKA Name 2'),
(23, 'AKA Name 3'),
(24, 'AKA Name 4'),
(25, 'AKA Name 5'),
(26, 'Enforcement Court'),
(27, 'Collection Order'),
(28, 'National Insurance Number'),
(29, 'Home Phone Number'),
(30, 'Business Phone Number'),
(31, 'Mobile Phone Number'),
(32, 'Email Address 1'),
(33, 'Email Address 2'),
(34, 'Document Language'),
(35, 'Hearing Language'),
(36, 'Vehicle Make'),
(37, 'Vehicle Registration'),
(38, 'Free Text Notes 1'),
(39, 'Free Text Notes 2'),
(40, 'Free Text Notes 3'),
(41, 'Hold Pay Out'),
(42, 'Pay by BACS'),
(43, 'BACS Sort Code'),
(44, 'BACS Account Type'),
(45, 'BACS Account Number'),
(46, 'BACS Account Name'),
(47, 'BACS Account Reference'),
(48, 'Employee Reference'),
(49, 'Employer Name'),
(50, 'Employer Address Line 1'),
(51, 'Employer Address Line 2'),
(52, 'Employer Address Line 3'),
(53, 'Employer Address Line 4'),
(54, 'Employer Address Line 5'),
(55, 'Employer Postcode'),
(56, 'Employer Phone Number'),
(57, 'Employer Email'),
(58, 'Parent Date of Birth'),
(59, 'Parent NI Number'),
(60, 'SC Date');