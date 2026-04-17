package server;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ClientHandler implements Runnable {
    private Socket client;
    public ClientHandler(Socket client) {
        this.client = client;
    }
    public void run(){
        String threadname = Thread.currentThread().getName();
        System.out.println("[" + threadname + "] Bắt đầu xử lý client: " + client.getInetAddress().getHostAddress());
        try(Socket AutoclosableSocket = this.client;
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(),true)){
                String clientmessage;
                while ((clientmessage = reader.readLine()) != null) {
                    // System.out.println("[" + threadname + "] Nhận từ client " + client.getInetAddress().getHostAddress() + ": " + clientmessage);
                    // if("bye".equalsIgnoreCase(clientmessage.trim())){
                    //     System.out.println("[" + threadname + "] Client " + client.getInetAddress().getHostAddress() + " đã gửi yêu cầu ngắt kết nối.");
                    //     break;
                    // }
                    if(clientmessage.trim().contains("GET_CINEMAS")){
                        String[] parts = clientmessage.split("\\|");
                        if(parts.length == 2){
                            String type_cinemaId = parts[1];
                            JSONObject cinemas = getcinemas(type_cinemaId);
                            if(cinemas != null){
                                System.out.println("[" + threadname + "] Đã tìm thấy rạp cho loại " + type_cinemaId + ": " + cinemas.toString());
                                writer.println(cinemas.toString());
                            } else {
                                writer.println("ERROR|Không tìm thấy rạp cho loại " + type_cinemaId);
                            }
                        } else writer.println("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_CINEMAS|<cinemaId>");
                    } else if(clientmessage.trim().contains("GET_MOVIES")){
                        String[] parts = clientmessage.split("\\|");
                        if(parts.length == 2){
                            String cinemaId = parts[1];
                            JSONObject movies = getmovies(cinemaId);
                            if(movies != null){
                                System.out.println("[" + threadname + "] Đã tìm thấy phim cho rạp " + cinemaId + ": " + movies.toString());
                                writer.println(movies.toString());
                            } else {
                                writer.println("ERROR|Không tìm thấy phim cho rạp " + cinemaId);
                            }
                        } else writer.println("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_MOVIES|<cinemaId>");
                    } else if (clientmessage.trim().startsWith("GET_MOVIE_EXTRA")) {
                    String[] parts = clientmessage.split("\\|");
                    if (parts.length == 2) {
                        String movieName = parts[1];
                        JSONObject result = new JSONObject();
                        

                        // 2. Khen Phim
                        JSONObject kpData = getKhenPhimReview(movieName);
                        result.put("kp_title", kpData.optString("title", "Xem review chi tiết"));
                        result.put("kp_url", kpData.optString("url", ""));

                        // 3. Moveek
                        JSONObject moveekData = getMoveekReview(movieName);
                        result.put("moveek_title", moveekData.optString("title", "Đọc review trên Moveek"));
                        result.put("moveek_url", moveekData.optString("url", ""));

                        // 4. VÉT CẠN TỪ BÁO MẠNG (MỚI)
                        JSONObject generalData = getGeneralReview(movieName);
                        result.put("general_source", generalData.optString("source", "Tìm kiếm trên Web"));
                        result.put("general_title", generalData.optString("title", "Xem bài đánh giá liên quan"));
                        result.put("general_url", generalData.optString("url", ""));
                        
                        writer.println(result.toString());
                    }
                }else {
                    writer.println("ERROR|Lệnh không được nhận dạng. Vui lòng gửi lệnh hợp lệ.");
                }
                
                }

        } catch(IOException e){
            System.err.println("[" + threadname + "] Lỗi I/O với client " + (client != null ? client.getInetAddress().getHostAddress() : "unknown") + ": " + e.getMessage());
        }
        System.out.println("[" + threadname + "] Kết thúc xử lý client: " + (client != null ? client.getInetAddress().getHostAddress() : "unknown"));
    }
    public JSONObject getcinemas(String type_cinemaId){
        try{
            LinkedHashMap<String, String> cinemas = new LinkedHashMap<>();
            String api = "https://zlp-movie-api.zalopay.vn/v2/movie/web/data/pcinemas?isReturnCinemas=true&locationId=1";
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());

            // Kiểm tra xem trường data có tồn tại hay không
            if(!json.has("data") || json.isNull("data")){
                System.err.println("Lỗi: Phản hồi API không chứa trường 'data'.");
                 return new JSONObject().put("status", "error").put("message", "Không có dữ liệu");
            }

            JSONArray data = json.getJSONArray("data");
            for(int i = 0; i < data.length(); i++){
                JSONObject cinema = data.getJSONObject(i);
                if(cinema.getInt("id") == Integer.parseInt(type_cinemaId)){
                    JSONArray cinemalist = cinema.getJSONArray("cinemas");
                    for(int j = 0; j < cinemalist.length(); j++){
                        JSONObject cine = cinemalist.getJSONObject(j);
                        cinemas.put(String.valueOf(cine.getInt("id")), cine.getString("name"));
                    }
                }
            }
            if(cinemas.isEmpty()){
                System.err.println("Không tìm thấy rạp nào cho loại " + type_cinemaId);
                return new JSONObject().put("status", "error").put("message", "Không tìm thấy rạp nào cho loại " + type_cinemaId);
            }
            return new JSONObject().put("status", "success").put("data", cinemas);
        } catch(IOException e){
            System.err.println("Lỗi khi lấy danh sách rạp: " + e.getMessage());
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());

        }
    }
    public JSONObject getmovies(String cinemaId){
        try{
            String api = "https://zlp-movie-api.zalopay.vn/v2/movie/web/data/sessions?cinemaId=" + cinemaId + "&date=" + LocalDate.now().toString();
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());
            if(!json.has("data") || json.isNull("data")){
                System.err.println("Lỗi: Phản hồi API không chứa trường 'data'.");
                 return new JSONObject().put("status", "error").put("message", "Không có dữ liệu");
            }

            JSONObject data = json.getJSONObject("data");
            JSONArray films = data.getJSONArray("films");
            return getactor(films);
            // return new JSONObject().put("films", films).put("status", "success");
        } catch(IOException e){
            System.err.println("Lỗi khi lấy danh sách phim: " + e.getMessage());
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }
    public JSONObject getactor(JSONArray films){
        try{
            String api = "https://cinestar.com.vn/_next/data/YZahHhMaxCbNZW34iKtBz/showtimes.json";
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());
            JSONArray listmovie = json.getJSONObject("pageProps").getJSONObject("res").getJSONArray("listMovie");
            
            for(int i=0; i < films.length(); i++){
                JSONObject film = films.getJSONObject(i);
                String titleEn = film.getString("nameEN");

                // --- GỌI API LẤY ĐIỂM Ở ĐÂY ---
                JSONObject ratings = getTMDBData(titleEn);
                film.put("imdbRating", ratings.getString("imdb"));
                film.put("rottenRating", ratings.getString("rotten"));

                // Logic lấy actor của ông giữ nguyên
                for(int j=0; j < listmovie.length(); j++){
                    JSONObject movie = listmovie.getJSONObject(j);
                    if(movie.getString("name_en").contains(titleEn.toUpperCase())){
                        film.put("actors", movie.getString("actor"));
                        break;
                    }
                }
            }
            return new JSONObject().put("data", films).put("status", "success");
        } catch(IOException e){
            System.err.println("Lỗi khi lấy danh sách diễn viên: " + e.getMessage());
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }
    public JSONObject getTMDBData(String titleEn) {
    try {
        String apiKey = "c0370d7716aa462edbbb5b4edc157fa8"; // Thay key của bạn vào đây
        String encodedTitle = java.net.URLEncoder.encode(titleEn, "UTF-8");
        

        String api = "https://api.themoviedb.org/3/search/movie?api_key=" + apiKey + "&query=" + encodedTitle;

        Document doc = Jsoup.connect(api).ignoreContentType(true).execute().parse();
        JSONObject json = new JSONObject(doc.text());
        JSONArray results = json.getJSONArray("results");

        JSONObject result = new JSONObject();
        if (results.length() > 0) {
            // Lấy kết quả đầu tiên (thường là chính xác nhất)
            JSONObject topMovie = results.getJSONObject(0);
            
            double voteAverage = topMovie.getDouble("vote_average");
            result.put("imdb", String.format("%.1f", voteAverage)); 
            

            result.put("rotten", topMovie.getInt("vote_count") + " votes");
        } else {
            result.put("imdb", "N/A");
            result.put("rotten", "N/A");
        }
        return result;
    } catch (Exception e) {
        return new JSONObject().put("imdb", "N/A").put("rotten", "N/A");
    }

}



