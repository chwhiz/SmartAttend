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
    public static final List<ScheduleItem>     scheduleDb = new ArrayList<>();
    // List ng mga sections para di na natin idisplay hardcoded wkwkwk
    public static final List<String>           sectionList = new ArrayList<>();
    
    // Developer Flags KASI NAKAKAINIS KAMO YUNG PANAY EDIT SA MISMONG CODE SDNGDFJKGHJDFKG
    // ginawa ko tong flags para makapag-skip sa long auth 
    public static boolean devBypassTOTP = false;
    public static boolean devBypassRateLimit = false;
    public static java.time.Duration devTimeOffset = null;

    public static java.time.LocalTime getNowTime() {
        return getNowDateTime().toLocalTime();
    }

    public static java.time.LocalDate getNowDate() {
        return getNowDateTime().toLocalDate();
    }

    public static java.time.ZonedDateTime getNowZoned() {
        return getNowDateTime().atZone(java.time.ZoneId.of("Asia/Manila"));
    }

    public static java.time.LocalDateTime getNowDateTime() {
        java.time.LocalDateTime realNow = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Manila"));
        return devTimeOffset != null ? realNow.plus(devTimeOffset) : realNow;
    }
    
    // Kept encapsulated to prevent external tampering (private moments wkwkwk)
    private static final List<AdminUser> adminDb = new ArrayList<>();

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static List<AdminUser> getAdmins() {
        return Collections.unmodifiableList(adminDb);
    }
    
    public static int getAdminCount() {
        return adminDb.size();
    }
    
    public static AdminUser authenticateAdmin(String id) {
        for (AdminUser admin : adminDb) {
            if (admin.id.equalsIgnoreCase(id)) {
                return admin;
            }
        }
        return null;
    }

    // =========================================================
    //  KIOSK IDENTIFIER
    // =========================================================

    // para malaman kung saang kiosk nanggaling yung attendance log.
    // pwede natin i-display sa admin panel para may idea sila kung aling gate ang pinasukan o nilisan ng student
    
    public static String kioskId = "Kiosk - Unassigned";

    public static void loadKioskConfig() {
        try {
            java.io.File file = new java.io.File("kiosk-config.txt");
            if (file.exists()) {
                java.util.Scanner sc = new java.util.Scanner(file);
                if (sc.hasNextLine()) {
                    kioskId = sc.nextLine().trim();
                }
                sc.close();
            } else {
                java.io.FileWriter fw = new java.io.FileWriter(file);
                fw.write("Kiosk 01 - Main Gate");
                kioskId = "Kiosk 01 - Main Gate";
                fw.close();
            }
        } catch (Exception e) {
            System.err.println("Failed to load kiosk config: " + e.getMessage());
        }
    }

    // =========================================================
    //  LOAD ALL
    // =========================================================
    // load lahat ng lists from db. kayo na bahala magexplain niyan
    // pero okay na 'yan as long as nagra-run hahahaha
    public static boolean loadDatabases() {
        loadKioskConfig();

        // clear muna everything bago kumuha bagong data. reset levels
        studentDb.clear();
        adminDb.clear();
        scheduleDb.clear();
        sectionList.clear();

        try (Connection con = getConnection()) {
            
            // ── Setup missing tables if needed (specifically sections) ──
            try (Statement st = con.createStatement()) {
                st.executeUpdate("CREATE TABLE IF NOT EXISTS sections (section_name VARCHAR(100) PRIMARY KEY)");
            }
            
            // ── Alter attendance_log to add kiosk_id (Ignore if already exists) ──
            // parang crashout workaround to add missing column lmao
            try (Statement st = con.createStatement()) {
                st.executeUpdate("ALTER TABLE attendance_log ADD COLUMN kiosk_id VARCHAR(100) DEFAULT 'Main'");
            } catch (SQLException e) {
                // Column likely already exists: MariaDB/MySQL use SQLState 42S21 and error code 1060.
                String sqlState = e.getSQLState();
                int errorCode = e.getErrorCode();

                if (!"42S21".equals(sqlState) && errorCode != 1060) {
                    // Unexpected error: rethrow so it can be logged/handled by the outer catch.
                    throw e;
                }
                // If we reach here, the column already exists; safe to ignore.
            }

            // ── Sections ──
            // kunin from database, if may sections man.
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT section_name FROM sections ORDER BY section_name")) {
                while (rs.next()) {
                    sectionList.add(rs.getString("section_name"));
                }
            }

            // ── Students ──
            // populate natin yung Map tapos arrays of string ang values :))
            try (Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(
                    "SELECT rfid_uid, student_id, full_name, section FROM students")) {
               while (rs.next())
                   studentDb.put(rs.getString("rfid_uid"),
                       new String[]{
                           rs.getString("full_name"),
                           rs.getString("section"),
                           rs.getString("student_id")   // index [2] stands for student_id
                       });
           }

            // ── Admins ──
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT id, school_id, full_name, secret_key FROM admins")) {
                while (rs.next())
                    adminDb.add(new AdminUser(
                        rs.getString("id"),
                        rs.getString("school_id"),
                        rs.getString("full_name"),
                        rs.getString("secret_key")));
            }

            // ── Schedules — ALL sections ──
            // imbes na prepared statement, statement na agad. literal raw strings babasahin natin
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(
                     "SELECT section, day, subject, start_time, end_time FROM schedules")) {
                while (rs.next())
                    scheduleDb.add(new ScheduleItem(
                        rs.getString("section"),                   // ← section raw first
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
    // reloads lang yung certain tables kung may mag-update from UI para di mabigat palagi
    
    // ── studentDb: key = rfid_uid, value = [fullName, section, studentId] ──
        public static void loadStudents() {
            // clear muna old students list
            studentDb.clear();
            String sql = "SELECT rfid_uid, student_id, full_name, section FROM students";
            try (Connection con = getConnection();
                 Statement st  = con.createStatement();
                 ResultSet rs  = st.executeQuery(sql)) {
                while (rs.next())
                    studentDb.put(rs.getString("rfid_uid"), new String[]{
                        rs.getString("full_name"),
                        rs.getString("section"),
                        rs.getString("student_id") // ito yung string ah, not just mapping reference
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
                 "SELECT id, school_id, full_name, secret_key FROM admins")) {
            while (rs.next())
                adminDb.add(new AdminUser(
                    rs.getString("id"),
                    rs.getString("school_id"),
                    rs.getString("full_name"),
                    rs.getString("secret_key")));
        } catch (SQLException e) {
            System.err.println("loadAdmins error: " + e.getMessage());
        }
    }

    // =========================================================
    //  SECTIONS CRUD
    // =========================================================
    
    // reset then load from database. simple query lang naman
    public static void loadSections() {
        sectionList.clear();
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT section_name FROM sections ORDER BY section_name")) {
            while (rs.next()) {
                sectionList.add(rs.getString("section_name"));
            }
        } catch (SQLException e) {
            System.err.println("loadSections error: " + e.getMessage());
        }
    }

    // add bago section. INSERT IGNORE para kung may duplicate, di mag-e-error tas magc-crash
    public static boolean insertSection(String sectionName) {
        String sql = "INSERT IGNORE INTO sections (section_name) VALUES (?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sectionName);
            ps.executeUpdate();
            
            // update rin in-memory list natin habang fresh pa yung changes
            if (!sectionList.contains(sectionName)) {
                sectionList.add(sectionName);
                Collections.sort(sectionList);
            }
            return true;
        } catch (SQLException e) {
            System.err.println("insertSection error: " + e.getMessage());
            return false;
        }
    }

    // tanggalin section. ingat dito baka magka-orphans yung students
    public static boolean deleteSection(String sectionName) {
        String sql = "DELETE FROM sections WHERE section_name = ?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sectionName);
            ps.executeUpdate();
            
            // alisin sa Java list para sync
            sectionList.remove(sectionName);
            return true;
        } catch (SQLException e) {
            System.err.println("deleteSection error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    //  STUDENTS CRUD
    // =========================================================
    
    // sasalpak yung bagong student sa db.
    // gamit PreparedStatement para iwas SQL injection charot WALA NAMAN SIGURONG GANUN
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
    
    // add admin. need yung secretKey para sa 2FA.
    public static boolean insertAdmin(String id, String schoolId, String fullName, String secretKey) {
        String sql = "INSERT INTO admins (id, school_id, full_name, secret_key) VALUES (?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, schoolId);
            ps.setString(3, fullName);
            ps.setString(4, secretKey);
            ps.executeUpdate();
            
            // i-sync yung internal list para wag nang mag-query ulit (mabagal kasi kung reload lahat)
            adminDb.add(new AdminUser(id, schoolId, fullName, secretKey)); 
            return true;
        } catch (SQLException e) {
            System.err.println("insertAdmin error: " + e.getMessage());
            return false;
        }
    }

    public static boolean updateAdmin(String oldId, String newId, String schoolId,
                                      String fullName, String secretKey) {
        String sql = "UPDATE admins SET id=?, school_id=?, full_name=?, secret_key=? WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, schoolId);
            ps.setString(3, fullName);
            ps.setString(4, secretKey);
            ps.setString(5, oldId);
            ps.executeUpdate();
            
            // manual update sa cache para updated sa memory.
            adminDb.removeIf(a -> a.id.equals(oldId));
            adminDb.add(new AdminUser(newId, schoolId, fullName, secretKey)); 
            return true;
        } catch (SQLException e) {
            System.err.println("updateAdmin error: " + e.getMessage());
            return false;
        }
    }

    // tanggal administator. pwede iself-delete ng admin ahaha we should prob fix that later on pero for now, bahala na sila sa sarili nilang account wkwkwk
    public static boolean deleteAdmin(String id) {
        String sql = "DELETE FROM admins WHERE id=?";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
            adminDb.removeIf(a -> a.id.equals(id)); // remove sa cache to sync state
            return true;
        } catch (SQLException e) {
            System.err.println("deleteAdmin error: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    //  ATTENDANCE LOG
    // =========================================================
    
    // papasok yung successful attendance event.
    // need natin ipasok sa DB para macheck ng users at admin
    public static boolean insertAttendance(String studentId, String fullName,
                                           String subject,   String section,
                                           String status,    java.time.LocalDate date,
                                           java.time.LocalTime time,
                                           String kiosk) {
        String sql = "INSERT INTO attendance_log " +
                     "(student_id, full_name, subject, section, status, log_date, log_time, kiosk_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, studentId);
            ps.setString(2, fullName);
            ps.setString(3, subject);
            ps.setString(4, section);
            ps.setString(5, status);
            ps.setDate  (6, java.sql.Date.valueOf(date));
            ps.setTime  (7, java.sql.Time.valueOf(time));
            ps.setString(8, kiosk);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("insertAttendance error: " + e.getMessage());
            return false;
        }
    }

    // kunin muna yung log mula sa db at ilipat sa list (DTO Pattern) para hindi magka-resource leak
    public static List<model.AttendanceResult> getAttendanceLogs() {
        List<model.AttendanceResult> logs = new ArrayList<>();
        String sql = "SELECT * FROM attendance_log ORDER BY log_date DESC, log_time DESC";
        
        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
             
            while (rs.next()) {
                String date = rs.getDate("log_date").toString();
                String time = rs.getTime("log_time").toString().substring(0, 5); // HH:MM
                
                logs.add(new model.AttendanceResult(
                    rs.getString("full_name"),
                    rs.getString("student_id"),
                    rs.getString("subject"),
                    date + "  " + time,
                    rs.getString("status"),
                    rs.getString("section")
                ));
            }
        } catch (SQLException e) {
            System.err.println("getAttendanceLogs error: " + e.getMessage());
        }
        return logs;
    }
}
