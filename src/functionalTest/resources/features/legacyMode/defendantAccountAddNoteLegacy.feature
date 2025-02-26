# @PO-141 @Legacy @Ignore
# Feature: Test the add note endpoint for Legacy
# 
#   Scenario: assert add note canned response is valid for Legacy route
#     When I make a request to the defendant account add notes api with
#       | associated_record_id | 500000000          |
#       | business_unit_id     | 17                 |
#       | note_text            | test account note1 |
#     Then the add notes response contains
#       | note_id              | 12345678           |
#       | associated_record_id | 500000000          |
#       | note_text            | test account note1 |
