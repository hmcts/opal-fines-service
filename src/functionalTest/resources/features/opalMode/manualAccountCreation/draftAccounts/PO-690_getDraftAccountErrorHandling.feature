
Feature: PO-690 get draft account error handling scenarios

  @PO-690
    #When request includes an invalid access token or does not include access token
  Scenario: Get draft account id - unhappy path -Response code 401
    Given I am testing as the "opal-test@hmcts.net" user
    When I request the draft account id with invalid token
    Then The draft account response returns 401
    Then I delete the created draft accounts

  Scenario: Get draft account id - unhappy path -Response code 404
    Given I am testing as the "opal-test@hmcts.net" user
    When I request the draft account with incorrect account id
    Then The draft account response returns 404
    Then I delete the created draft accounts

  Scenario: Get draft account id - unhappy path -Response code 406
    Given I am testing as the "opal-test@hmcts.net" user
    When I request the draft account with content type mismatch
    Then The draft account response returns 404
    Then I delete the created draft accounts
