package client;
import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CinemaFinderUI extends JFrame {
    private JPanel listPanel;
    private JPanel grid = new JPanel(new GridLayout(0, 4, 25, 25)); // Lưới 4 cột
    private LinkedHashMap<Integer, String> branches;
    private static final String SERVER_PUBLIC_KEY_B64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyIHQv416vWEdZeDzpiN0RavmUAA/YFjvTcL24rtJzqCjP+vqa9IQGFQElfBEvOjxatzr/R+EUytJva9rRtTFu5KY2KM7Q9gCqe/FKu48tiFE6JX7FBiEUiM/tG2bN9sEQzX9f6tHRSL5wdkGbJXsyuwdltfFzLfMZLWTG+0j8rNfJYU89D5eN76ezSWpvmsxPYmUZWsqxOnZ+4LvNgAiFlzjueSBaiwoSg+ibZnGpF4Z3FF2Pq2QMuUs5KAFSArZ5d+aQGUGYgGRKo7/wKVXy7TOaW6aLtXD+wguLI43hnUbCEWpTzhpws1q9Hf55yAli5DRZsrBhOP7P3MY0/aILwIDAQAB";

    // --- CÁC MÀU SẮC CHỦ ĐẠO TỪ THIẾT KẾ ---
    private static final Color PRIMARY_BLUE = new Color(41, 121, 255);
    private static final Color PRIMARY_PURPLE = new Color(124, 77, 255);
    private static final Color BG_MAIN = new Color(248, 249, 250);
    private static final Color TEXT_MUTED = new Color(117, 117, 117);
    private static final Color BORDER_COLOR = new Color(224, 224, 224);
    private static final int cgvcinemaId = 3;
    private static final int galaxycinemaId = 2;
    private static final int lottecinemaId = 26;
    private String ipserver;

    // #region Search IP server từ API
    public void searchipserver() throws IOException{
        String api = "https://retoolapi.dev/lKNfWn/data/1";
        Document doc = Jsoup.connect(api).ignoreContentType(true).ignoreHttpErrors(true).header("Content-Type", "application/json").method(Connection.Method.GET).execute().parse();
        JSONObject json = new JSONObject(doc.text());
        ipserver = json.get("ip").toString();
        System.out.println("IP Server: " + ipserver);
    }
    public CinemaFinderUI() {
        try {
            searchipserver();
        } catch (IOException e) {
            System.err.println("Lỗi khi tìm IP server: " + e.getMessage());
        }

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
        mainBody.add(createSidebarSection(cgvcinemaId), BorderLayout.WEST);

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
    private JPanel createSidebarSection(int typecinemaId) {
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
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        // Dữ liệu giả lập bằng List
        branches = new LinkedHashMap<>();
        getlistcinema(typecinemaId);
        renderBranches(listPanel, branches, typecinemaId);

        JScrollPane scrollSidebar = new JScrollPane(listPanel);
        scrollSidebar.setBorder(null);
        scrollSidebar.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(scrollSidebar, BorderLayout.CENTER);

        return sidebar;
    }

    // #region Lấy list rạp chiếu
    private void getlistcinema(int cinemaId){
        SwingWorker<LinkedHashMap<Integer, String>, Void> worker = new SwingWorker<LinkedHashMap<Integer, String>, Void>() {
        @Override
        protected LinkedHashMap<Integer, String> doInBackground() {
            LinkedHashMap<Integer, String> rawData = new LinkedHashMap<>();
            
            try (Socket socket = new Socket(ipserver, 4000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                
                SecretKey sessionKey = CryptoManager.generateSessionKey();
                // Gửi yêu cầu lấy danh sách rạp theo loại
                String command = "GET_CINEMAS|" + cinemaId;
                String packet = CryptoManager.encryptClientRequest(command, SERVER_PUBLIC_KEY_B64, sessionKey);
                out.println(packet);

                
                String response = in.readLine();
                String jsonresponse = CryptoManager.decryptServerResponse(response, sessionKey);
                JSONObject json = new JSONObject(jsonresponse);
                String status = json.getString("status"); 
                if(status.equals("error")){
                    return new LinkedHashMap<>(); 
                }
                JSONObject data = json.getJSONObject("data");
                for(String key : data.keySet()){
                    rawData.put(Integer.parseInt(key), data.getString(key));
                }
            } catch (Exception e) {
                System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
            }
            
            return rawData;
        }

        @Override
        protected void done() {
            try {
                // Lấy dữ liệu List từ hàm doInBackground
                LinkedHashMap<Integer, String> resultList = get(); 
                branches.clear();
                listPanel.removeAll();
                if(resultList.isEmpty()){
                    JLabel lblEmpty = new JLabel("Không tìm thấy rạp nào cho khu vực này.");
                    lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                    lblEmpty.setForeground(TEXT_MUTED);
                    lblEmpty.setBorder(new EmptyBorder(0, 10, 0, 0));
                    listPanel.add(lblEmpty);
                } else{
                    for (Map.Entry<Integer, String> entry : resultList.entrySet()) {
                        int id = entry.getKey();
                        String name = entry.getValue();
                        branches.put(id, name);
                    }
                    selectedIndex = branches.isEmpty() ? 0 : branches.keySet().iterator().next();
                    // Xóa nội dung cũ và render lại 
                    renderBranches(listPanel, branches, cinemaId);
                }
                listPanel.revalidate();
                listPanel.repaint();
                
            } catch (Exception e) {
                System.err.println("Lỗi xử lý UI sau khi nhận dữ liệu: " + e.getMessage());
            }
        }
    };

    // Bắt đầu thực thi luồng ngầm
    worker.execute();
    }
    // render danh sách rạp chiếu
    int selectedIndex = 0;
    private void renderBranches(JPanel listPanel, LinkedHashMap<Integer, String> branches, int typecinemaId) {
        listPanel.removeAll(); // Xóa hết các item cũ trước khi vẽ lại
        for (Map.Entry<Integer, String> entry : branches.entrySet()) {
            final int index = entry.getKey();
            String branchName = entry.getValue();
            boolean isSelected = (index == selectedIndex);

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
                    if (typecinemaId == cgvcinemaId) {
                        ImageIcon icon = new ImageIcon(new ImageIcon("image/cgv_logo.png").getImage().getScaledInstance(30, 18, Image.SCALE_SMOOTH));
                        lblName.setIcon(icon);
                    } else if (typecinemaId == galaxycinemaId) {
                        ImageIcon icon = new ImageIcon(new ImageIcon("image/galaxy_logo.png").getImage().getScaledInstance(30, 18, Image.SCALE_SMOOTH));
                        lblName.setIcon(icon);
                    }
                    else if (typecinemaId == lottecinemaId) {
                        ImageIcon icon = new ImageIcon(new ImageIcon("image/lotte_logo.png").getImage().getScaledInstance(30, 18, Image.SCALE_SMOOTH));
                        lblName.setIcon(icon);
                    }
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
                    renderBranches(listPanel, branches, typecinemaId); // Vẽ lại toàn bộ danh sách
                    getlistmovie(index); // Lấy danh sách phim cho rạp được chọn
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
        getlistmovie(selectedIndex);
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
        
        JPanel tagCGV = createFilterTag("CGV", "image/cgv_logo.png", allTags, cgvcinemaId);
        JPanel tagGalaxy = createFilterTag("Galaxy Cinema", "image/galaxy_logo.png", allTags, galaxycinemaId);
        JPanel tagLotte = createFilterTag("Lotte", "image/lotte_logo.png", allTags, lottecinemaId);

        tagsPanel.add(tagCGV);
        tagsPanel.add(tagGalaxy);
        tagsPanel.add(tagLotte);
        selectSingleTag(tagCGV, allTags); //mặc định là cgv

        panel.add(tagsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFilterTag(String name, String iconPath, List<JPanel> allTags, int id) {
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
                getlistcinema(id);
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

    // #region Lấy list phim
    private void getlistmovie(int cinemaId){
        if (grid != null) {
            grid.removeAll();
            JLabel lblLoading = new JLabel("Đang tải các bộ phim, vui lòng chờ...");
            lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblLoading.setForeground(Color.GRAY); // Hoặc màu TEXT_MUTED của bạn
            grid.add(lblLoading);
            grid.revalidate();
            grid.repaint();
        }
        SwingWorker<List<Movie>, Void> worker = new SwingWorker<List<Movie>, Void>() {
            protected List<Movie> doInBackground(){
                List<Movie> movies = new ArrayList<>();
                try (Socket socket = new Socket(ipserver, 4000);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    SecretKey sessionKey = CryptoManager.generateSessionKey();
                    String command = "GET_MOVIES|" + cinemaId;
                    String packet = CryptoManager.encryptClientRequest(command, SERVER_PUBLIC_KEY_B64, sessionKey);
                    out.println(packet);
                    String response = in.readLine();
                    String jsonresponse = CryptoManager.decryptServerResponse(response, sessionKey);
                    JSONObject json = new JSONObject(jsonresponse);
                    String status = json.getString("status"); 
                    if(status.equals("error")){
                        return new ArrayList<>(); 
                    }
                    JSONArray data = json.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject movieJson = data.getJSONObject(i);
//                        System.out.println(">>> CLIENT NHẬN ĐƯỢC: " + movieJson.optString("nameEN") + " | Link: " + movieJson.optString("trailer"));
                        Movie movie = new Movie(
                            movieJson.getInt("id"),
                            movieJson.getString("nameVI"),
                            movieJson.getString("nameEN"),
                            movieJson.getInt("duration"),
                            movieJson.getInt("age"),
                            movieJson.getString("category"),
                            movieJson.getString("desc"),
                            movieJson.getString("director"),
                            movieJson.optString("actors", "N/A"),
                            movieJson.getString("publishDate"),
                            movieJson.optString("imdbRating", "N/A"),    // Đọc điểm IMDb
                            movieJson.optString("rottenRating", "N/A"),  // Đọc điểm Rotten
                            movieJson.getJSONObject("images").getString("type1_size2"),
                            movieJson.getJSONObject("images").getString("banner"),
                            movieJson.optString("trailer","")
                        );
                        movie.cinemaName = movieJson.optString("cinemaName", "");
                        movie.cinemaAddress = movieJson.optString("cinemaAddress", "");
                        movie.provider = movieJson.optString("providerName", movieJson.optString("publisher", ""));
                        JSONArray sessionGroups = movieJson.optJSONArray("sessionGroups");
                        if (sessionGroups != null) {
                            for (int groupIndex = 0; groupIndex < sessionGroups.length(); groupIndex++) {
                                JSONObject groupJson = sessionGroups.getJSONObject(groupIndex);
                                Movie.SessionGroup group = new Movie.SessionGroup(groupJson.optString("groupName", "Không rõ định dạng"));
                                JSONArray sessions = groupJson.optJSONArray("sessions");
                                if (sessions != null) {
                                    for (int sessionIndex = 0; sessionIndex < sessions.length(); sessionIndex++) {
                                        JSONObject sessionJson = sessions.getJSONObject(sessionIndex);
                                        group.sessions.add(new Movie.SessionTime(
                                            sessionJson.optString("purchaseDeadline", ""),
                                            sessionJson.optString("sessionEndTime", "")
                                        ));
                                    }
                                }
                                movie.sessionGroups.add(group);
                            }
                        }
                        movies.add(movie);
                    }

                } catch (Exception e) {
                    System.err.println("Lỗi kết nối tới Server: " + e.getMessage());
                }
                return movies;
            }
            protected void done(){
                try{
                    List<Movie> movies = get();
                    grid.removeAll();
                    if(movies.isEmpty()){
                        JLabel lblEmpty = new JLabel("Không tìm thấy phim nào cho rạp này.");
                        lblEmpty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                        lblEmpty.setForeground(TEXT_MUTED);
                        grid.add(lblEmpty);
                    } else {
                        for(Movie m : movies){
                            grid.add(createMovieCard(m));
                        }
                    }
                    grid.revalidate();
                    grid.repaint();
                } catch (Exception e) {
                    System.err.println("Lỗi xử lý UI sau khi nhận dữ liệu: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel createMovieGridSection() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(BG_MAIN);

        JLabel lblTitle = new JLabel("Phim đang chiếu");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        panel.add(lblTitle, BorderLayout.NORTH);
        grid.setBackground(BG_MAIN);
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
                new MovieDetailsDialog(CinemaFinderUI.this, m, ipserver).setVisible(true);
            }
        });

        // --- 1. Tạo container hỗ trợ xếp chồng (Overlay) ---
        Image loadedImg = null;
        try {
            URL url = new URL(m.posterurl);
            loadedImg = ImageIO.read(url);
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh: " + m.posterurl);
        }
        final Image posterImage = loadedImg; // Phải là final để dùng trong class vô danh

        // --- 2. Tạo Poster Container với hình nền ---
        // FlowLayout.RIGHT sẽ tự động đẩy mọi thứ nhét vào nó sang góc trên-phải
        JPanel posterContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (posterImage != null) {
                    // Vẽ ảnh lấp đầy toàn bộ khung (tràn viền 100%)
                    g.drawImage(posterImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    // Nếu lỗi mạng, hiển thị nền xám
                    g.setColor(new Color(230, 230, 230));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(Color.DARK_GRAY);
                    g.drawString("No Image", getWidth() / 2 - 25, getHeight() / 2);
                }
            }
        };
        posterContainer.setPreferredSize(new Dimension(250, 300));
        // Khóa chiều cao ở 200px, chiều ngang cho phép giãn theo grid
        posterContainer.setMaximumSize(new Dimension(Short.MAX_VALUE, 300)); 

        // --- 3. Tạo Badge điểm số ---
        JPanel ratingBadge = new RoundedPanel(8, new Color(255, 193, 7));
        try {
            // Chỉnh lại kích thước icon ngôi sao cho gọn gàng hơn
            ImageIcon staricon = new ImageIcon(new ImageIcon("image/star.png").getImage().getScaledInstance(12, 12, Image.SCALE_SMOOTH));
            JLabel lblRating = new JLabel(m.imdbRating.equals("N/A") ? "N/A" : m.imdbRating);
            lblRating.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblRating.setIcon(staricon);
            ratingBadge.add(lblRating);
        } catch(Exception e) {
            ratingBadge.add(new JLabel("⭐"));
        }

        // --- 4. Gắn huy hiệu vào container ---
        posterContainer.add(ratingBadge);
        
        card.add(posterContainer);
        // card.add(poster);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);
        info.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel(m.titleVn);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        JLabel subTitle = new JLabel(m.titleEn);
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subTitle.setForeground(TEXT_MUTED);
        
        JLabel details = new JLabel(" " + m.duration + "  •  " + (m.ageRating == 0 ? "P" : m.ageRating + "+"));
        try {
            ImageIcon clockicon = new ImageIcon("image/time.png");
            details.setIcon(clockicon);
        } catch(Exception e) {}
        
        details.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        details.setForeground(TEXT_MUTED);

        JPanel tags = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        tags.setOpaque(false);
        // for (String genre : m.genres) {
        //     RoundedPanel t = new RoundedPanel(5, new Color(227, 242, 253));
        //     JLabel l = new JLabel(genre);
        //     l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        //     l.setForeground(new Color(25, 118, 210));
        //     t.add(l);
        //     tags.add(t);
        // }
        RoundedPanel t = new RoundedPanel(5, new Color(227, 242, 253));
            JLabel l = new JLabel(m.genre);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            l.setForeground(new Color(25, 118, 210));
            t.add(l);
            tags.add(t);

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

        // JPanel status = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        // status.setBackground(new Color(232, 245, 233));
        // JLabel lblStatus = new JLabel("<html><b style='color:#2E7D32;'>● Trạng thái kết nối</b><br/>Đang kết nối với server - Dữ liệu được mã hóa bằng AES-256<br/><span style='color:#757575'>Server đang trích xuất dữ liệu từ: Lotte Cinema, Galaxy Cinema</span></html>");
        // lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // status.add(lblStatus);

        JPanel copy = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
        copy.setBackground(new Color(38, 50, 56));
        JLabel lblCopy = new JLabel("<html><center><span style='color:white;'>© 2026 Cinema Finder - Ứng dụng tìm kiếm phim và lịch chiếu</span><br/><span style='color:#B0BEC5; font-size:10px;'>Dữ liệu được trích xuất từ các website rạp chiếu phim và được mã hóa an toàn</span></center></html>");
        copy.add(lblCopy);

        // footer.add(status);
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