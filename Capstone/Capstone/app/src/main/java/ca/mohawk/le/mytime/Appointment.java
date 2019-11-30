package ca.mohawk.le.mytime;

/**
 * Represents an appointment
 */
public class Appointment {
    String name; // Appointment name
    String doctor; // Appointment doctor
    String address; // Appointment address
    String date; // Appointment date
    String time; // Appointment time

    /**
     * Class constructor
     */
    public Appointment(String name, String doctor, String address, String date, String time) {
        this.name = name;
        this.doctor = doctor;
        this.address = address;
        this.date = date;
        this.time = time;
    }

}
