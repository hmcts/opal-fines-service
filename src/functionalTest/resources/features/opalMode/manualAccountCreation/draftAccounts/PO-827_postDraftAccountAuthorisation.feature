@Opal
Feature: PO-827 - Authorisation for Post Draft Account

  @PO-827 @cleanUpData
  Scenario: Post Draft Account - Invalid Auth
    Given I set an invalid token
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 401

  @PO-827 @cleanUpData
  Scenario: Post Draft Account - No Permission
    Given I am testing as the "opal-test-2@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 73                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403

  @PO-827 @cleanUpData
  Scenario: Post Draft Account - Permission in different BU
    Given I am testing as the "opal-test@hmcts.net" user
    When I create a draft account with the following details
      | business_unit_id | 26                                          |
      | account          | draftAccounts/accountJson/adultAccount.json |
      | account_type     | Fine                                        |
      | account_status   |                                             |
      | submitted_by     | BUUID                                       |
      | timeline_data    |                                             |
    Then The draft account response returns 403
