package databaseconnection;

import java.sql.*;
import java.util.*;

public class PatientDAO {

    public boolean addPatient(String fullName, String birthdate, String gender,
                              String contact, String address, String email) {
        String sql = "INSERT INTO patients (full_name,birthdate,gender,contact,address,email) VALUES (?,?,?,?,?,?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, birthdate);
            ps.setString(3, gender);
            ps.setString(4, contact);
            ps.setString(5, address);
            ps.setString(6, email);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePatient(int id, String fullName, String birthdate, String gender,
                                 String contact, String address, String email) {
        String sql = "UPDATE patients SET full_name=?,birthdate=?,gender=?,contact=?,address=?,email=? WHERE patient_id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, birthdate);
            ps.setString(3, gender);
            ps.setString(4, contact);
            ps.setString(5, address);
            ps.setString(6, email);
            ps.setInt(7, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean deletePatient(int id) {
        String sql = "DELETE FROM patients WHERE patient_id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Object[]> searchPatients(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE full_name LIKE ? OR contact LIKE ? OR email LIKE ?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("patient_id"),
                    rs.getString("full_name"),
                    rs.getString("birthdate"),
                    rs.getString("gender"),
                    rs.getString("contact"),
                    rs.getString("address"),
                    rs.getString("email"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getAllPatients() { return searchPatients(""); }

    // For combo boxes in Appointment/Payment forms
    public List<String[]> getPatientList() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT patient_id, full_name FROM patients WHERE status='Active'";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(new String[]{rs.getString("patient_id"), rs.getString("full_name")});
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}