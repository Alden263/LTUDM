import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class CinemaFinderUI extends JFrame {

    // --- CÁC MÀU SẮC CHỦ ĐẠO TỪ THIẾT KẾ ---
    private static final Color PRIMARY_BLUE = new Color(41, 121, 255);
    private static final Color PRIMARY_PURPLE = new Color(124, 77, 255);
    private static final Color BG_MAIN = new Color(248, 249, 250);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);

    public CinemaFinderUI() {
        setTitle("Cinema Finder");
        setSize(1280, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        // 1. Header (Gradient)
        add(createHeader(), BorderLayout.NORTH);

        // --- TÁI CẤU TRÚC LAYOUT CHÍNH ---
        JPanel mainBody = new JPanel(new BorderLayout());

        // 2. Sidebar Bên Trái (Danh sách chi nhánh rạp)
        mainBody.add(createSidebarSection(), BorderLayout.WEST);

        // 3. Nội dung bên phải (Chứa Filter Hệ thống rạp + Lưới Phim)
        JPanel rightContent = new JPanel();
        rightContent.setLayout(new BoxLayout(rightContent, BoxLayout.Y_AXIS));
        rightContent.setBackground(BG_MAIN);
        rightContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        // Trả lại nguyên vẹn FilterSection của bạn
        rightContent.add(createFilterSection());
        rightContent.add(Box.createVerticalStrut(30));
        rightContent.add(createMovieGridSection());

        JScrollPane scrollRight = new JScrollPane(rightContent);
        scrollRight.setBorder(null);
        scrollRight.getVerticalScrollBar().setUnitIncrement(16);
        mainBody.add(scrollRight, BorderLayout.CENTER);

        add(mainBody, BorderLayout.CENTER);

        // 4. Footer
        add(createFooter(), BorderLayout.SOUTH);
    }

    // =========================================================
    // CÁC THÀNH PHẦN GIAO DIỆN
    // =========================================================

    private JPanel createHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_BLUE, getWidth(), 0, PRIMARY_PURPLE);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        header.setPreferredSize(new Dimension(1000, 80));
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 30, 15));

        JLabel title = new JLabel("<html><b style='font-size:18px; color:white;'>Cinema Finder</b><br/><span style='font-size:10px; color:white;'>Tìm phim và lịch chiếu tại TP.HCM</span></html>");
        header.add(title);
        return header;
    }

    // --- Sidebar danh sách chi nhánh ---
    private JPanel createSidebarSection() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setBackground(Color.WHITE);
        sidebar.setPreferredSize(new Dimension(280, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER_COLOR));

        // Thanh tìm kiếm trong sidebar
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(Color.WHITE);
        searchPanel.setBorder(new EmptyBorder(35, 15, 35, 15));
        // JTextField txtSearch = new JTextField(" Tìm theo tên rạp ...");
        // txtSearch.setPreferredSize(new Dimension(250, 35));
        // txtSearch.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        // searchPanel.add(txtSearch, BorderLayout.CENTER);

        sidebar.add(searchPanel, BorderLayout.NORTH);

        // Container danh sách
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        // Dữ liệu giả lập bằng List
        List<String> branches = new ArrayList<>();
        branches.add("CGV Giga Mall Thủ Đức");
        branches.add("CGV Pandora City");
        branches.add("CGV Sư Vạn Hạnh");
        branches.add("CGV Liberty Citypoint");
        branches.add("CGV Vincom Center Landmark 81");
        branches.add("CGV Crescent Mall");
        branches.add("CGV Vincom Đồng Khởi");
        renderBranches(listPanel, branches);

        JScrollPane scrollSidebar = new JScrollPane(listPanel);
        scrollSidebar.setBorder(null);
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(scrollSidebar, BorderLayout.CENTER);

        return sidebar;
    }

    int selectedIndex = 0;
    private void renderBranches(JPanel listPanel, List<String> branches) {
        listPanel.removeAll(); // Xóa hết các item cũ trước khi vẽ lại
        
        for (int i = 0; i < branches.size(); i++) {
            final int index = i;
            String branchName = branches.get(i);
            boolean isSelected = (i == selectedIndex);

            // --- Khởi tạo Item Panel ---
            JPanel item = new JPanel(new BorderLayout());
            item.setBackground(isSelected ? new Color(255, 240, 245) : Color.WHITE);
            item.setPreferredSize(new Dimension(300, 50));
            item.setMaximumSize(new Dimension(300, 50));
            item.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(245, 245, 245)));
            item.setCursor(new Cursor(Cursor.HAND_CURSOR));

            // --- Nội dung bên trái (Icon + Tên) ---
            JPanel contentWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
            contentWrapper.setOpaque(false);

            // JLabel lblIcon = new JLabel(" "); // Mock icon
            JLabel lblName = new JLabel(branchName);
            lblName.setFont(new Font("Segoe UI", isSelected ? Font.BOLD : Font.PLAIN, 13));
            try {
                    ImageIcon icon = new ImageIcon(new ImageIcon("image/cgv_logo.png").getImage().getScaledInstance(30, 18, Image.SCALE_SMOOTH));
                    lblName.setIcon(icon);
                } catch (Exception e) {
                    lblName.setText("CGV");
                }
            if (isSelected) lblName.setForeground(new Color(220, 20, 60));
            contentWrapper.add(lblName);
            item.add(contentWrapper, BorderLayout.CENTER);

            // --- Mũi tên bên phải ---
            JLabel lblArrow = new JLabel(" > ");
            lblArrow.setForeground(Color.LIGHT_GRAY);
            lblArrow.setBorder(new EmptyBorder(0, 0, 0, 15));
            item.add(lblArrow, BorderLayout.EAST);

            // --- Sự kiện Click và Hover ---
            item.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedIndex = index; // Cập nhật vị trí được chọn
                    renderBranches(listPanel, branches); // Vẽ lại toàn bộ danh sách
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (index != selectedIndex) item.setBackground(new Color(248, 249, 250));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (index != selectedIndex) item.setBackground(Color.WHITE);
                }
            });

            listPanel.add(item);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
    // --- Phần Filter Hệ thống rạp ---
    private JPanel createFilterSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_MAIN);
        
        JLabel lblTitle = new JLabel("Chọn hệ thống rạp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        tagsPanel.setBackground(BG_MAIN);

        //Quản lý các tag
        List<JPanel> allTags = new ArrayList<>();
        
        JPanel tagCGV = createFilterTag("CGV", "image/cgv_logo.png", allTags);
        JPanel tagGalaxy = createFilterTag("Galaxy Cinema", "image/galaxy_logo.png", allTags);
        JPanel tagLotte = createFilterTag("Lotte", "image/lotte_logo.png", allTags);

        tagsPanel.add(tagCGV);
        tagsPanel.add(tagGalaxy);
        tagsPanel.add(tagLotte);
        selectSingleTag(tagCGV, allTags); //mặc định là cgv

        panel.add(tagsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterTag(String name, String iconPath, List<JPanel> allTags) {
        // Tạo Panel với góc bo tròn
        RoundedPanel tag = new RoundedPanel(15, Color.WHITE);
        tag.setName(name);
        tag.setLayout(new GridBagLayout()); 
        tag.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

        // Label chứa Logo
        JLabel lblIcon = new JLabel();
        try {
            ImageIcon originalIcon = new ImageIcon(iconPath);
            Image img = originalIcon.getImage().getScaledInstance(140, 80, Image.SCALE_SMOOTH);
            lblIcon.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblIcon.setText(name); 
            lblIcon.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        }
        lblIcon.setToolTipText(name); 
        tag.add(lblIcon);
        allTags.add(tag);

        lblIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectSingleTag(tag, allTags);
            }
        });

        return tag;
    }

    private void selectSingleTag(JPanel selectedTag, List<JPanel> allTags) {
        for (JPanel tag : allTags) {
            if (tag == selectedTag) {
                tag.setBackground(new Color(240, 248, 255)); 
                tag.setPreferredSize(new Dimension(150, 90)); 
                tag.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 2)); 
            } else {
                tag.setBackground(Color.WHITE); 
                tag.setPreferredSize(new Dimension(140, 80)); 
                tag.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1)); 
            }
            tag.revalidate();
            tag.repaint();
        }
    }

    private JPanel createMovieGridSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_MAIN);

        JLabel lblTitle = new JLabel("Phim đang chiếu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 4, 25, 25)); // Lưới 4 cột
        grid.setBackground(BG_MAIN);

        // Tạo dữ liệu giả bằng List
        List<Movie> movies = new ArrayList<>();
        movies.add(new Movie("Hành Tinh Cát: Phần Hai", "Dune: Part Two", "166 phút", "C13", "8.8", new String[]{"Sci-Fi", "Adventure"}));
        movies.add(new Movie("Nhóm Marvels", "The Marvels", "105 phút", "C13", "7.2", new String[]{"Action", "Adventure"}));
        movies.add(new Movie("Oppenheimer", "Oppenheimer", "180 phút", "C16", "8.6", new String[]{"Biography", "Drama"}));
        movies.add(new Movie("Kung Fu Gấu Trúc 4", "Kung Fu Panda 4", "94 phút", "P", "7.5", new String[]{"Animation", "Action"}));
        movies.add(new Movie("Godzilla và Kong: Đế Chế Mới", "Godzilla x Kong", "115 phút", "C13", "7.8", new String[]{"Action", "Sci-Fi"}));

        for (Movie m : movies) {
            grid.add(createMovieCard(m));
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMovieCard(Movie m) {
        RoundedPanel card = new RoundedPanel(15, Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MovieDetailsDialog(CinemaFinderUI.this, m).setVisible(true);
            }
        });

        JPanel poster = new JPanel(new BorderLayout());
        poster.setBackground(Color.DARK_GRAY);
        poster.setPreferredSize(new Dimension(250, 200));
        poster.setMaximumSize(new Dimension(500, 200));
        
        JPanel ratingBadge = new RoundedPanel(8, new Color(255, 193, 7));
        try {
            ImageIcon staricon = new ImageIcon("image/star.png");
            JLabel lblRating = new JLabel(" " + m.rating);
            lblRating.setIcon(staricon);
            ratingBadge.add(lblRating);
        } catch(Exception e) {
            ratingBadge.add(new JLabel("⭐ " + m.rating));
        }
        
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(ratingBadge);
        poster.add(badgeWrapper, BorderLayout.NORTH);
        card.add(poster);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel(m.titleVn);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel subTitle = new JLabel(m.titleEn);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);
        
        JLabel details = new JLabel(" " + m.duration + "  •  " + m.ageRating);
        try {
            ImageIcon clockicon = new ImageIcon("image/time.png");
            details.setIcon(clockicon);
        } catch(Exception e) {}
        
        details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        details.setForeground(TEXT_MUTED);

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tags.setOpaque(false);
        for (String genre : m.genres) {
            RoundedPanel t = new RoundedPanel(5, new Color(227, 242, 253));
            JLabel l = new JLabel(genre);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            l.setForeground(new Color(25, 118, 210));
            t.add(l);
            tags.add(t);
        }

        info.add(title);
        info.add(Box.createVerticalStrut(5));
        info.add(subTitle);
        info.add(Box.createVerticalStrut(10));
        info.add(details);
        info.add(Box.createVerticalStrut(10));
        info.add(tags);

        card.add(info);
        return card;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));

        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        status.setBackground(new Color(232, 245, 233));
        JLabel lblStatus = new JLabel("<html><b style='color:#2E7D32;'>● Trạng thái kết nối</b><br/>Đang kết nối với server - Dữ liệu được mã hóa bằng AES-256<br/><span style='color:#757575'>Server đang trích xuất dữ liệu từ: Lotte Cinema, Galaxy Cinema</span></html>");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.add(lblStatus);

        JPanel copy = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        copy.setBackground(new Color(38, 50, 56));
        JLabel lblCopy = new JLabel("<html><center><span style='color:white;'>© 2026 Cinema Finder - Ứng dụng tìm kiếm phim và lịch chiếu</span><br/><span style='color:#B0BEC5; font-size:10px;'>Dữ liệu được trích xuất từ các website rạp chiếu phim và được mã hóa an toàn</span></center></html>");
        copy.add(lblCopy);

        footer.add(status);
        footer.add(copy);
        return footer;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new CinemaFinderUI().setVisible(true);
        });
    }
}