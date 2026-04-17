package client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;

import org.json.JSONObject;

class MovieDetailsDialog extends JDialog {
    private static final Color PRIMARY_BLUE = new Color(41, 121, 255);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    
    // ĐƯA CÁC BIẾN NÀY LÊN ĐẦU ĐỂ KHÔNG BỊ LỖI SCOPE
    private JTextArea txtPlot;
    private JPanel reviewPanel;
    private String ipserver;

    // THÊM THAM SỐ ipserver VÀO CONSTRUCTOR
    public MovieDetailsDialog(JFrame parent, Movie m, String ipserver) {
        super(parent, true);
        this.ipserver = ipserver; // Nhận IP từ màn hình chính truyền sang
        
        setSize(900, 700);
        setLocationRelativeTo(parent);
        setUndecorated(true); 
        
        RoundedPanel mainPanel = new RoundedPanel(20, Color.WHITE);
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JButton btnClose = new JButton("X");
        btnClose.setBounds(840, 0, 60, 60);
        btnClose.setContentAreaFilled(false);
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setForeground(Color.WHITE);
        btnClose.setFont(new Font("Arial", Font.BOLD, 20));
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> dispose());

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(new Dimension(900, 250));

        Image loadedBanner = null;
        try {
            loadedBanner = ImageIO.read(new URL(m.banner));
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh banner: " + e.getMessage());
        }
        final Image bannerImage = loadedBanner;

