Feature: SuperBay
  Scenario: must load the homepage
    Given I load the homepage
    Then I must be on the homepage

  Scenario: must be able to search
    Given I load the homepage
    When I search for "computer"
    Then I should have some results
    When I click on the first result
    Then I should see the first result



