/**
* OPAL Program
*
* MODULE      : delete_discovery_mock_data.sql
*
* DESCRIPTION : Delete all data, intended for Discovery+ and no longer required for the Delivery phase of the project. It also enables us to load reference data such as LJA, Courts etc without having to unnecessarily update child mocked up data, that serve no purpose, in these tables.
*
* VERSION HISTORY:
*
* Date          Author      Version     Nature of Change
* ----------    -------     --------    -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
* 17/01/2025    A Dennis    1.0         PO-1142 Delete all data, intended for Discovery+ and no longer required for the Delivery phase of the project. It also enables us to load reference data such as LJA, Courts etc without having to unnecessarily update child mocked up data, that serve no purpose, in these tables.
*
**/
DELETE FROM log_actions;
DELETE FROM print_definition;
DELETE FROM debtor_detail; 
DELETE FROM defendant_transactions;
DELETE FROM mis_debtors;
DELETE FROM notes;
DELETE FROM defendant_account_parties;
DELETE FROM payment_terms;
DELETE FROM fixed_penalty_offences;
DELETE FROM defendant_accounts;
DELETE FROM parties;
DELETE FROM enforcers;
