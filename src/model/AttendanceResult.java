package model;

// simple model para sa attendance result. ito yung gagamitin para i-display yung mga details sa attendance log table.
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
