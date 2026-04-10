
import java.awt.*;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;
// import javax.swing.BoxLayout;
// import javax.swing.JButton;
// import javax.swing.JDialog;
// import javax.swing.JFrame;
// import javax.swing.JLayeredPane;
// import javax.swing.JPanel;
import javax.swing.border.*;

// import CinemaFinderUI.Movie;
// import CinemaFinderUI.RoundedPanel;

class MovieDetailsDialog extends JDialog {
    private static final Color PRIMARY_BLUE = new Color(41, 121, 255);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
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
        JButton btnClose = new JButton("X");
        btnClose.setBounds(840, 0, 60, 60);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 20));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        // JLayeredPane để overlay nút X lên trên ảnh Cover
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 250));

        // Ảnh Cover (Giả định bằng màu)
        Image loadedBanner = null;
        try {
            loadedBanner = ImageIO.read(new URL(m.banner));
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh banner: " + e.getMessage());
        }
        final Image bannerImage = loadedBanner;

        // 1. Dùng JPanel và Override paintComponent để vẽ nền
        JPanel coverPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bannerImage != null) {
                    // Vẽ ảnh lấp đầy toàn bộ coverPanel
                    g.drawImage(bannerImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Nếu lỗi mạng, hiển thị nền xám dự phòng
                    g.setColor(new Color(60, 60, 60));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
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
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(900, 60));
        statsPanel.add(createStatCard(new ImageIcon("image/star.png"), " IMDB", 8 + "/10", new Color(255, 248, 225)));
        statsPanel.add(createStatCard(new ImageIcon("image/tomato.png"), " Rotten Tomatoes", "93%", new Color(255, 235, 238)));
        statsPanel.add(createStatCard(new ImageIcon("image/time.png"), " Thời lượng", m.duration + " phút", new Color(227, 242, 253)));
        contentPanel.add(statsPanel);
        contentPanel.add(Box.createVerticalStrut(25));

        // Nội dung phim
        JLabel lblPlotTitle = new JLabel("Nội dung phim");
        lblPlotTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlotTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã có sẵn
        contentPanel.add(lblPlotTitle);
        contentPanel.add(Box.createVerticalStrut(10));
        
        JTextArea txtPlot = new JTextArea("Câu chuyện về nhà vật lý lý thuyết người Mỹ J. Robert Oppenheimer và vai trò của ông trong việc phát triển bom nguyên tử. Bộ phim khám phá cuộc đời và sự nghiệp của Oppenheimer, bao gồm cả thời gian ông làm việc trong Dự án Manhattan.");
        txtPlot.setWrapStyleWord(true);
        txtPlot.setLineWrap(true);
        txtPlot.setOpaque(false);
        txtPlot.setEditable(false);
        txtPlot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPlot.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(txtPlot);
        contentPanel.add(Box.createVerticalStrut(20));

        // Info Grid (Đạo diễn, diễn viên...)
        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        infoGrid.setOpaque(false);
        infoGrid.add(createInfoBlock("Đạo diễn", "Christopher Nolan"));
        infoGrid.add(createInfoBlock("Diễn viên", "Cillian Murphy, Emily Blunt, Matt Damon..."));
        infoGrid.add(createInfoBlock("Thể loại", String.join(", ", m.genre)));
        
        JPanel rightBottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rightBottomInfo.setOpaque(false);
        rightBottomInfo.add(createInfoBlock("Khởi chiếu", "20/1/2024"));
        rightBottomInfo.add(Box.createHorizontalStrut(50));
        rightBottomInfo.add(createInfoBlock("Phân loại", m.ageRating + "+"));
        
        // Nút Trailer
        JButton btnTrailer = new JButton(" Trailer");
        btnTrailer.setIcon(new ImageIcon("image/play.png"));
        btnTrailer.setPreferredSize(new Dimension(110, 35));
        btnTrailer.setFont(new Font("Arial", Font.BOLD, 15));
        btnTrailer.setBackground(new Color(229, 57, 53));
        btnTrailer.setForeground(Color.WHITE);
        btnTrailer.setFocusPainted(false);
        btnTrailer.setOpaque(true);
        btnTrailer.setBorderPainted(false);
        rightBottomInfo.add(Box.createHorizontalStrut(50));
        rightBottomInfo.add(btnTrailer);

        infoGrid.add(rightBottomInfo);
        contentPanel.add(infoGrid);
        contentPanel.add(Box.createVerticalStrut(30));

        // Bài đánh giá
        JLabel lblReview = new JLabel("Bài đánh giá");
        lblReview.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblReview.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(lblReview);
        contentPanel.add(Box.createVerticalStrut(10));
        
        JPanel reviewPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        reviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        reviewPanel.setOpaque(false);
        reviewPanel.add(createReviewCard("IMDB", "Oppenheimer - Nolan's Best Work"));
        reviewPanel.add(createReviewCard("Rotten Tomatoes", "Oppenheimer Review - Brilliant and Haunting"));
        contentPanel.add(reviewPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        // Lịch chiếu hôm nay
        JLabel lblShowtimes = new JLabel("Lịch chiếu hôm nay - 25/3/2026");
        lblShowtimes.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblShowtimes.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(lblShowtimes);
        contentPanel.add(Box.createVerticalStrut(15));

        // Ép trái cho block lịch chiếu 1
        JPanel showtime1 = createCinemaShowtimeBlock("Galaxy Nguyễn Du", "116 Nguyễn Du, Q.1, TP.HCM", new String[]{"09:00", "11:30", "16:45", "22:00"});
        showtime1.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(showtime1);
        contentPanel.add(Box.createVerticalStrut(15));
        
        // Ép trái cho block lịch chiếu 2
        JPanel showtime2 = createCinemaShowtimeBlock("Galaxy Tân Bình", "246 Nguyễn Hồng Đào, Q. Tân Bình, TP.HCM", new String[]{"14:00", "19:30"});
        showtime2.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(showtime2);

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

    private JPanel createStatCard(ImageIcon icon,String title, String value, Color bgColor) {
        RoundedPanel p = new RoundedPanel(10, bgColor);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel lTitle = new JLabel(title);
        lTitle.setIcon(icon);
        lTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
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
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblName.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));

        JLabel lblAddr = new JLabel(address);
        lblAddr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAddr.setForeground(TEXT_MUTED);
        lblAddr.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblAddr.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
        
        p.add(lblName);
        p.add(lblAddr);
        p.add(Box.createVerticalStrut(15));
        
        JPanel timeGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timeGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeGrid.setOpaque(false);
        for (String t : times) {
            RoundedPanel timeBtn = new RoundedPanel(8, Color.WHITE);
            timeBtn.setBorder(BorderFactory.createLineBorder(PRIMARY_BLUE, 1));
            timeBtn.setLayout(new BoxLayout(timeBtn, BoxLayout.Y_AXIS));
            timeBtn.setBorder(BorderFactory.createCompoundBorder(timeBtn.getBorder(), new EmptyBorder(5, 10, 5, 10)));
            
            JLabel lblT = new JLabel(t);
            lblT.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblT.setForeground(PRIMARY_BLUE);
            lblT.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel lblFormat = new JLabel("2D Phụ đề");
            lblFormat.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblFormat.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblPrice = new JLabel("80.000đ");
            lblPrice.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblPrice.setAlignmentX(Component.CENTER_ALIGNMENT);
            
            timeBtn.add(lblT);
            timeBtn.add(lblFormat);
            timeBtn.add(lblPrice);
            timeGrid.add(timeBtn);
        }
        p.add(timeGrid);
        
        return p;
    }
}
