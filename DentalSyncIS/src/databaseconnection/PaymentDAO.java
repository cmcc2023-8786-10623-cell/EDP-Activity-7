package databaseconnection;

import java.sql.*;
import java.util.*;

public class PaymentDAO {

    public boolean addPayment(int patientId, Integer appointmentId, double amount,
                              String date, String method, String procedure, String status) {
        String sql = "INSERT INTO payments (patient_id,appointment_id,amount,payment_date,payment_method,procedure_name,status) VALUES (?,?,?,?,?,?,?)";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, patientId);
            if (appointmentId != null) ps.setInt(2, appointmentId);
            else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, amount);
            ps.setString(4, date);
            ps.setString(5, method);
            ps.setString(6, procedure);
            ps.setString(7, status);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status=? WHERE payment_id=?";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Object[]> searchPayments(String keyword) {
        List<Object[]> list = new ArrayList<>();
        String sql = """
            SELECT py.payment_id, p.full_name, py.procedure_name,
                   py.amount, py.payment_date, py.payment_method, py.status
            FROM payments py
            JOIN patients p ON py.patient_id = p.patient_id
            WHERE p.full_name LIKE ? OR py.procedure_name LIKE ? OR py.payment_method LIKE ?
            ORDER BY py.payment_date DESC
            """;
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            String k = "%" + keyword + "%";
            ps.setString(1, k); ps.setString(2, k); ps.setString(3, k);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Object[]{
                    rs.getInt("payment_id"),
                    rs.getString("full_name"),
                    rs.getString("procedure_name"),
                    rs.getDouble("amount"),
                    rs.getString("payment_date"),
                    rs.getString("payment_method"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Object[]> getAllPayments() { return searchPayments(""); }

    // Summary by method for chart in report
    public Map<String, Double> getRevenueByMethod() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT payment_method, SUM(amount) as total FROM payments WHERE status='Paid' GROUP BY payment_method";
        try (Connection cn = DatabaseConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                map.put(rs.getString("payment_method"), rs.getDouble("total"));
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }
}