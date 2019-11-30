package ca.mohawk.le.mytime;

/**
 * Represents a health test
 */
public class HealthTest {
    String name; // Health test name
    String frequency; // Health test frequency
    String unit; // Health test unit (days, months, years)
    String lastTestDate; // Last date of the health test

    /**
     * Class constructor
     */
    public HealthTest(String name, String frequency, String unit, String lastTestDate) {
        this.name = name;
        this.frequency = frequency;
        this.unit = unit;
        this.lastTestDate = lastTestDate;
    }
}
