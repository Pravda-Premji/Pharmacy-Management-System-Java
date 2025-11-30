import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class PharmacyGUI {
    public static void main(String[] args) {
        // Create the main window
        JFrame frame = new JFrame("Pharmacy Management");
        frame.setSize(420, 360);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null); // simple absolute layout for quick UI

        // Buttons
        JButton addBtn = new JButton("Add Medicine");
        JButton viewBtn = new JButton("View Medicines");
        JButton exitBtn = new JButton("Exit");

        // Position and size: x, y, width, height
        addBtn.setBounds(110, 60, 200, 40);
        viewBtn.setBounds(110, 130, 200, 40);
        exitBtn.setBounds(110, 200, 200, 40);

        frame.add(addBtn);
        frame.add(viewBtn);
        frame.add(exitBtn);

        // Show window
        frame.setLocationRelativeTo(null); // centre on screen
        frame.setVisible(true);

        // --- Add Medicine action ---
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String name = JOptionPane.showInputDialog(frame, "Enter Medicine Name:");
                    if (name == null || name.trim().isEmpty()) return;

                    String priceStr = JOptionPane.showInputDialog(frame, "Enter Price:");
                    if (priceStr == null || priceStr.trim().isEmpty()) return;
                    double price = Double.parseDouble(priceStr);

                    String qtyStr = JOptionPane.showInputDialog(frame, "Enter Quantity:");
                    if (qtyStr == null || qtyStr.trim().isEmpty()) return;
                    int qty = Integer.parseInt(qtyStr);

                    Connection conn = DBConnection.getConnection();
                    if (conn == null) {
                        JOptionPane.showMessageDialog(frame, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String sql = "INSERT INTO medicines(name, price, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(sql)) {
                        ps.setString(1, name);
                        ps.setDouble(2, price);
                        ps.setInt(3, qty);
                        ps.executeUpdate();
                    }

                    JOptionPane.showMessageDialog(frame, "Medicine added successfully!");
                } catch (NumberFormatException nf) {
                    JOptionPane.showMessageDialog(frame, "Please enter valid numeric values for price and quantity.", "Input error", JOptionPane.WARNING_MESSAGE);
                } catch (SQLException sq) {
                    sq.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Database error: " + sq.getMessage(), "DB error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- View Medicines action ---
        viewBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    Connection conn = DBConnection.getConnection();
                    if (conn == null) {
                        JOptionPane.showMessageDialog(frame, "Database connection failed!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    StringBuilder sb = new StringBuilder();
                    String sql = "SELECT id, name, price, quantity FROM medicines";
                    try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            sb.append(rs.getInt("id")).append(" | ")
                              .append(rs.getString("name")).append(" | ")
                              .append(rs.getDouble("price")).append(" | ")
                              .append(rs.getInt("quantity")).append("\n");
                        }
                    }

                    if (sb.length() == 0) sb.append("No medicines found.");
                    JTextArea textArea = new JTextArea(sb.toString());
                    textArea.setEditable(false);
                    JScrollPane scroll = new JScrollPane(textArea);
                    scroll.setPreferredSize(new java.awt.Dimension(380, 220));
                    JOptionPane.showMessageDialog(frame, scroll, "Medicines", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException sq) {
                    sq.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Database error: " + sq.getMessage(), "DB error", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // --- Exit action ---
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
    }
}
