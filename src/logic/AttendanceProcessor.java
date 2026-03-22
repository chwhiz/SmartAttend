package logic;

import model.AttendanceResult;
import model.ScheduleItem;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class AttendanceProcessor {

    private static final ZoneId PH_TIME = ZoneId.of("Asia/Manila");

    /**
     * Processes attendance logic for a given student based on their RFID UID.
     * Determines current schedule, tardiness, and logs the result.
     * 
     * @param rfidUid The unique ID from the scanner
     * @param isTimeOut Indicates if this scan is for leaving
     * @return AttendanceResult object containing logged details, or null if unassigned
     */

    // kunin yung records from db gamit rfid.
    public static AttendanceResult process(String rfidUid, boolean isTimeOut) {
        String[] data = DatabaseManager.studentDb.get(rfidUid);
        if (data == null) return null; // pag wala sa db, invisible ata yung student (or wrong scan lang)

        // set up variables kasi need natin i-format properly sa display
        String name      = data[0];
        String section   = data[1];
        String studentId = data[2];   // yung display ID nila e.g. "12-3456-789"

        LocalTime now      = DatabaseManager.getNowTime();
        LocalDate today    = DatabaseManager.getNowDate();
        String    todayStr = today.getDayOfWeek().toString();

        // default values if walang class currently. either pauwi na or tambay
        String subject     = isTimeOut ? "Leaving Campus" : "Free Time / Break";
        String status      = isTimeOut ? "TIME OUT" : "LOGGED";
        String timeDetails = "--:--";

        // hanapin natin sa schedule arrays kung may class ba sila now
        // basta loop na to
        for (ScheduleItem item : DatabaseManager.scheduleDb) {
            if (item.section.equalsIgnoreCase(section)
                    && item.day.equals(todayStr)
                    && !now.isBefore(item.startTime)
                    && now.isBefore(item.endTime)) {
                
                subject = item.subject;
                timeDetails = item.startTime + " - " + item.endTime;
                
                if (isTimeOut) {
                    status = "EARLY OUT"; // umuwi nang maaga, cut ba to? haha
                } else {
                    // checks kung late. 5 mins is grace period. above 15 is absent na yan pre
                    long minsLate = ChronoUnit.MINUTES.between(item.startTime, now);
                    if (minsLate <= 5) {
                        status = "PRESENT";
                    } else if (minsLate <= 15) {
                        status = "LATE (+" + minsLate + "m)";
                    } else {
                        status = "ABSENT";
                    }
                }
                break; // nahanap na yung current subj, tapos na tayo rito
            }
        }

        // isave sa db yung result siyempre para may proof
        logToDatabase(name, section, studentId, subject, status, now, today);
        return new AttendanceResult(name, studentId, subject, timeDetails, status, section);
    }

    private static void logToDatabase(String name, String section, String studentId,
                                      String subject, String status,
                                      LocalTime time, LocalDate date) {
        DatabaseManager.insertAttendance(
            studentId.replace("-", ""),
            name, subject, section, status, date, time, DatabaseManager.kioskId
        );
    }
}
