package model;

import java.time.LocalTime;
// simple model para sa schedule item. gagamitin to para i-store yung schedule ng bawat section sa memory (after natin kunin from db) tapos i-loop natin to every attendance scan para malaman kung anong subject at time details ang dapat i-display.
public class ScheduleItem {
    public final String    section;
    public final String    day;
    public final String    subject;
    public final LocalTime startTime;
    public final LocalTime endTime;

    public ScheduleItem(String section, String day, String subject,
                        LocalTime startTime, LocalTime endTime) {
        this.section   = section;
        this.day       = day;
        this.subject   = subject;
        this.startTime = startTime;
        this.endTime   = endTime;
    }
}
