Feature: Happy Paths

  Scenario: Temperature increase, one dependency
    Given the configuration file "temp_increase_one_dep.yaml"
    And The temperature of "depA" is 30
    When the temperature of "depA" does not change
    Then rpm of fan "fanA" is 25
    When The temperature increases to 40
    Then Fan RPM is set to 45
