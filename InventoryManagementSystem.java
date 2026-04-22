import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// ================================================================
//  PRODUCT MODEL
// ================================================================
class Product {
    private static int counter = 1;
    private final int id;
    private String name, category;
    private double costPrice, sellingPrice;
    private int stock, totalPurchased, totalShipped;

    public Product(String name, String category, double costPrice, double sellingPrice, int initialStock) {
        this.id             = counter++;
        this.name           = name;
        this.category       = category;
        this.costPrice      = costPrice;
        this.sellingPrice   = sellingPrice;
        this.stock          = initialStock;
        this.totalPurchased = initialStock;
        this.totalShipped   = 0;
    }

    public int    getId()             { return id; }
    public String getName()           { return name; }
    public String getCategory()       { return category; }
    public double getCostPrice()      { return costPrice; }
    public double getSellingPrice()   { return sellingPrice; }
    public int    getStock()          { return stock; }
    public int    getTotalPurchased() { return totalPurchased; }
    public int    getTotalShipped()   { return totalShipped; }

    public void setName(String n)         { this.name = n; }
    public void setCategory(String c)     { this.category = c; }
    public void setCostPrice(double p)    { this.costPrice = p; }
    public void setSellingPrice(double p) { this.sellingPrice = p; }

    public boolean purchase(int qty) {
        if (qty <= 0) return false;
        stock += qty;
        totalPurchased += qty;
        return true;
    }

    public boolean ship(int qty) {
        if (qty <= 0 || qty > stock) return false;
        stock -= qty;
        totalShipped += qty;
        return true;
    }

    public double getRevenue()    { return totalShipped * sellingPrice; }
    public double getTotalCost()  { return totalPurchased * costPrice; }
    public double getProfitLoss() { return getRevenue() - getTotalCost(); }

    @Override public String toString() { return name + "  (ID: " + id + ")"; }
}

// ================================================================
//  MAIN APPLICATION
// ================================================================
public class InventoryManagementSystem extends JFrame {

    // -- Colors --
    private static final Color C_BG      = new Color(245, 247, 250);
    private static final Color C_SIDEBAR = new Color(22,  33,  55);
    private static final Color C_LOGO    = new Color(15,  23,  42);
    private static final Color C_ACCENT  = new Color(31,  78, 194);
    private static final Color C_GREEN   = new Color(22, 120,  85);
    private static final Color C_DANGER  = new Color(181, 29,  45);
    private static final Color C_ORANGE  = new Color(170, 64,   8);
    private static final Color C_TMAIN   = new Color(15,   23,  42);
    private static final Color C_TSUB    = new Color(100, 116, 139);
    private static final Color C_WHITE   = Color.WHITE;
    private static final Color C_ROWALT  = new Color(249, 250, 252);
    private static final Color C_BORDER  = new Color(226, 232, 240);
    private static final Color C_NAVACT  = new Color(40,   58,  90);
    private static final Color C_NAVHOV  = new Color(30,   44,  70);
    private static final Color C_NAVTXT  = new Color(180, 194, 220);

    // -- Fonts --
    private static final Font F_TITLE = new Font("Segoe UI", Font.BOLD,  20);
    private static final Font F_HEAD  = new Font("Segoe UI", Font.BOLD,  13);
    private static final Font F_BODY  = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font F_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    private static final Font F_NAV   = new Font("Segoe UI", Font.BOLD,  13);
    private static final DecimalFormat FMT = createCurrencyFormat();