        JPanel coverPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bannerImage != null) {
                    g.drawImage(bannerImage, 0, 0, getWidth(), getHeight(), this);
                } else {
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

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(20, 30, 30, 30));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.setOpaque(false);
        statsPanel.setMaximumSize(new Dimension(900, 60));

        String imdbDisplay = m.imdbRating.equals("N/A") ? "N/A" : m.imdbRating + "/10";
        String rottenDisplay = m.rottenRating;

        statsPanel.add(createStatCard(new ImageIcon("image/star.png"), " TMDB", imdbDisplay, new Color(255, 248, 225)));
        statsPanel.add(createStatCard(new ImageIcon("image/tomato.png"), "Số Lượng Votes", rottenDisplay, new Color(255, 235, 238)));
        statsPanel.add(createStatCard(new ImageIcon("image/time.png"), " Thời Lượng", m.duration + " phút", new Color(227, 242, 253)));

        contentPanel.add(statsPanel);

        JLabel lblPlotTitle = new JLabel("Nội dung phim");
        lblPlotTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPlotTitle.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(lblPlotTitle);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // SỬA LỖI 1: KHỞI TẠO BIẾN TOÀN CỤC THAY VÌ TẠO BIẾN CỤC BỘ MỚI
        this.txtPlot = new JTextArea(m.description.isEmpty() ? "Đang cập nhật nội dung phim..." : m.description);
        txtPlot.setWrapStyleWord(true);
        txtPlot.setLineWrap(true);
        txtPlot.setOpaque(false);
        txtPlot.setEditable(false);
        txtPlot.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        txtPlot.setForeground(TEXT_MUTED);
        txtPlot.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(txtPlot);
        contentPanel.add(Box.createVerticalStrut(20));

        JPanel infoGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        infoGrid.setAlignmentX(Component.LEFT_ALIGNMENT); 
        infoGrid.setOpaque(false);
        infoGrid.add(createInfoBlock("Đạo diễn", m.director.isEmpty() ? "Đang cập nhật" : m.director));
        infoGrid.add(createInfoBlock("Diễn viên", m.actors.isEmpty() ? "Đang cập nhật" : m.actors));
        infoGrid.add(createInfoBlock("Thể loại", m.genre));
        
        JPanel rightBottomInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        rightBottomInfo.setOpaque(false);
        rightBottomInfo.add(createInfoBlock("Khởi chiếu", m.releaseDate.isEmpty() ? "Sắp chiếu" : m.releaseDate));
        rightBottomInfo.add(Box.createHorizontalStrut(50));
        rightBottomInfo.add(createInfoBlock("Phân loại", m.ageRating + "+"));
        
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

        JLabel lblReview = new JLabel("Bài đánh giá");
        lblReview.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblReview.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(lblReview);
        contentPanel.add(Box.createVerticalStrut(10));
        
        // SỬA LỖI 1: KHỞI TẠO BIẾN TOÀN CỤC

        this.reviewPanel = new JPanel(new GridLayout(0, 1, 0, 15));
        reviewPanel.setAlignmentX(Component.LEFT_ALIGNMENT); 
        reviewPanel.setOpaque(false);
        // Đặt kích thước tối đa để các nút không bị giãn dài xuống dưới màn hình
        reviewPanel.setMaximumSize(new Dimension(900, 150));
        
        // Hiển thị trạng thái Loading tạm thời
        reviewPanel.add(createReviewCard("Hệ thống", "Đang tìm kiếm các bài review...", ""));
        contentPanel.add(reviewPanel);
        contentPanel.add(Box.createVerticalStrut(30));
        
        // Hiển thị trạng thái Loading tạm thời trước khi có Link thật
        reviewPanel.add(createReviewCard("Khen Phim", "Đang tìm kiếm review...", ""));
        contentPanel.add(reviewPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        JLabel lblShowtimes = new JLabel("Lịch chiếu tham khảo");
        lblShowtimes.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblShowtimes.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(lblShowtimes);
        contentPanel.add(Box.createVerticalStrut(15));

        JPanel showtime1 = createCinemaShowtimeBlock("Rạp Beta Cinemas", "Thông tin lịch chiếu đang được cập nhật", new String[]{"09:00", "11:30", "16:45", "22:00"});
        showtime1.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(showtime1);
        contentPanel.add(Box.createVerticalStrut(15));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(0, 0, 0, 0)); 
        wrapper.add(mainPanel, BorderLayout.CENTER);
        
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

    private JPanel createReviewCard(String source, String text, String url) {
        RoundedPanel p = new RoundedPanel(10, Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(15, 15, 15, 15)));

        // Kiểm tra xem có URL thật hay không
        boolean hasUrl = (url != null && !url.isEmpty());

        JLabel src = new JLabel(source);
        src.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        src.setForeground(TEXT_MUTED);
        
        // Nếu có URL thì hiện chữ xanh gạch chân, không có thì hiện chữ xám
        String displayText = hasUrl ? "<html><u>" + text + "</u> ↗</html>" : "Chưa có bài review cho phim này";
        JLabel link = new JLabel(displayText);
        link.setFont(new Font("Segoe UI", Font.BOLD, 13));
        link.setForeground(hasUrl ? PRIMARY_BLUE : TEXT_MUTED);
        
        p.add(src);
        p.add(Box.createVerticalStrut(5));
        p.add(link);

        // Chỉ gắn sự kiện click và đổi con trỏ chuột nếu có URL
        if (hasUrl) {
            p.setCursor(new Cursor(Cursor.HAND_CURSOR));
            p.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception ex) {
                        System.err.println("Không thể mở trình duyệt: " + ex.getMessage());
                    }
                }
            });
        }

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

    private void fetchExtraInfo(String movieName) {
        SwingWorker<JSONObject, Void> worker = new SwingWorker<>() {
            @Override
            protected JSONObject doInBackground() throws Exception {
                try (Socket socket = new Socket(ipserver, 4000);
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    
                    out.println("GET_MOVIE_EXTRA|" + movieName);
                    return new JSONObject(in.readLine());
                }
            }

            @Override
            protected void done() {
                try {
                    JSONObject data = get();
                    
                    // Cập nhật AI Summary
                    txtPlot.setText(data.getString("ai_summary"));
                    txtPlot.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    txtPlot.setForeground(Color.BLACK); 
                    
                    reviewPanel.removeAll();
                    
                    // 1. Thêm Card Khen Phim
                    String kpUrl = data.optString("kp_url", "");
                    reviewPanel.add(createReviewCard("Khen Phim", data.optString("kp_title", "Xem review"), kpUrl));
                    
                    // 2. Thêm Card Moveek
                    String moveekUrl = data.optString("moveek_url", "");
                    reviewPanel.add(createReviewCard("Moveek", data.optString("moveek_title", "Review trên Moveek"), moveekUrl));
                    
                    // 3. Thêm Card Báo mạng (Chỉ hiện nếu có link)
                    String generalUrl = data.optString("general_url", "");
                    if (!generalUrl.isEmpty()) {
                        String sourceName = data.optString("general_source", "Web Review");
                        String generalTitle = data.optString("general_title", "Đọc bài viết");
                        reviewPanel.add(createReviewCard(sourceName, generalTitle, generalUrl));
                    }
                    
                    reviewPanel.revalidate();
                    reviewPanel.repaint();
                    
                } catch (Exception e) {
                    txtPlot.setText("Không thể tải thông tin từ Server. Vui lòng thử lại sau.");
                    txtPlot.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }
}