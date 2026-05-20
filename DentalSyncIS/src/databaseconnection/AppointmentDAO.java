package databaseconnection;

import java.sql.*;
import java.util.*;

public class AppointmentDAO {

    public boolean addAppointment(int patientId, String date, String time,
                                  String procedure, String dentist, String notes) {
        String sql = "INSERT INTO appointments (patient_id,appointment_date,appointment_time,procedure_name,dentist_name,notes) VALUES (?,?,?,?,?,?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            ps.setString(2, date);
            ps.setString(3, time);
            ps.setString(4, procedure);
            ps.setString(5, dentist);
            ps.setString(6, notes);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateStatus(int appointmentId, String status) {
        String sql = "UPDATE appointments SET status=? WHERE appointment_id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deleteAppointment(int id) {
        String sql = "DELETE FROM appointments WHERE appointment_id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Object[]> searchAppointments(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT a.appointment_id, p.full_name, a.appointment_date,
                   a.appointment_time, a.procedure_name, a.dentist_name,
                   a.status, a.notes
            FROM appointments a
            JOIN patients p ON a.patient_id = p.patient_id
            WHERE p.full_name LIKE ? OR a.procedure_name LIKE ? OR a.dentist_name LIKE ?
            ORDER BY a.appointment_date DESC
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("appointment_id"),
                    rs.getString("full_name"),
                    rs.getString("appointment_date"),
                    rs.getString("appointment_time"),
                    rs.getString("procedure_name"),
                    rs.getString("dentist_name"),
                    rs.getString("status"),
                    rs.getString("notes")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getAllAppointments() { return searchAppointments(""); }

    // For payment form combo
    public List<String[]> getAppointmentList() {
        List<String[]> list = new ArrayList<>();
        String sql = """
            SELECT a.appointment_id, p.full_name, a.procedure_name
            FROM appointments a JOIN patients p ON a.patient_id=p.patient_id
            WHERE a.status='Completed'
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new String[]{
                    rs.getString("appointment_id"),
                    rs.getString("full_name") + " - " + rs.getString("procedure_name")
                });
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}