package logic;

import model.AdminUser;
import model.ScheduleItem;

import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private static final String URL  = "jdbc:mariadb://10.0.10.20:3306/attendance_db";
    private static final String USER = "attendance_user";
    private static final String PASS = "strongpassword";

    public static final Map<String, String[]> studentDb  = new HashMap<>();
    public static final List<AdminUser>        adminDb    = new ArrayList<>();
    public static final List<ScheduleItem>     scheduleDb = new ArrayList<>();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    // =========================================================
    //  LOAD ALL
    // =========================================================
    public static boolean loadDatabases() {
        studentDb.clear();
        adminDb.clear();
        scheduleDb.clear();

        try (Connection con = getConnection()) {

            // ── Students ──
            try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(
                    "SELECT rfid_uid, student_id, full_name, section FROM students")) {
               while (rs.next())
                   studentDb.put(rs.getString("rfid_uid"),
                       new String[]{
                           rs.getString("full_name"),
                           rs.getString("section"),
                           rs.getString("student_id")   // index [2]
                       });
           }

            // ── Admins ──
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT id, full_name, secret_key FROM admins")) {
                while (rs.next())
                    adminDb.add(new AdminUser(
                        rs.getString("id"),
                        rs.getString("full_name"),
                        rs.getString("secret_key")));
            }

            // ── Schedules — ALL sections ──
            try (Statement st = con.createStatement();             // ← Statement na, not PreparedStatement
                 ResultSet rs = st.executeQuery(
                     "SELECT section, day, subject, start_time, end_time FROM schedules")) {
                while (rs.next())
                    scheduleDb.add(new ScheduleItem(
                        rs.getString("section"),                   // ← section first
                        rs.getString("day").toUpperCase(),
                        rs.getString("subject"),
                        rs.getTime("start_time").toLocalTime(),
                        rs.getTime("end_time").toLocalTime()));
            }


            return true;

        } catch (SQLException e) {
            System.err.println("DB Load Error: " + e.getMessage());
            return false;
        }
    }

    // ── Selective reloads (used after CRUD ops) ───────────────
    // ── studentDb: key = rfid_uid, value = [fullName, section, studentId] ──
        public static void loadStudents() {
            studentDb.clear();
            String sql = "SELECT rfid_uid, student_id, full_name, section FROM students";
            try (Connection con = getConnection();
                 Statement st  = con.createStatement();
                 ResultSet rs  = st.executeQuery(sql)) {
                while (rs.next())
                    studentDb.put(rs.getString("rfid_uid"), new String[]{
                        rs.getString("full_name"),
                        rs.getString("section"),
                        rs.getString("student_id")
                    });
            } catch (SQLException e) {
                System.err.println("loadStudents error: " + e.getMessage());
            }
        }

    public static void loadAdmins() {
        adminDb.clear();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id, full_name, secret_key FROM admins")) {
            while (rs.next())
                adminDb.add(new AdminUser(
                    rs.getString("id"),
                    rs.getString("full_name"),
                    rs.getString("secret_key")));
        } catch (SQLException e) {
            System.err.println("loadAdmins error: " + e.getMessage());
        }
    }

    // =========================================================
    //  STUDENTS CRUD
    // =========================================================
    public static boolean insertStudent(String rfidUid, String studentId,
                                    String fullName, String section) {
    String sql = "INSERT INTO students (rfid_uid, student_id, full_name, section) " +
                 "VALUES (?, ?, ?, ?)";
    try (Connection con = getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setString(1, rfidUid);
        ps.setString(2, studentId);
        ps.setString(3, fullName);
        ps.setString(4, section);
        ps.executeUpdate();
        studentDb.put(rfidUid, new String[]{fullName, section, studentId});
        return true;
    } catch (SQLException e) {
        System.err.println("insertStudent error: " + e.getMessage());
        return false;
    }
}

        public static boolean updateStudent(String oldRfid, String newRfid,
                                            String studentId, String fullName,
                                            String section) {
            String sql = "UPDATE students SET rfid_uid=?, student_id=?, " +
                         "full_name=?, section=? WHERE rfid_uid=?";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, newRfid);
                ps.setString(2, studentId);
                ps.setString(3, fullName);
                ps.setString(4, section);
                ps.setString(5, oldRfid);
                ps.executeUpdate();
                studentDb.remove(oldRfid);
                studentDb.put(newRfid, new String[]{fullName, section, studentId});
                return true;
            } catch (SQLException e) {
                System.err.println("updateStudent error: " + e.getMessage());
                return false;
            }
        }

        public static boolean deleteStudent(String rfidUid) {
            String sql = "DELETE FROM students WHERE rfid_uid=?";
            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, rfidUid);
                ps.executeUpdate();
                studentDb.remove(rfidUid);
                return true;
            } catch (SQLException e) {
                System.err.println("deleteStudent error: " + e.getMessage());
                return false;
            }
        }


    // =========================================================
    //  ADMINS CRUD
    // =========================================================
    public static boolean insertAdmin(String id, String fullName, String secretKey) {
        String sql = "INSERT INTO admins (id, full_name, secret_key) VALUES (?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, fullName);
            ps.setString(3, secretKey);
            ps.executeUpdate();
            adminDb.add(new AdminUser(id, fullName, secretKey)); // sync cache
            return true;
        } catch (SQLException e) {
            System.err.println("insertAdmin error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAdmin(String oldId, String newId,
                                      String fullName, String secretKey) {
        String sql = "UPDATE admins SET id=?, full_name=?, secret_key=? WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, fullName);
            ps.setString(3, secretKey);
            ps.setString(4, oldId);
            ps.executeUpdate();
            adminDb.removeIf(a -> a.id.equals(oldId));
            adminDb.add(new AdminUser(newId, fullName, secretKey)); // sync cache
            return true;
        } catch (SQLException e) {
            System.err.println("updateAdmin error: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteAdmin(String id) {
        String sql = "DELETE FROM admins WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            adminDb.removeIf(a -> a.id.equals(id)); // sync cache
            return true;
        } catch (SQLException e) {
            System.err.println("deleteAdmin error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    //  ATTENDANCE LOG
    // =========================================================
    public static boolean insertAttendance(String studentId, String fullName,
                                           String subject,   String section,
                                           String status,    String date,
                                           String time) {
        String sql = """
            INSERT INTO attendance_log
                (student_id, full_name, subject, section, status, date_logged, time_logged)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, fullName);
            ps.setString(3, subject);
            ps.setString(4, section);
            ps.setString(5, status);
            ps.setString(6, date);
            ps.setString(7, time);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertAttendance error: " + e.getMessage());
            return false;
        }
    }

    public static ResultSet getAttendanceLogs() throws SQLException {
        Connection con = getConnection(); // caller must close
        Statement st   = con.createStatement();
        return st.executeQuery(
            "SELECT * FROM attendance_log ORDER BY date_logged DESC, time_logged DESC");
    }
}