    private static DecimalFormat createCurrencyFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setCurrencySymbol("Rs.");
        return new DecimalFormat("¤ #,##0.00", symbols);
    }

    // -- State --
    private final java.util.List<Product> products = new ArrayList<>();
    private final java.util.List<JButton> navBtns  = new ArrayList<>();
    private JPanel mainArea;
    private JLabel statusBar;
    private int    activeNav = 0;

    // ============================================================
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new InventoryManagementSystem().setVisible(true));
    }

    public InventoryManagementSystem() {
        seedData();
        setTitle("Inventory Management System  --  MNI Project 2");
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(0, 0));

        add(buildSidebar(),   BorderLayout.WEST);

        mainArea = new JPanel(new BorderLayout());
        mainArea.setBackground(C_BG);
        add(mainArea, BorderLayout.CENTER);

        add(buildStatusBar(), BorderLayout.SOUTH);

        navigateTo(0);
    }

    // ============================================================
    //  SEED DATA
    // ============================================================
    private void seedData() {
        products.add(new Product("Wireless Mouse",    "Electronics", 350,  599,  50));
        products.add(new Product("USB-C Hub",         "Electronics", 700, 1199,  30));
        products.add(new Product("Notebook A4",       "Stationery",   25,   49, 200));
        products.add(new Product("HDMI Cable 2m",     "Electronics", 180,  299,  75));
        products.add(new Product("Whiteboard Marker", "Stationery",   12,   25, 300));
        products.get(0).ship(12);
        products.get(1).ship(8);
        products.get(2).ship(60);
        products.get(3).ship(20);
        products.get(4).ship(90);
    }

    // ============================================================
    //  NAVIGATE  --  rebuilds the page fresh every time
    // ============================================================
    private void navigateTo(int index) {
        activeNav = index;

        // Update nav highlight
        for (int i = 0; i < navBtns.size(); i++) {
            JButton b = navBtns.get(i);
            boolean active = (i == index);
            b.setBackground(active ? new Color(40, 40, 40) : new Color(20, 20, 20));
            b.setForeground(C_WHITE);
        }

        // Build fresh page
        JPanel page;
        switch (index) {
            case 0:  page = buildListPage();     break;
            case 1:  page = buildInfoPage();     break;
            case 2:  page = buildPurchasePage(); break;
            case 3:  page = buildShippingPage(); break;
            case 4:  page = buildBalancePage();  break;
            case 5:  page = buildPLPage();       break;
            default: return;
        }

        mainArea.removeAll();
        mainArea.add(page, BorderLayout.CENTER);
        mainArea.revalidate();
        mainArea.repaint();
    }

    // ============================================================
    //  SIDEBAR
    // ============================================================
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(C_SIDEBAR);
        side.setPreferredSize(new Dimension(215, 0));

        // Logo strip
        JPanel logo = new JPanel(new BorderLayout());
        logo.setBackground(C_LOGO);
        logo.setMaximumSize (new Dimension(215, 65));
        logo.setMinimumSize (new Dimension(215, 65));
        logo.setPreferredSize(new Dimension(215, 65));
        JLabel logoLbl = new JLabel("  InvenTrack");
        logoLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        logoLbl.setForeground(C_WHITE);
        logoLbl.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 0));
        logo.add(logoLbl, BorderLayout.CENTER);
        side.add(logo);

        side.add(Box.createVerticalStrut(12));

        // Section label
        JLabel sec = new JLabel("  NAVIGATION");
        sec.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sec.setForeground(new Color(80, 100, 130));
        sec.setMaximumSize(new Dimension(215, 22));
        side.add(sec);
        side.add(Box.createVerticalStrut(4));

        // Nav buttons
        String[] labels = {
            "    All Products",
            "    Product Info",
            "    Purchase",
            "    Shipping",
            "    Balance Stock",
            "    Profit & Loss"
        };
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            JButton btn = new JButton(labels[i]);
            btn.setFont(F_NAV);
            btn.setForeground(C_WHITE);
            btn.setBackground(new Color(20, 20, 20));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setContentAreaFilled(true);
            btn.setOpaque(true);
            btn.setMaximumSize (new Dimension(215, 44));
            btn.setMinimumSize (new Dimension(215, 44));
            btn.setPreferredSize(new Dimension(215, 44));
            btn.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> navigateTo(idx));
            btn.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    if (activeNav != idx) btn.setBackground(new Color(40, 40, 40));
                }
                public void mouseExited(MouseEvent e) {
                    if (activeNav != idx) btn.setBackground(new Color(20, 20, 20));
                }
            });
            navBtns.add(btn);
            side.add(btn);
            side.add(Box.createVerticalStrut(2));
        }

        side.add(Box.createVerticalGlue());

        // Add product button at bottom
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 12));
        wrap.setBackground(C_SIDEBAR);
        wrap.setMaximumSize(new Dimension(215, 62));
        JButton addBtn = mkBtn("+ Add Product", C_ACCENT);
        addBtn.setPreferredSize(new Dimension(178, 38));
        addBtn.addActionListener(e -> showAddDialog());
        wrap.add(addBtn);
        side.add(wrap);

        return side;
    }

    // ============================================================
    //  STATUS BAR
    // ============================================================
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 5));
        bar.setBackground(new Color(235, 238, 245));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, C_BORDER));
        statusBar = new JLabel("Ready.");
        statusBar.setFont(F_SMALL);
        statusBar.setForeground(C_TSUB);
        bar.add(statusBar);
        return bar;
    }

    private void setStatus(String msg) { statusBar.setText(msg); }

    // ============================================================
    //  PAGE A  --  ALL PRODUCTS
    // ============================================================
    private JPanel buildListPage() {
        JPanel page = makePage("All Products",
            "Complete product catalogue  --  " + products.size() + " item(s)");
        JPanel body = getBody(page);

        String[] cols = {"ID", "Name", "Category", "Cost Price", "Sell Price", "Stock", "Status"};
        Object[][] rows = new Object[products.size()][7];
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            rows[i] = new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                FMT.format(p.getCostPrice()), FMT.format(p.getSellingPrice()),
                p.getStock(), stockTag(p.getStock())
            };
        }

        DefaultTableModel mdl = noEditModel(rows, cols);
        JTable table = styledTable(mdl);

        // Color status column
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());

        // Bottom actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actions.setBackground(C_BG);
        actions.setOpaque(true);
        JButton delBtn = mkBtn("Delete Selected", C_DANGER);
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { err("Select a product row to delete."); return; }
            int id     = (int) table.getValueAt(row, 0);
            String nm  = (String) table.getValueAt(row, 1);
            int ans = JOptionPane.showConfirmDialog(this,
                "Delete '" + nm + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                products.removeIf(p -> p.getId() == id);
                navigateTo(0);
                setStatus("Deleted: " + nm);
            }
        });
        actions.add(delBtn);

        body.add(wrapScroll(table), BorderLayout.CENTER);
        body.add(actions,           BorderLayout.SOUTH);
        return page;
    }

    // ============================================================
    //  PAGE B  --  PRODUCT INFO
    // ============================================================
    private JPanel buildInfoPage() {
        JPanel page = makePage("Product Information",
            "Select a product to view its full details");
        JPanel body = getBody(page);

        if (products.isEmpty()) {
            body.add(emptyMsg("No products yet. Use 'Add Product' to get started."), BorderLayout.CENTER);
            return page;
        }

        // Top selector
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setBackground(C_BG);
        top.setOpaque(true);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));
        JLabel lbl = new JLabel("Select Product:");
        lbl.setFont(F_HEAD);
        lbl.setForeground(C_TSUB);
        JComboBox<Product> combo = new JComboBox<>(products.toArray(new Product[0]));
        combo.setFont(F_BODY);
        combo.setPreferredSize(new Dimension(260, 34));
        JButton viewBtn = mkBtn("View Details", C_ACCENT);
        top.add(lbl);
        top.add(combo);
        top.add(viewBtn);

        // Detail card
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 24, 20, 24)));

        String[] keys = {"Product ID", "Name", "Category",
                         "Cost Price", "Selling Price", "Current Stock", "Stock Status"};
        JLabel[] vals = new JLabel[keys.length];
        GridBagConstraints gc = makeGBC();
        for (int i = 0; i < keys.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0;
            JLabel k = new JLabel(keys[i] + ":");
            k.setFont(F_HEAD);
            k.setForeground(C_TSUB);
            card.add(k, gc);
            gc.gridx = 1; gc.weightx = 1;
            vals[i] = new JLabel("--");
            vals[i].setFont(F_BODY);
            vals[i].setForeground(C_TMAIN);
            card.add(vals[i], gc);
        }

        Runnable fill = () -> {
            Product p = (Product) combo.getSelectedItem();
            if (p == null) return;
            vals[0].setText(String.valueOf(p.getId()));
            vals[1].setText(p.getName());
            vals[2].setText(p.getCategory());
            vals[3].setText(FMT.format(p.getCostPrice()));
            vals[4].setText(FMT.format(p.getSellingPrice()));
            vals[5].setText(String.valueOf(p.getStock()));
            String tag = stockTag(p.getStock());
            vals[6].setText(tag);
            vals[6].setFont(F_HEAD);
            vals[6].setForeground(
                tag.equals("In Stock")     ? C_GREEN  :
                tag.equals("Low Stock")    ? C_ORANGE : C_DANGER);
            setStatus("Viewing: " + p.getName());
        };

        viewBtn.addActionListener(e -> fill.run());
        combo.addActionListener(e  -> fill.run());
        fill.run(); // auto-show first item

        body.add(top,  BorderLayout.NORTH);
        body.add(card, BorderLayout.CENTER);
        return page;
    }

    // ============================================================
    //  PAGE C  --  PURCHASE
    // ============================================================
    private JPanel buildPurchasePage() {
        JPanel page = makePage("Purchase", "Record incoming stock for a product");
        JPanel body = getBody(page);

        if (products.isEmpty()) {
            body.add(emptyMsg("No products. Add a product first."), BorderLayout.CENTER);
            return page;
        }

        JPanel card = formCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = makeGBC();

        JComboBox<Product> combo = new JComboBox<>(products.toArray(new Product[0]));
        combo.setFont(F_BODY);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        qty.setFont(F_BODY);
        fixSpin(qty);

        JLabel hint = new JLabel("Current stock: "
            + (products.isEmpty() ? "--" : products.get(0).getStock()));
        hint.setFont(F_SMALL);
        hint.setForeground(C_TSUB);

        combo.addActionListener(e -> {
            Product p = (Product) combo.getSelectedItem();
            if (p != null) hint.setText("Current stock: " + p.getStock());
        });

        addRow(card, gc, 0, "Product:",         combo);
        addRow(card, gc, 1, "Quantity to Add:", qty);

        gc.gridx = 1; gc.gridy = 2;
        gc.insets = new Insets(2, 12, 14, 12);
        card.add(hint, gc);

        JLabel feedback = new JLabel(" ");
        feedback.setFont(F_BODY);

        JButton btn = mkBtn("Confirm Purchase", C_GREEN);
        btn.addActionListener(e -> {
            Product p = (Product) combo.getSelectedItem();
            if (p == null) { err("Please select a product."); return; }
            int q = (int) qty.getValue();
            p.purchase(q);
            hint.setText("Current stock: " + p.getStock());
            feedback.setForeground(C_GREEN);
            feedback.setText("Added " + q + " units to '" + p.getName()
                + "'. New stock: " + p.getStock());
            setStatus("Purchase: +" + q + " to " + p.getName());
        });

        gc.gridx = 1; gc.gridy = 3;
        gc.insets = new Insets(18, 12, 8, 12);
        card.add(btn, gc);
        gc.gridy = 4;
        gc.insets = new Insets(6, 12, 8, 12);
        card.add(feedback, gc);

        body.add(card, BorderLayout.NORTH);
        return page;
    }

    // ============================================================
    //  PAGE D  --  SHIPPING
    // ============================================================
    private JPanel buildShippingPage() {
        JPanel page = makePage("Shipping", "Dispatch stock -- deducts units from inventory");
        JPanel body = getBody(page);

        if (products.isEmpty()) {
            body.add(emptyMsg("No products. Add a product first."), BorderLayout.CENTER);
            return page;
        }

        JPanel card = formCard();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = makeGBC();

        JComboBox<Product> combo = new JComboBox<>(products.toArray(new Product[0]));
        combo.setFont(F_BODY);
        JSpinner qty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));
        qty.setFont(F_BODY);
        fixSpin(qty);

        JLabel hint = new JLabel("Available stock: "
            + (products.isEmpty() ? "--" : products.get(0).getStock()));
        hint.setFont(F_SMALL);
        hint.setForeground(C_TSUB);

        combo.addActionListener(e -> {
            Product p = (Product) combo.getSelectedItem();
            if (p != null) hint.setText("Available stock: " + p.getStock());
        });

        addRow(card, gc, 0, "Product:",           combo);
        addRow(card, gc, 1, "Quantity to Ship:", qty);

        gc.gridx = 1; gc.gridy = 2;
        gc.insets = new Insets(2, 12, 14, 12);
        card.add(hint, gc);

        JLabel feedback = new JLabel(" ");
        feedback.setFont(F_BODY);

        JButton btn = mkBtn("Confirm Shipping", C_ACCENT);
        btn.addActionListener(e -> {
            Product p = (Product) combo.getSelectedItem();
            if (p == null) { err("Please select a product."); return; }
            int q = (int) qty.getValue();
            if (q > p.getStock()) {
                feedback.setForeground(C_DANGER);
                feedback.setText("Error: Only " + p.getStock()
                    + " units available. Cannot ship " + q + ".");
                return;
            }
            p.ship(q);
            hint.setText("Available stock: " + p.getStock());
            feedback.setForeground(C_ACCENT);
            feedback.setText("Shipped " + q + " units of '" + p.getName()
                + "'. Remaining: " + p.getStock());
            setStatus("Shipping: -" + q + " from " + p.getName());
        });

        gc.gridx = 1; gc.gridy = 3;
        gc.insets = new Insets(18, 12, 8, 12);
        card.add(btn, gc);
        gc.gridy = 4;
        gc.insets = new Insets(6, 12, 8, 12);
        card.add(feedback, gc);

        body.add(card, BorderLayout.NORTH);
        return page;
    }

    // ============================================================
    //  PAGE E  --  BALANCE STOCK
    // ============================================================
    private JPanel buildBalancePage() {
        JPanel page = makePage("Balance Stock",
            "Real-time stock levels across all products");
        JPanel body = getBody(page);

        String[] cols = {"ID", "Product Name", "Category",
                         "Total Purchased", "Total Shipped", "Balance Stock", "Status"};
        Object[][] rows = new Object[products.size()][7];
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            rows[i] = new Object[]{
                p.getId(), p.getName(), p.getCategory(),
                p.getTotalPurchased(), p.getTotalShipped(),
                p.getStock(), stockTag(p.getStock())
            };
        }

        DefaultTableModel mdl = noEditModel(rows, cols);
        JTable table = styledTable(mdl);

        // Balance Stock column
        table.getColumnModel().getColumn(5).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int row, int col) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(
                                    t, v, sel, foc, row, col);
                    int n = (int) v;
                    l.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    l.setHorizontalAlignment(CENTER);
                    if (!sel) {
                        l.setBackground(row % 2 == 0 ? C_WHITE : C_ROWALT);
                        l.setForeground(n == 0 ? C_DANGER : n <= 10 ? C_ORANGE : C_GREEN);
                    }
                    return l;
                }
            }
        );

        // Status column
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());

        body.add(wrapScroll(table), BorderLayout.CENTER);
        return page;
    }

    // ============================================================
    //  PAGE F  --  PROFIT & LOSS
    // ============================================================
    private JPanel buildPLPage() {
        JPanel page = makePage("Profit & Loss",
            "Financial summary -- revenue, cost, and net result per product");
        JPanel body = getBody(page);

        double totalRev  = products.stream().mapToDouble(Product::getRevenue).sum();
        double totalCost = products.stream().mapToDouble(Product::getTotalCost).sum();
        double net       = totalRev - totalCost;

        // Summary cards
        JPanel kpiRow = new JPanel(new GridLayout(1, 3, 14, 0));
        kpiRow.setOpaque(false);
        kpiRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        kpiRow.add(kpiCard("Total Revenue",  FMT.format(totalRev),        C_ACCENT));
        kpiRow.add(kpiCard("Total Cost",     FMT.format(totalCost),       C_ORANGE));
        kpiRow.add(kpiCard(net >= 0 ? "Net Profit" : "Net Loss",
                           FMT.format(Math.abs(net)),
                           net >= 0 ? C_GREEN : C_DANGER));

        // Table
        String[] cols = {"ID", "Product", "Purchased", "Shipped",
                         "Revenue", "Cost", "P/L Amount", "Result"};
        Object[][] rows = new Object[products.size()][8];
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            double pl = p.getProfitLoss();
            rows[i] = new Object[]{
                p.getId(), p.getName(),
                p.getTotalPurchased(), p.getTotalShipped(),
                FMT.format(p.getRevenue()), FMT.format(p.getTotalCost()),
                FMT.format(Math.abs(pl)),
                pl >= 0 ? "Profit" : "Loss"
            };
        }

        DefaultTableModel mdl = noEditModel(rows, cols);
        JTable table = styledTable(mdl);

        // Result column
        table.getColumnModel().getColumn(7).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean foc, int row, int col) {
                    JLabel l = (JLabel) super.getTableCellRendererComponent(
                                    t, v, sel, foc, row, col);
                    l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    l.setHorizontalAlignment(CENTER);
                    if (!sel) {
                        l.setBackground(row % 2 == 0 ? C_WHITE : C_ROWALT);
                        l.setForeground("Profit".equals(v) ? C_GREEN : C_DANGER);
                    }
                    return l;
                }
            }
        );

        JPanel content = new JPanel(new BorderLayout(0, 0));
        content.setOpaque(false);
        content.add(kpiRow,          BorderLayout.NORTH);
        content.add(wrapScroll(table), BorderLayout.CENTER);

        body.add(content, BorderLayout.CENTER);
        return page;
    }

    // ============================================================
    //  ADD PRODUCT DIALOG
    // ============================================================
    private void showAddDialog() {
        JDialog dlg = new JDialog(this, "Add New Product", true);
        dlg.setSize(430, 390);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(C_BG);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_BG);
        form.setBorder(BorderFactory.createEmptyBorder(22, 26, 10, 26));
        GridBagConstraints gc = makeGBC();

        JTextField nameF = field(220);
        JTextField catF  = field(220);
        JTextField costF = field(220); costF.setText("0");
        JTextField sellF = field(220); sellF.setText("0");
        JSpinner   stk   = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
        stk.setFont(F_BODY);
        fixSpin(stk);

        addRow(form, gc, 0, "Product Name *:",   nameF);
        addRow(form, gc, 1, "Category:",         catF);
        addRow(form, gc, 2, "Cost Price (Rs.):", costF);
        addRow(form, gc, 3, "Sell Price (Rs.):", sellF);
        addRow(form, gc, 4, "Initial Stock:",    stk);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        btns.setBackground(C_BG);

        JButton cancel = new JButton("Cancel");
        cancel.setFont(F_BODY);
        cancel.setBackground(new Color(20, 20, 20));
        cancel.setForeground(C_WHITE);
        cancel.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        cancel.setFocusPainted(false);
        cancel.setContentAreaFilled(true);
        cancel.setOpaque(true);
        cancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancel.addActionListener(e -> dlg.dispose());

        JButton save = mkBtn("Save Product", C_ACCENT);
        save.addActionListener(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) { err("Product name is required."); return; }
            String cat = catF.getText().trim();
            if (cat.isEmpty()) cat = "General";
            try {
                double cost = Double.parseDouble(costF.getText().trim());
                double sell = Double.parseDouble(sellF.getText().trim());
                int    qty  = (int) stk.getValue();
                products.add(new Product(name, cat, cost, sell, qty));
                dlg.dispose();
                navigateTo(0);
                setStatus("Product added: " + name);
            } catch (NumberFormatException ex) {
                err("Enter valid numbers for cost and sell price.");
            }
        });

        btns.add(cancel);
        btns.add(save);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    // ============================================================
    //  UI UTILITY METHODS
    // ============================================================

    /** Creates a page with header. Body panel is named "__body__". */
    private JPanel makePage(String title, String subtitle) {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setBackground(C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(28, 28, 20, 28));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));

        JLabel t = new JLabel(title);
        t.setFont(F_TITLE);
        t.setForeground(C_TMAIN);
        JLabel s = new JLabel(subtitle);
        s.setFont(F_SMALL);
        s.setForeground(C_TSUB);
        header.add(t);
        header.add(Box.createVerticalStrut(3));
        header.add(s);

        JPanel body = new JPanel(new BorderLayout(0, 8));
        body.setOpaque(false);
        body.setName("__body__");

        panel.add(header, BorderLayout.NORTH);
        panel.add(body,   BorderLayout.CENTER);
        return panel;
    }

    /** Extracts the body panel from a page panel. */
    private JPanel getBody(JPanel page) {
        for (Component c : page.getComponents())
            if (c instanceof JPanel && "__body__".equals(((JPanel)c).getName()))
                return (JPanel) c;
        return page;
    }

    private DefaultTableModel noEditModel(Object[][] rows, String[] cols) {
        return new DefaultTableModel(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(F_BODY);
        t.setRowHeight(36);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(219, 234, 254));
        t.setSelectionForeground(C_TMAIN);
        t.setBackground(C_WHITE);
        t.setForeground(C_TMAIN);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(F_HEAD);
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(C_TSUB);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 38));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                                tbl, v, sel, foc, row, col);
                if (!sel) {
                    l.setBackground(row % 2 == 0 ? C_WHITE : C_ROWALT);
                    l.setForeground(C_TMAIN);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        });
        return t;
    }

    private JScrollPane wrapScroll(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(C_BORDER));
        sp.getViewport().setBackground(C_WHITE);
        return sp;
    }

    private JPanel formCard() {
        JPanel card = new JPanel();
        card.setBackground(C_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        return card;
    }

    private JPanel kpiCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(C_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER, 1),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        JLabel t = new JLabel(title); t.setFont(F_SMALL); t.setForeground(C_TSUB);
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 18));
        v.setForeground(accent);
        card.add(t, BorderLayout.NORTH);
        card.add(v, BorderLayout.CENTER);
        return card;
    }

    private JButton mkBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(F_HEAD);
        
        // 1. Actually use the passed background color
        btn.setBackground(bg); 
        btn.setForeground(C_WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        btn.setFocusPainted(false);
        
        // 2. CRITICAL: Stop the OS LookAndFeel from painting the default white/grey button
        btn.setBorderPainted(false); 
        
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // 3. Create a dynamic hover color (makes it slightly darker than whatever 'bg' is)
        Color hoverColor = new Color(
            Math.max(bg.getRed() - 30, 0),
            Math.max(bg.getGreen() - 30, 0),
            Math.max(bg.getBlue() - 30, 0)
        );

        // 4. Update the hover listeners to use the correct colors
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(hoverColor); }
            public void mouseExited (MouseEvent e) { btn.setBackground(bg); }
        });
        
        return btn;
    }

    private JTextField field(int width) {
        JTextField f = new JTextField();
        f.setFont(F_BODY);
        f.setPreferredSize(new Dimension(width, 32));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(C_BORDER),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private GridBagConstraints makeGBC() {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(8, 12, 8, 12);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;
        return gc;
    }

    private void addRow(JPanel form, GridBagConstraints gc,
                        int row, String labelText, Component field) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel l = new JLabel(labelText);
        l.setFont(F_HEAD);
        l.setForeground(C_TSUB);
        form.add(l, gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(field, gc);
    }

    private void fixSpin(JSpinner s) {
        JComponent editor = s.getEditor();
        if (editor instanceof JSpinner.DefaultEditor)
            ((JSpinner.DefaultEditor) editor).getTextField().setColumns(8);
    }

    private String stockTag(int qty) {
        if (qty == 0) return "Out of Stock";
        if (qty <= 10) return "Low Stock";
        return "In Stock";
    }

    /** Shared renderer for stock/status columns */
    private TableCellRenderer statusRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(
                                t, v, sel, foc, row, col);
                String s = String.valueOf(v);
                l.setFont(new Font("Segoe UI", Font.BOLD, 12));
                l.setHorizontalAlignment(CENTER);
                if (!sel) {
                    l.setBackground(row % 2 == 0 ? C_WHITE : C_ROWALT);
                    l.setForeground(
                        s.equals("In Stock")     ? C_GREEN  :
                        s.equals("Low Stock")    ? C_ORANGE : C_DANGER);
                }
                return l;
            }
        };
    }

    private JPanel emptyMsg(String msg) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(40, 0, 0, 0));
        JLabel l = new JLabel(msg);
        l.setFont(F_BODY);
        l.setForeground(C_TSUB);
        p.add(l);
        return p;
    }

    private void err(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}