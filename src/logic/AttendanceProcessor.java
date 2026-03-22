package logic;

import model.AttendanceResult;
import model.ScheduleItem;

import java.sql.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class AttendanceProcessor {

    private static final ZoneId PH_TIME = ZoneId.of("Asia/Manila");

    // ← Now takes rfidUid only; looks up all data internally
    public static AttendanceResult process(String rfidUid) {
        String[] data = DatabaseManager.studentDb.get(rfidUid);
        if (data == null) return null;

        String name      = data[0];
        String section   = data[1];
        String studentId = data[2];   // display ID e.g. "12-3456-789"

        LocalTime now      = LocalTime.now(PH_TIME);
        LocalDate today    = LocalDate.now(PH_TIME);
        String    todayStr = today.getDayOfWeek().toString();

        String subject     = "Free Time / Break";
        String status      = "LOGGED";
        String timeDetails = "--:--";

        for (ScheduleItem item : DatabaseManager.scheduleDb) {
            if (item.section.equalsIgnoreCase(section)
                    && item.day.equals(todayStr)
                    && !now.isBefore(item.startTime)
                    && now.isBefore(item.endTime)) {
                subject = item.subject;
                long minsLate = ChronoUnit.MINUTES.between(item.startTime, now);
                status      = minsLate > 15
                    ? "LATE (+" + minsLate + "m)"
                    : "PRESENT";
                timeDetails = item.startTime + " - " + item.endTime;
                break;
            }
        }

        logToDatabase(name, section, studentId, subject, status, now, today);
        return new AttendanceResult(name, studentId, subject, timeDetails, status, section);
    }

    private static void logToDatabase(String name, String section, String studentId,
                                      String subject, String status,
                                      LocalTime time, LocalDate date) {
        String sql = "INSERT INTO attendance_log " +
                     "(student_id, full_name, section, subject, status, log_date, log_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId.replace("-", ""));
            ps.setString(2, name);
            ps.setString(3, section);
            ps.setString(4, subject);
            ps.setString(5, status);
            ps.setDate  (6, java.sql.Date.valueOf(date));
            ps.setTime  (7, java.sql.Time.valueOf(time));
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Log Error: " + e.getMessage());
        }
    }
}
