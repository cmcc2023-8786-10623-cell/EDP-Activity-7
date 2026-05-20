package databaseconnection;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.*;
import java.util.*;
import java.util.List;

public class ReportPanel extends JPanel {

    private User currentUser;
    private PatientDAO pDao = new PatientDAO();
    private AppointmentDAO aDao = new AppointmentDAO();
    private PaymentDAO pyDao = new PaymentDAO();
    private JComboBox<String> cmbType;
    private DefaultTableModel model;
    private JTable table;
    private List<Object[]> currentData = new ArrayList<>();
    private String[] currentCols;

    public ReportPanel(User user) {
        this.currentUser = user;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(java.awt.Color.WHITE);
        add(buildTop(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
    }

    private JPanel buildTop() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBackground(java.awt.Color.WHITE);
        p.setBorder(BorderFactory.createTitledBorder("Report Generator"));
        cmbType = new JComboBox<>(new String[]{"Patient Records", "Appointment Records", "Payment Records"});
        JButton btnGen = new JButton("Generate Report");
        styleBtn(btnGen, new java.awt.Color(25, 135, 84));
        btnGen.addActionListener(e -> generateReport());
        p.add(new JLabel("Report Type:"));
        p.add(cmbType);
        p.add(btnGen);
        return p;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Name", "Detail", "Date", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(24);
        return new JScrollPane(table);
    }

    private JPanel buildBottom() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        p.setBackground(java.awt.Color.WHITE);
        JButton btnExport = new JButton("📥 Export to Excel");
        styleBtn(btnExport, new java.awt.Color(13, 110, 253));
        btnExport.addActionListener(e -> exportToExcel());
        p.add(btnExport);
        return p;
    }

    private void generateReport() {
        model.setRowCount(0);
        model.setColumnCount(0);
        currentData.clear();
        String type = (String) cmbType.getSelectedItem();

        switch (type) {
            case "Patient Records" -> {
                currentCols = new String[]{"ID", "Full Name", "Birthdate", "Gender", "Contact", "Email", "Status"};
                for (String c : currentCols) model.addColumn(c);
                List<Object[]> rows = pDao.getAllPatients();
                for (Object[] r : rows) {
                    model.addRow(new Object[]{r[0], r[1], r[2], r[3], r[4], r[6], r[7]});
                    currentData.add(r);
                }
            }
            case "Appointment Records" -> {
                currentCols = new String[]{"ID", "Patient", "Date", "Time", "Procedure", "Dentist", "Status"};
                for (String c : currentCols) model.addColumn(c);
                List<Object[]> rows = aDao.getAllAppointments();
                for (Object[] r : rows) {
                    model.addRow(r);
                    currentData.add(r);
                }
            }
            case "Payment Records" -> {
                currentCols = new String[]{"ID", "Patient", "Procedure", "Amount (₱)", "Date", "Method", "Status"};
                for (String c : currentCols) model.addColumn(c);
                List<Object[]> rows = pyDao.getAllPayments();
                for (Object[] r : rows) {
                    model.addRow(r);
                    currentData.add(r);
                }
            }
        }
        JOptionPane.showMessageDialog(this, model.getRowCount() + " records loaded.");
    }

    private void exportToExcel() {
        if (currentData.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please generate a report with data first.");
            return;
        }

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(cmbType.getSelectedItem().toString() + "_Report.xlsx"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (XSSFWorkbook wb = new XSSFWorkbook()) {
                XSSFSheet sheet1 = wb.createSheet("Report");
                buildSheet1(wb, sheet1);
                
                // Add Chart Data Sheet
                XSSFSheet sheet2 = wb.createSheet("Chart Data");
                buildSheet2(wb, sheet2);

                try (FileOutputStream fos = new FileOutputStream(fc.getSelectedFile())) {
                    wb.write(fos);
                }
                JOptionPane.showMessageDialog(this, "Export Successful!");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }

    private void buildSheet1(XSSFWorkbook wb, XSSFSheet sheet) {
        // Create Header Style
        XSSFCellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 25, (byte) 135, (byte) 84}, null));
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // Title Row
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("DentaSync Dental Clinic - " + cmbType.getSelectedItem());
        cell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, currentCols.length - 1));

        // Column Headers
        Row colRow = sheet.createRow(2);
        for (int i = 0; i < currentCols.length; i++) {
            colRow.createCell(i).setCellValue(currentCols[i]);
        }

        // Data Rows
        int rowIdx = 3;
        for (Object[] dataRow : currentData) {
            Row r = sheet.createRow(rowIdx++);
            for (int i = 0; i < dataRow.length; i++) {
                r.createCell(i).setCellValue(String.valueOf(dataRow[i]));
            }
        }

        // Signature
        Row sigRow = sheet.createRow(rowIdx + 2);
        sigRow.createCell(0).setCellValue("Generated by: " + currentUser.getFullName());
    }

    private void buildSheet2(XSSFWorkbook wb, XSSFSheet sheet) {
        Row r = sheet.createRow(0);
        r.createCell(0).setCellValue("Summary Category");
        r.createCell(1).setCellValue("Count");
        // Logic for summary counts goes here based on currentData
    }

    private void styleBtn(JButton b, java.awt.Color bg) {
        b.setBackground(bg);
        b.setForeground(java.awt.Color.BLACK);
        b.setFocusPainted(false);
        b.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 13));
    }
}