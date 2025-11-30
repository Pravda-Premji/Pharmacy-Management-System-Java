import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MedicineManager extends JFrame {

    private JTextField nameField, priceField, quantityField; // Input fields for adding medicine
    private JTable table; // Table to display all medicines
    private DefaultTableModel tableModel; // Table model

    public MedicineManager() {
        setTitle("Pharmacy Management - Medicine Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Top panel: form to add a new medicine ---
        JPanel addPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add Medicine"));

        addPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        addPanel.add(nameField);

        addPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        addPanel.add(priceField);

        addPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField();
        addPanel.add(quantityField);

        JButton addButton = new JButton("Add Medicine");
        addPanel.add(new JLabel()); // empty space
        addPanel.add(addButton);

        // --- Table to display medicines from database ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Price", "Quantity"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);

        // --- Delete button ---
        JButton deleteButton = new JButton("Delete Selected");

        // --- Layout ---
        setLayout(new BorderLayout());
        add(addPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(deleteButton, BorderLayout.SOUTH);

        // --- Load table initially ---
        loadMedicines();

        // --- Add medicine action ---
        addButton.addActionListener(e -> addMedicine());

        // --- Delete selected medicine action ---
        deleteButton.addActionListener(e -> deleteSelectedMedicine());
    }

    // --- Load all medicines from DB into the table ---
    private void loadMedicines() {
        tableModel.setRowCount(0); // clear the table first
        try (Connection conn = DBConnection.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM medicine");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading medicines: " + e.getMessage());
        }
    }

    // --- Add new medicine to DB ---
    private void addMedicine() {
        String name = nameField.getText();
        String priceText = priceField.getText();
        String quantityText = quantityField.getText();

        if (name.isEmpty() || priceText.isEmpty() || quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields!");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            int quantity = Integer.parseInt(quantityText);

            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO medicine (name, price, quantity) VALUES (?, ?, ?)");
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setInt(3, quantity);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Medicine added successfully!");
                loadMedicines(); // refresh table
                nameField.setText("");
                priceField.setText("");
                quantityField.setText("");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Price and Quantity must be numbers!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error adding medicine: " + ex.getMessage());
        }
    }

    // --- Delete selected medicine ---
    private void deleteSelectedMedicine() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medicine to delete!");
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0); // get ID of selected medicine

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this medicine?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM medicine WHERE id=?");
                ps.setInt(1, id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Medicine deleted successfully!");
                loadMedicines(); // refresh table
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting medicine: " + e.getMessage());
            }
        }
    }
}
