package model;

public class AttendanceResult {
    public String name, displayID, subject, timeDetails, status, section;

    public AttendanceResult(String name, String displayID,
                            String subject, String timeDetails, 
                            String status, String section) {
        this.name = name;
        this.displayID = displayID;
        this.subject = subject;
        this.timeDetails = timeDetails;
        this.status = status;
        this.section = section;
    }
}
