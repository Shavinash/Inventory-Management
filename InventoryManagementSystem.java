import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class InventoryManagementSystem extends JFrame {

    // JDBC connection details
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ims_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "shavinashsankar@2005";

    private DefaultTableModel tableModel;
    private JTable table;
    private JTextArea auditArea;
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Input fields
    private JTextField tfName = new JTextField(10);
    private JTextField tfSKU = new JTextField(10);
    private JTextField tfCategory = new JTextField(10);
    private JTextField tfQuantity = new JTextField(10);
    private JTextField tfSupplier = new JTextField(10);
    private JTextField tfPrice = new JTextField(10);
    private JTextField tfLocation = new JTextField(10);

    private Connection con;

    public InventoryManagementSystem() {
        // GUI setup
        setTitle("Inventory Management System");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Connect to database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to MySQL successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed:\n" + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Table setup
        String[] columns = {"Product", "SKU", "Category", "Quantity", "Supplier", "Price", "Location"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane tablePane = new JScrollPane(table);

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 7, 5, 5));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Inventory Details"));
        inputPanel.add(new JLabel("Product Name:")); inputPanel.add(tfName);
        inputPanel.add(new JLabel("SKU:")); inputPanel.add(tfSKU);
        inputPanel.add(new JLabel("Category:")); inputPanel.add(tfCategory);
        inputPanel.add(new JLabel("Quantity:")); inputPanel.add(tfQuantity);
        inputPanel.add(new JLabel("Supplier:")); inputPanel.add(tfSupplier);
        inputPanel.add(new JLabel("Price:")); inputPanel.add(tfPrice);
        inputPanel.add(new JLabel("Location:")); inputPanel.add(tfLocation);

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        JButton btnReport = new JButton("Show Stock Report");
        JButton btnAudit = new JButton("View Audit Trail");
        buttonPanel.add(btnAdd); buttonPanel.add(btnUpdate); buttonPanel.add(btnDelete);
        buttonPanel.add(btnReport); buttonPanel.add(btnAudit);

        // Audit area
        auditArea = new JTextArea(15, 30);
        auditArea.setEditable(false);
        JScrollPane auditScroll = new JScrollPane(auditArea);
        auditScroll.setBorder(BorderFactory.createTitledBorder("Audit Trail"));

        add(inputPanel, BorderLayout.NORTH);
        add(tablePane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(auditScroll, BorderLayout.EAST);

        // Load inventory
        loadInventory();

        // Button actions
        btnAdd.addActionListener(e -> addItem());
        btnUpdate.addActionListener(e -> updateItem());
        btnDelete.addActionListener(e -> deleteItem());
        btnReport.addActionListener(e -> showReport());
        btnAudit.addActionListener(e -> viewAuditTrail());

        setVisible(true);
    }

    private void loadInventory() {
        tableModel.setRowCount(0);
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM inventory");
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getString("product_name"),
                        rs.getString("sku"),
                        rs.getString("category"),
                        rs.getInt("quantity"),
                        rs.getString("supplier"),
                        rs.getDouble("price"),
                        rs.getString("location")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addItem() {
        try {
            String name = tfName.getText();
            String sku = tfSKU.getText();
            String category = tfCategory.getText();
            int qty = Integer.parseInt(tfQuantity.getText());
            String supplier = tfSupplier.getText();
            double price = Double.parseDouble(tfPrice.getText());
            String loc = tfLocation.getText();

            // Check duplicate SKU
            PreparedStatement check = con.prepareStatement("SELECT sku FROM inventory WHERE sku=?");
            check.setString(1, sku);
            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "SKU already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO inventory (product_name, sku, category, quantity, supplier, price, location) VALUES (?,?,?,?,?,?,?)"
            );
            ps.setString(1, name); ps.setString(2, sku); ps.setString(3, category);
            ps.setInt(4, qty); ps.setString(5, supplier); ps.setDouble(6, price); ps.setString(7, loc);
            ps.executeUpdate();

            tableModel.addRow(new Object[]{name, sku, category, qty, supplier, price, loc});
            log("Added item: " + sku);
            JOptionPane.showMessageDialog(this, "Item added successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input or database error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateItem() {
        String sku = tfSKU.getText();
        try {
            PreparedStatement check = con.prepareStatement("SELECT * FROM inventory WHERE sku=?");
            check.setString(1, sku);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "SKU not found!");
                return;
            }

            int qty = Integer.parseInt(tfQuantity.getText());
            double price = Double.parseDouble(tfPrice.getText());
            String loc = tfLocation.getText();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE inventory SET quantity=?, price=?, location=? WHERE sku=?"
            );
            ps.setInt(1, qty); ps.setDouble(2, price); ps.setString(3, loc); ps.setString(4, sku);
            ps.executeUpdate();

            loadInventory();
            log("Updated item: " + sku);
            JOptionPane.showMessageDialog(this, "Item updated successfully!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid input or database error:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteItem() {
        String sku = tfSKU.getText();
        try {
            PreparedStatement check = con.prepareStatement("SELECT * FROM inventory WHERE sku=?");
            check.setString(1, sku);
            ResultSet rs = check.executeQuery();
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "SKU not found!");
                return;
            }

            PreparedStatement ps = con.prepareStatement("DELETE FROM inventory WHERE sku=?");
            ps.setString(1, sku);
            ps.executeUpdate();

            loadInventory();
            log("Deleted item: " + sku);
            JOptionPane.showMessageDialog(this, "Item deleted successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showReport() {
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT SUM(quantity) as totalQty, SUM(quantity*price) as totalValue FROM inventory");
            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Total Items: " + tableModel.getRowCount() +
                                "\nTotal Quantity: " + rs.getInt("totalQty") +
                                "\nTotal Inventory Value: " + String.format("%.2f", rs.getDouble("totalValue")),
                        "Stock Report", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void viewAuditTrail() {
        auditArea.setText("");
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM audit ORDER BY timestamp DESC");
            while (rs.next()) {
                auditArea.append("[" + rs.getString("timestamp") + "] " + rs.getString("action") + "\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void log(String message) {
        auditArea.append("[" + dtf.format(LocalDateTime.now()) + "] " + message + "\n");
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO audit (action) VALUES (?)");
            ps.setString(1, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(InventoryManagementSystem::new);
    }
}