// --- HÀM CÀO LINK KHEN PHIM ---
public JSONObject getKhenPhimReview(String movieName) {
    try {
        // Lần 1: Tìm bằng tên đầy đủ
        JSONObject result = searchKhenPhim(movieName);
        if (result.has("url")) return result;

        // Lần 2: Nếu tên có dấu ":" hoặc " - ", cắt lấy phần tên chính và tìm lại
        if (movieName.contains(":")) {
            String shortName = movieName.split(":")[0].trim();
            result = searchKhenPhim(shortName);
            if (result.has("url")) return result;
        } else if (movieName.contains(" - ")) {
            String shortName = movieName.split(" - ")[0].trim();
            result = searchKhenPhim(shortName);
            if (result.has("url")) return result;
        }
    } catch (Exception e) {
        System.err.println("Lỗi cào Khen Phim: " + e.getMessage());
    }
    return new JSONObject(); // Trả về JSON rỗng nếu tịt ngòi
}

// Hàm phụ trợ tách logic search cho gọn
private JSONObject searchKhenPhim(String query) throws Exception {
    String searchUrl = "https://khenphim.com/?s=" + java.net.URLEncoder.encode(query, "UTF-8");
    Document doc = Jsoup.connect(searchUrl)
            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
            .timeout(10000)
            .get();

    // Lấy TẤT CẢ các thẻ tiêu đề bài viết
    org.jsoup.select.Elements elements = doc.select("h3.entry-title a, h3.td-module-title a, .td-module-title a, h2.entry-title a");
    
    // Đổi tên phim cần tìm sang chữ thường để dễ so sánh
    String queryLower = query.toLowerCase();

    for (org.jsoup.nodes.Element el : elements) {
        String title = el.text();
        
        // CHỐT CHẶN: Tiêu đề bài review bắt buộc phải chứa tên phim
        if (title.toLowerCase().contains(queryLower)) {
            return new JSONObject().put("title", title).put("url", el.attr("href"));
        }
    }
    
    // Nếu quét hết mà không có bài nào chứa tên phim -> Báo rỗng
    return new JSONObject();
}
// --- HÀM CÀO LINK MOVEEK ---
public JSONObject getMoveekReview(String movieName) {
    try {
        // Lần 1: Tìm bằng tên đầy đủ
        JSONObject result = searchMoveek(movieName);
        if (result.has("url")) return result;

        // Lần 2: Nếu tên có dấu ":" hoặc " - ", cắt lấy phần tên chính và tìm lại
        if (movieName.contains(":")) {
            return searchMoveek(movieName.split(":")[0].trim());
        } else if (movieName.contains(" - ")) {
            return searchMoveek(movieName.split(" - ")[0].trim());
        }
    } catch (Exception e) {
        System.err.println("Lỗi cào Moveek: " + e.getMessage());
    }
    return new JSONObject();
}

