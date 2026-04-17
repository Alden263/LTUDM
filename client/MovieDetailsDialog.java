package client;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
        txtPlot.setMaximumSize(new Dimension(800, Integer.MAX_VALUE));
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

        // Lịch chiếu hôm nay
        String today = java.time.LocalDate.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"));
        // JLabel lblShowtimes = new JLabel("Lịch chiếu hôm nay " + today);
        
        // Hiển thị trạng thái Loading tạm thời trước khi có Link thật
        reviewPanel.add(createReviewCard("Khen Phim", "Đang tìm kiếm review...", ""));
        contentPanel.add(reviewPanel);
        contentPanel.add(Box.createVerticalStrut(30));

        JLabel lblShowtimes = new JLabel("Lịch chiếu tham khảo");
        lblShowtimes.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblShowtimes.setAlignmentX(Component.LEFT_ALIGNMENT); 
        contentPanel.add(lblShowtimes);
        contentPanel.add(Box.createVerticalStrut(15));

        // Ép trái cho block lịch chiếu
        List<String> times = new ArrayList<>();
        for (Movie.SessionGroup group : m.sessionGroups) {
            for (Movie.SessionTime session : group.sessions) {
                String start = formatTime(session.startTime);
                String end = formatTime(session.endTime);
                if (!start.isEmpty() && !end.isEmpty()) {
                    times.add(start + "~" + end);
                }
            }
        }

        JPanel showtime1 = createCinemaShowtimeBlock(
            m.cinemaName,
            m.cinemaAddress,
            m.provider,
            times.toArray(new String[0])
        );
        showtime1.setAlignmentX(Component.LEFT_ALIGNMENT); // Đã thêm ép trái
        contentPanel.add(showtime1);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
            scrollPane.setBorder(null);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);
            mainPanel.add(scrollPane, BorderLayout.CENTER);

            JPanel wrapper = new JPanel(new BorderLayout());
            wrapper.setBackground(new Color(0, 0, 0, 0)); 
            wrapper.add(mainPanel, BorderLayout.CENTER);
            
            setBackground(new Color(0, 0, 0, 0));
            setContentPane(wrapper);

            // DÒNG QUAN TRỌNG NHẤT: Gọi hàm để bắt đầu tải dữ liệu từ Server
            fetchExtraInfo(m.titleVn);

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
        p.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép toàn bộ khối sát lề trái

        // 1. NHÃN (Label): Ngắn nên chỉ cần JLabel là đủ
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép lề trái

        // 2. GIÁ TRỊ (Value): Dài nên BẮT BUỘC dùng JTextArea để rớt dòng
        JTextArea val = new JTextArea(value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 14));
        val.setWrapStyleWord(true);
        val.setLineWrap(true);
        // Ép chiều ngang tối đa khoảng 350px để danh sách diễn viên tự rớt dòng
        val.setMaximumSize(new Dimension(350, Integer.MAX_VALUE)); 
        val.setEditable(false);
        val.setOpaque(false);
        val.setFocusable(false);
        val.setAlignmentX(Component.LEFT_ALIGNMENT); // Ép lề trái

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
        String displayText = hasUrl ? "<html><u>" + text + "</u> ↗</html>" : text;
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

    private JPanel createCinemaShowtimeBlock(String name, String address, String providerName, String[] times) {
        RoundedPanel p = new RoundedPanel(10, Color.WHITE);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel headerRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblLogo = new JLabel();
        try {
            String logoPath = resolveCinemaLogo(providerName, name);
            ImageIcon icon = new ImageIcon(new ImageIcon(logoPath)
                .getImage().getScaledInstance(44, 28, Image.SCALE_SMOOTH));
            lblLogo.setIcon(icon);
        } catch (Exception e) {
            lblLogo.setPreferredSize(new Dimension(44, 28));
        }

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel lblName = new JLabel(name);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblAddr = new JLabel(address);
        lblAddr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblAddr.setForeground(TEXT_MUTED);
        lblAddr.setAlignmentX(Component.LEFT_ALIGNMENT);

        headerText.add(lblName);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(lblAddr);

        headerRow.add(lblLogo);
        headerRow.add(headerText);

        p.add(headerRow);
        p.add(Box.createVerticalStrut(12));

        JLabel lblGroup = new JLabel("2D Phụ đề Eng&Viet");
        lblGroup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblGroup);
        p.add(Box.createVerticalStrut(10));

        JPanel timeRow = new JPanel(new GridLayout(0, 5, 10, 10));
        timeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        timeRow.setOpaque(false);
        for (String t : times) {
            RoundedPanel timeBtn = new RoundedPanel(16, Color.WHITE);
            timeBtn.setLayout(new BoxLayout(timeBtn, BoxLayout.X_AXIS));
            timeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225), 1),
                new EmptyBorder(8, 14, 8, 14)
            ));

            String displayTime = t.replace("~", " ~ ");

            JLabel lblT = new JLabel(displayTime);
            lblT.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblT.setForeground(PRIMARY_BLUE);

            timeBtn.add(lblT);
            timeRow.add(timeBtn);
        }
        p.add(timeRow);

        return p;
    }

    private String formatDate(String ymd) {
        try {
            if (ymd != null && ymd.length() >= 10) {
                return java.time.LocalDate.parse(ymd.substring(0, 10))
                    .format(java.time.format.DateTimeFormatter.ofPattern("d/M/yyyy"));
            }
            return ymd == null ? "" : ymd;
        } catch (Exception e) {
            return ymd == null ? "" : ymd;
        }
    }

    private String formatTime(String dt) {
        try {
            if (dt == null || dt.isEmpty()) {
                return "";
            }
            return java.time.LocalDateTime.parse(dt.replace(" ", "T"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return dt == null ? "" : dt;
        }
    }

    private String resolveCinemaLogo(String providerName, String cinemaName) {
        String provider = providerName == null ? "" : providerName.toLowerCase();
        String cinema = cinemaName == null ? "" : cinemaName.toLowerCase();
        if (provider.contains("galaxy") || cinema.contains("galaxy")) {
            return "image/galaxy_logo.png";
        }
        if (provider.contains("lotte") || cinema.contains("lotte")) {
            return "image/lotte_logo.png";
        }
        return "image/cgv_logo.png";
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
                
                // AI đã bỏ nên không cần txtPlot.setText nữa
                
                // Xóa các card "Loading" tạm thời
                reviewPanel.removeAll();
                
                // 1. Thêm Card Khen Phim
                String kpUrl = data.optString("kp_url", "");
                if (!kpUrl.isEmpty()) {
                    reviewPanel.add(createReviewCard("Khen Phim", data.optString("kp_title", "Xem review chi tiết"), kpUrl));
                } else {
                    reviewPanel.add(createReviewCard("Khen Phim", "Không tìm thấy bài review trên Khen Phim", ""));
                }
                
                // 2. Thêm Card Moveek
                String moveekUrl = data.optString("moveek_url", "");
                if (!moveekUrl.isEmpty()) {
                    reviewPanel.add(createReviewCard("Moveek", data.optString("moveek_title", "Đọc review trên Moveek"), moveekUrl));
                } else {
                    reviewPanel.add(createReviewCard("Moveek", "Không tìm thấy bài review trên Moveek", ""));
                }
                
                // 3. Thêm Card Báo mạng (Vét cạn)
                String generalUrl = data.optString("general_url", "");
                if (!generalUrl.isEmpty()) {
                    String sourceName = data.optString("general_source", "Web Review");
                    reviewPanel.add(createReviewCard(sourceName, data.optString("general_title", "Đọc bài viết"), generalUrl));
                }
                
                // Vẽ lại giao diện
                reviewPanel.revalidate();
                reviewPanel.repaint();
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        };
        worker.execute();
    }
}