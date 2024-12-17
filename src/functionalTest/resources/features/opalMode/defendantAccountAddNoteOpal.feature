# @PO-139 @PO-138 @Opal
# Feature: Test the add note endpoint for Opal PO-139
#   Scenario: assert add note response is valid for opal route
#     Given I am testing as the "opal-test@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id | 500000000              |
#       | business_unit_id     | 73                     |
#       | note_text            | test account note Opal |
#     Then the add notes response contains
#       | associated_record_id | 500000000              |
#       | note_text            | test account note Opal |
# 
#   Scenario: latest added note response is returned in ac details request
#     Given I am testing as the "opal-test@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id | 500000001               |
#       | business_unit_id     | 73                     |
#       | note_text            | test account note2 Opal |
#     And the add notes response contains
#       | associated_record_id | 500000001               |
#       | note_text            | test account note2 Opal |
#     When I make a request to the defendant account details api with
#       | defendantID | 500000001 |
#     Then the following account note is returned in the ac details request
#       | account_notes | test account note2 Opal |
