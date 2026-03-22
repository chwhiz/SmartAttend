package model;

import java.time.LocalTime;

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