/// Thuật toán "Động cơ kép": Vượt tường lửa Moveek bằng DuckDuckGo
private JSONObject searchMoveek(String query) throws Exception {
    String queryLower = query.toLowerCase();

    // --- BƯỚC 1: Tìm trực tiếp trên Moveek ---
    try {
        String searchUrl = "https://moveek.com/tim-kiem/?q=" + java.net.URLEncoder.encode(query, "UTF-8");
        Document doc = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .timeout(5000)
                .get();

        org.jsoup.select.Elements links = doc.select("a[href*='/bai-viet/']");

        for (org.jsoup.nodes.Element link : links) {
            String url = link.attr("abs:href");
            String title = link.text().trim();
            if (title.isEmpty() && link.parent() != null) title = link.parent().text().trim();

            // CHỐT CHẶN 1: Tiêu đề hoặc URL phải chứa tên phim
            if (title.toLowerCase().contains(queryLower) || url.toLowerCase().contains(queryLower.replace(" ", "-"))) {
                if (title.isEmpty()) title = "Review chi tiết trên Moveek";
                return new JSONObject().put("title", title).put("url", url);
            }
        }
    } catch (Exception e) {
        // Lỗi thì âm thầm nhảy sang Bước 2
    }

    // --- BƯỚC 2: Fallback qua DuckDuckGo ---
    try {
        String ddgUrl = "https://html.duckduckgo.com/html/?q=site:moveek.com/bai-viet/+" + java.net.URLEncoder.encode(query, "UTF-8");
        Document doc = Jsoup.connect(ddgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(7000)
                .get();

        // Quét tất cả các khối kết quả
        org.jsoup.select.Elements results = doc.select("div.result");
        
        for (org.jsoup.nodes.Element res : results) {
            org.jsoup.nodes.Element linkEl = res.selectFirst("a.result__url");
            org.jsoup.nodes.Element titleEl = res.selectFirst("h2.result__title a");

            if (linkEl != null && titleEl != null) {
                String title = titleEl.text();
                
                // CHỐT CHẶN 2: Tiêu đề trên DuckDuckGo phải chứa tên phim
                if (title.toLowerCase().contains(queryLower)) {
                    String url = linkEl.attr("href");
                    if (url.contains("uddg=")) {
                        url = java.net.URLDecoder.decode(url.split("uddg=")[1].split("&")[0], "UTF-8");
                    }
                    return new JSONObject().put("title", title).put("url", url);
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Lỗi luồng DuckDuckGo: " + e.getMessage());
    }

    return new JSONObject();
}
// THUẬT TOÁN VÉT CẠN: Tự động tìm review trên mọi trang báo mạng (ĐÃ LỌC TRÙNG)
public JSONObject getGeneralReview(String movieName) {
    try {
        // Cú pháp tìm kiếm: "Review phim Tên-Phim"
        String query = "Review phim " + movieName;
        String ddgUrl = "https://html.duckduckgo.com/html/?q=" + java.net.URLEncoder.encode(query, "UTF-8");
        
        Document doc = Jsoup.connect(ddgUrl)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .timeout(7000)
                .get();

        org.jsoup.select.Elements results = doc.select("div.result");
        
        for (org.jsoup.nodes.Element res : results) {
            org.jsoup.nodes.Element linkEl = res.selectFirst("a.result__url");
            org.jsoup.nodes.Element titleEl = res.selectFirst("h2.result__title a");

            if (linkEl != null && titleEl != null) {
                String url = linkEl.attr("href");
                if (url.contains("uddg=")) {
                    url = java.net.URLDecoder.decode(url.split("uddg=")[1].split("&")[0], "UTF-8");
                }
                
                // BỘ LỌC CHỐNG TRÙNG LẶP: Bỏ qua Khen Phim, Moveek và các mạng xã hội
                if (!url.contains("youtube.com") && 
                    !url.contains("facebook.com") && 
                    !url.contains("tiktok.com") && 
                    !url.contains("moveek.com") && 
                    !url.contains("khenphim.com")) {
                    
                    // Tự động trích xuất tên Trang Web từ Link (VD: vnexpress.net)
                    String domain = new java.net.URL(url).getHost().replace("www.", "");
                    return new JSONObject()
                            .put("title", titleEl.text())
                            .put("url", url)
                            .put("source", "Bài viết từ " + domain); 
                }
            }
        }
    } catch (Exception e) {
        System.err.println("Lỗi luồng tìm kiếm vét cạn: " + e.getMessage());
    }
    return new JSONObject();
}
}