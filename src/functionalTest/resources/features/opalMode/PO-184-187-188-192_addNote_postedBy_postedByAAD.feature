# @PO-184 @PO-187 @PO-188 @PO-192 @Opal
# Feature: tests for changes to postedBy and postedByAAD
#
#   Scenario: postedBy and postedByAAD are correct for the different users
#     Given I am testing as the "opal-test@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id  | 500000010                                |
#       | business_unit_id      | 73                                       |
#       | note_text             | test postedBy and PostedByAAD Opal user1 |
#     And the add notes response contains
#       | associated_record_id  | 500000010                                |
#       | note_text             | test postedBy and PostedByAAD Opal user1 |
#       | posted_by             | L073JG                                   |
#       | posted_by_user_id     | 500000000                                |
#
#     Given I am testing as the "opal-test-2@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id | 500000010                                |
#       | business_unit_id     | 71                                       |
#       | note_text            | test postedBy and PostedByAAD Opal user2 |
#     Then the add notes request is forbidden
#
#     Given I am testing as the "opal-test-3@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id  | 500000010                                |
#       | business_unit_id      | 26                                       |
#       | note_text             | test postedBy and PostedByAAD Opal user3 |
#     And the add notes response contains
#       | associated_record_id  | 500000010                                |
#       | note_text             | test postedBy and PostedByAAD Opal user3 |
#       | posted_by             | L026SH                                   |
#       | posted_by_user_id     | 500000002                                |
#
#     Given I am testing as the "opal-test-4@hmcts.net" user
#     When I make a request to the defendant account add notes api with
#       | associated_record_id  | 500000010                                |
#       | business_unit_id      | 47                                       |
#       | note_text             | test postedBy and PostedByAAD Opal user4 |
#     And the add notes response contains
#       | associated_record_id  | 500000010                                |
#       | note_text             | test postedBy and PostedByAAD Opal user4 |
#       | posted_by             | L047SA                                   |
#       | posted_by_user_id     | 500000003                                |
