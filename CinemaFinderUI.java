import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class CinemaFinderUI extends JFrame {

    // --- CÁC MÀU SẮC CHỦ ĐẠO TỪ THIẾT KẾ ---
    private static final Color PRIMARY_BLUE = new Color(41, 121, 255);
    private static final Color PRIMARY_PURPLE = new Color(124, 77, 255);
    private static final Color BG_MAIN = new Color(248, 249, 250);
    private static final Color TEXT_DARK = new Color(33, 33, 33);
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

        // 2. Main Content (Scrollable)
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBackground(BG_MAIN);
        mainContent.setBorder(new EmptyBorder(20, 40, 20, 40));

        mainContent.add(createFilterSection());
        mainContent.add(Box.createVerticalStrut(30));
        mainContent.add(createMovieGridSection());

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Footer
        add(createFooter(), BorderLayout.SOUTH);
    }

    // =========================================================
    // CÁC THÀNH PHẦN GIAO DIỆN MÀN HÌNH CHÍNH
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

    private JPanel createFilterSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_MAIN);
        
        JLabel lblTitle = new JLabel("Chọn hệ thống rạp");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(lblTitle, BorderLayout.NORTH);

        JPanel tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        tagsPanel.setBackground(BG_MAIN);
        
        tagsPanel.add(createFilterTag("CGV Cinemas", false));
        tagsPanel.add(createFilterTag("Galaxy Cinema", true));
        tagsPanel.add(createFilterTag("Lotte Cinema", true));

        panel.add(tagsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterTag(String name, boolean isSelected) {
        RoundedPanel tag = new RoundedPanel(10, isSelected ? new Color(240, 248, 255) : Color.WHITE);
        tag.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        if (isSelected) {
            tag.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
        } else {
            tag.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        }

        JCheckBox cb = new JCheckBox();
        cb.setSelected(isSelected);
        cb.setOpaque(false);
        tag.add(cb);
        
        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tag.add(lblName);
        
        return tag;
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

        // Bắt sự kiện click để mở Dialog
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new MovieDetailsDialog(CinemaFinderUI.this, m).setVisible(true);
            }
        });

        // Hình ảnh giả định
        JPanel poster = new JPanel(new BorderLayout());
        poster.setBackground(Color.DARK_GRAY);
        poster.setPreferredSize(new Dimension(250, 200));
        poster.setMaximumSize(new Dimension(500, 200));
        
        // Nút điểm số
        JPanel ratingBadge = new RoundedPanel(8, new Color(255, 193, 7));
        // ratingBadge.add(new JLabel("⭐ " + m.rating));
        JLabel lblRating = new JLabel("⭐ " + m.rating);
        lblRating.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12)); // Sử dụng font hỗ trợ Emoji
        ratingBadge.add(lblRating);
        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        badgeWrapper.setOpaque(false);
        badgeWrapper.add(ratingBadge);
        poster.add(badgeWrapper, BorderLayout.NORTH);
        card.add(poster);

        // Thông tin phim
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel(m.titleVn);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel subTitle = new JLabel(m.titleEn);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);
        
        JLabel details = new JLabel("⏱ " + m.duration + "  •  " + m.ageRating);
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

        // Thanh trạng thái
        JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        status.setBackground(new Color(232, 245, 233));
        JLabel lblStatus = new JLabel("<html><b style='color:#2E7D32;'>● Trạng thái kết nối</b><br/>Đang kết nối với server - Dữ liệu được mã hóa bằng AES-256<br/><span style='color:#757575'>Server đang trích xuất dữ liệu từ: Lotte Cinema, Galaxy Cinema</span></html>");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        status.add(lblStatus);

        // Thanh bản quyền
        JPanel copy = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        copy.setBackground(new Color(38, 50, 56));
        JLabel lblCopy = new JLabel("<html><center><span style='color:white;'>© 2026 Cinema Finder - Ứng dụng tìm kiếm phim và lịch chiếu</span><br/><span style='color:#B0BEC5; font-size:10px;'>Dữ liệu được trích xuất từ các website rạp chiếu phim và được mã hóa an toàn</span></center></html>");
        copy.add(lblCopy);

        footer.add(status);
        footer.add(copy);
        return footer;
    }

    // =========================================================
    // DIALOG CHI TIẾT PHIM VÀ LỊCH CHIẾU
    // =========================================================
    class MovieDetailsDialog extends JDialog {
        public MovieDetailsDialog(JFrame parent, Movie m) {
            super(parent, true);
            setSize(900, 700);
            setLocationRelativeTo(parent);
            setUndecorated(true); // Bỏ thanh title bar mặc định
            
            // Container chính bo góc
            RoundedPanel mainPanel = new RoundedPanel(20, Color.WHITE);
            mainPanel.setLayout(new BorderLayout());
            mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

            // Nút đóng (X)
            JButton btnClose = new JButton("✕");
            btnClose.setBounds(850, 10, 40, 40);
            btnClose.setContentAreaFilled(false);
            btnClose.setBorderPainted(false);
            btnClose.setForeground(Color.WHITE);
            btnClose.setFont(new Font("Arial", Font.BOLD, 18));
            btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnClose.addActionListener(e -> dispose());

            // JLayeredPane để overlay nút X lên trên ảnh Cover
            JLayeredPane layeredPane = new JLayeredPane();
            layeredPane.setPreferredSize(new Dimension(900, 250));

            // Ảnh Cover (Giả định bằng màu)
            JPanel coverPanel = new JPanel(new BorderLayout());
            coverPanel.setBackground(new Color(60, 60, 60));
            coverPanel.setBounds(0, 0, 900, 250);
            
            JPanel titleOverlay = new JPanel();
            titleOverlay.setLayout(new BoxLayout(titleOverlay, BoxLayout.Y_AXIS));
            titleOverlay.setOpaque(false);
            titleOverlay.setBorder(new EmptyBorder(150, 30, 20, 20));
            
            JLabel lblTitle = new JLabel(m.titleVn);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
            lblTitle.setForeground(Color.WHITE);
            JLabel lblSub = new JLabel(m.titleEn);
            lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            lblSub.setForeground(Color.LIGHT_GRAY);
            
            titleOverlay.add(lblTitle);
            titleOverlay.add(lblSub);
            coverPanel.add(titleOverlay, BorderLayout.WEST);

            layeredPane.add(coverPanel, Integer.valueOf(0));
            layeredPane.add(btnClose, Integer.valueOf(1));

            mainPanel.add(layeredPane, BorderLayout.NORTH);

            // Nội dung chi tiết (Cuộn được)
            JPanel contentPanel = new JPanel();
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

            // Thống kê (Stats)
            JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
            statsPanel.setOpaque(false);
            statsPanel.setMaximumSize(new Dimension(900, 60));
            statsPanel.add(createStatCard("⭐ IMDB", m.rating + "/10", new Color(255, 248, 225)));
            statsPanel.add(createStatCard("🍅 Rotten Tomatoes", "93%", new Color(255, 235, 238)));
            statsPanel.add(createStatCard("⏱ Thời lượng", m.duration, new Color(227, 242, 253)));
            contentPanel.add(statsPanel);
            contentPanel.add(Box.createVerticalStrut(25));

            // Nội dung phim
            JLabel lblPlotTitle = new JLabel("Nội dung phim");
            lblPlotTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
            contentPanel.add(lblPlotTitle);
            contentPanel.add(Box.createVerticalStrut(10));
            
            JTextArea txtPlot = new JTextArea("Câu chuyện về nhà vật lý lý thuyết người Mỹ J. Robert Oppenheimer và vai trò của ông trong việc phát triển bom nguyên tử. Bộ phim khám phá cuộc đời và sự nghiệp của Oppenheimer, bao gồm cả thời gian ông làm việc trong Dự án Manhattan.");
            txtPlot.setWrapStyleWord(true);
            txtPlot.setLineWrap(true);
            txtPlot.setOpaque(false);
            txtPlot.setEditable(false);
            txtPlot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            contentPanel.add(txtPlot);
            contentPanel.add(Box.createVerticalStrut(20));

            // Info Grid (Đạo diễn, diễn viên...)
            JPanel infoGrid = new JPanel(new GridLayout(2, 2, 20, 20));
            infoGrid.setOpaque(false);
            infoGrid.add(createInfoBlock("Đạo diễn", "Christopher Nolan"));
            infoGrid.add(createInfoBlock("Diễn viên", "Cillian Murphy, Emily Blunt, Matt Damon..."));
            infoGrid.add(createInfoBlock("Thể loại", String.join(", ", m.genres)));
            
            JPanel rightBottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            rightBottomInfo.setOpaque(false);
            rightBottomInfo.add(createInfoBlock("Khởi chiếu", "20/1/2024"));
            rightBottomInfo.add(Box.createHorizontalStrut(50));
            rightBottomInfo.add(createInfoBlock("Phân loại", m.ageRating));
            
            // Nút Trailer
            JButton btnTrailer = new JButton("▶ Trailer");
            btnTrailer.setBackground(new Color(229, 57, 53));
            btnTrailer.setForeground(Color.WHITE);
            btnTrailer.setFocusPainted(false);
            rightBottomInfo.add(Box.createHorizontalStrut(50));
            rightBottomInfo.add(btnTrailer);

            infoGrid.add(rightBottomInfo);
            contentPanel.add(infoGrid);
            contentPanel.add(Box.createVerticalStrut(30));

            // Bài đánh giá
            JLabel lblReview = new JLabel("Bài đánh giá");
            lblReview.setFont(new Font("Segoe UI", Font.BOLD, 18));
            contentPanel.add(lblReview);
            contentPanel.add(Box.createVerticalStrut(10));
            
            JPanel reviewPanel = new JPanel(new GridLayout(1, 2, 20, 0));
            reviewPanel.setOpaque(false);
            reviewPanel.add(createReviewCard("IMDB", "Oppenheimer - Nolan's Best Work"));
            reviewPanel.add(createReviewCard("Rotten Tomatoes", "Oppenheimer Review - Brilliant and Haunting"));
            contentPanel.add(reviewPanel);
            contentPanel.add(Box.createVerticalStrut(30));

            // Lịch chiếu hôm nay
            JLabel lblShowtimes = new JLabel("Lịch chiếu hôm nay - 25/3/2026");
            lblShowtimes.setFont(new Font("Segoe UI", Font.BOLD, 20));
            contentPanel.add(lblShowtimes);
            contentPanel.add(Box.createVerticalStrut(15));

            contentPanel.add(createCinemaShowtimeBlock("Galaxy Nguyễn Du", "116 Nguyễn Du, Q.1, TP.HCM", new String[]{"09:00", "11:30", "16:45", "22:00"}));
            contentPanel.add(Box.createVerticalStrut(15));
            contentPanel.add(createCinemaShowtimeBlock("Galaxy Tân Bình", "246 Nguyễn Hồng Đào, Q. Tân Bình, TP.HCM", new String[]{"14:00", "19:30"}));

            JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            // Bọc mainPanel trong một panel trong suốt để tạo padding giả shadow
            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(new Color(0, 0, 0, 0)); // Trong suốt
            wrapper.add(mainPanel, BorderLayout.CENTER);
            
            // Đặt nền trong suốt cho JDialog
            setBackground(new Color(0, 0, 0, 0));
            setContentPane(wrapper);
        }

        private JPanel createStatCard(String title, String value, Color bgColor) {
            RoundedPanel p = new RoundedPanel(10, bgColor);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(new EmptyBorder(10, 15, 10, 15));
            JLabel lTitle = new JLabel(title);
            lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JLabel lVal = new JLabel(value);
            lVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
            p.add(lTitle);
            p.add(Box.createVerticalStrut(5));
            p.add(lVal);
            return p;
        }

        private JPanel createInfoBlock(String label, String value) {
            JPanel p = new JPanel();
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setOpaque(false);
            JLabel lbl = new JLabel(label);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lbl.setForeground(TEXT_MUTED);
            JLabel val = new JLabel(value);
            val.setFont(new Font("Segoe UI", Font.BOLD, 14));
            p.add(lbl);
            p.add(Box.createVerticalStrut(3));
            p.add(val);
            return p;
        }

        private JPanel createReviewCard(String source, String text) {
            RoundedPanel p = new RoundedPanel(10, Color.WHITE);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(15, 15, 15, 15)));
            JLabel src = new JLabel(source);
            src.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            src.setForeground(TEXT_MUTED);
            JLabel link = new JLabel(text + " ↗");
            link.setFont(new Font("Segoe UI", Font.BOLD, 13));
            link.setForeground(PRIMARY_BLUE);
            p.add(src);
            p.add(Box.createVerticalStrut(5));
            p.add(link);
            return p;
        }

        private JPanel createCinemaShowtimeBlock(String name, String address, String[] times) {
            RoundedPanel p = new RoundedPanel(10, Color.WHITE);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(15, 20, 15, 20)));
            
            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
            JLabel lblAddr = new JLabel(address);
            lblAddr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblAddr.setForeground(TEXT_MUTED);
            
            p.add(lblName);
            p.add(lblAddr);
            p.add(Box.createVerticalStrut(15));
            
            JPanel timeGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            timeGrid.setOpaque(false);
            for (String t : times) {
                RoundedPanel timeBtn = new RoundedPanel(8, Color.WHITE);
                timeBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
                timeBtn.setLayout(new BoxLayout(timeBtn, BoxLayout.Y_AXIS));
                timeBtn.setBorder(BorderFactory.createCompoundBorder(timeBtn.getBorder(), new EmptyBorder(5, 10, 5, 10)));
                
                JLabel lblT = new JLabel(t);
                lblT.setFont(new Font("Segoe UI", Font.BOLD, 16));
                lblT.setForeground(PRIMARY_BLUE);
                JLabel lblFormat = new JLabel("2D Phụ đề");
                lblFormat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                JLabel lblPrice = new JLabel("80.000đ");
                lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                
                timeBtn.add(lblT);
                timeBtn.add(lblFormat);
                timeBtn.add(lblPrice);
                timeGrid.add(timeBtn);
            }
            p.add(timeGrid);
            
            return p;
        }
    }

    // =========================================================
    // LỚP HỖ TRỢ (MODELS & CUSTOM UI)
    // =========================================================
    
    // Model Dữ liệu Phim
    static class Movie {
        String titleVn, titleEn, duration, ageRating, rating;
        String[] genres;

        public Movie(String titleVn, String titleEn, String duration, String ageRating, String rating, String[] genres) {
            this.titleVn = titleVn;
            this.titleEn = titleEn;
            this.duration = duration;
            this.ageRating = ageRating;
            this.rating = rating;
            this.genres = genres;
        }
    }

    // Custom Panel để vẽ bo góc
    class RoundedPanel extends JPanel {
        private int cornerRadius = 15;

        public RoundedPanel(int radius, Color bgColor) {
            super();
            this.cornerRadius = radius;
            setOpaque(false); // Quan trọng để nền không bị vuông
            setBackground(bgColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius));
            super.paintComponent(g2);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        // Thiết lập giao diện hệ thống cho đẹp hơn
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