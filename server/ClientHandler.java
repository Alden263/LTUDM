package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.LinkedHashMap;

import javax.crypto.SecretKey;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class ClientHandler implements Runnable {
    private Socket client;
    private static final String AES_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String RSA_TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final int T_LEN = 128;
    private static final int IV_LENGTH = 12;
    private static final String SERVER_PRIVATE_KEY_B64 = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDIgdC/jXq9YR1l4POmI3RFq+ZQAD9gWO9Nwvbiu0nOoKM/6+pr0hAYVASV8ES86PFq3Ov9H4RTK0m9r2tG1MW7kpjYoztD2AKp78Uq7jy2IUTolfsUGIRSIz+0bZs32wRDNf1/q0dFIvnB2QZslezK7B2W18XMt8xktZMb7SPys18lhTz0Pl43vp7NJam+azE9iZRlayrE6dn7gu82ACIWXOO55IFqLChKD6JtmcakXhncUXY+rZAy5SzkoAVICtnl35pAZQZiAZEqjv/ApVfLtM5pbpou1cP7CC4sjjeGdRsIRalPOGnCzWr0d/nnICWLkNFmysGE4/s/cxjT9ogvAgMBAAECggEAIV9Z+0f1EehEGFzksNZd1+rQnqgr5EFpMywsI3jAxB+AjHcbwW5pN2NkkAA5sKek5wB2Vt8UCzO9RlEYLeXkM2AnCIfCqXwelWJPEuIsQLQw3/V2oNWj0HgFdmZ320gc4YLu+nzrk9pKb7VLmG8rxmqeUij/xx8gafK+1XsW7KIa26py/YmsR+F8lMpzpwZ6VZFS/XY7ONUuZUU0K1+jZtEyPFk6/EsX6JpeVgC9nigYn0AJi+Xy7/C0pPbDu2vk7CPzw8C72mkp3aqhZI8hiLQMKnRB7GK8f6ublTmUtZRdSWYv9uTljkvwLOpMBoNt4JFL7I19MHRJ9/+BP2/84QKBgQDlB1sXZBVxjiQpWYSGtvMbTu/PYqntLu4RWgzQpg1OSFIKIcwobknqWRifTAMw6o2lbpE5D+wn7+7WLfP1ituJ1rgMwltbdSFxleu6EbP4pJNcCUcPc1Aqd7inzOd1p1mJ+yatH/88qePM+2OAQ+qXkyMuKc9Gs1bpIlEPMBE04wKBgQDgHpp0O0fEWmyi7ZjdhrcLarZ7iMf7zPxFbaa2oMHCSUmg21KpBkFVats/9ESLzQTFvZ+9OyiywTvjHOrBHQqBV+NOmjBalmRLWrRhNURnIt5+JTC7oHhXpjrjwsHXk5srt7THfp9NgORlWH5way2Q4XKyBvhvUfBVsqSXCtlNRQKBgQCmJyz9jj1EUYOIgAn2FZnO0PiHMqPmj2plKjgr0pvKlAr4kMZk+oYjPpnvTxztiuxR/SHcDOIdoyJZPaNEGoXkvOJvVz0h35rwpXwLVDaFhxfk6FImMlkRRiOvkKbGoy8BPDQ12wW3Q+Ug26u27Q6vAi6+mdRnDUpa/etiolOJUwKBgHqgA/5omQHUP/B9c9BxbyGuB55b3p/lLnqGXls6Zgl5s/FxTy7wHzsvNjst2Xtsd5oCAK62+bOkirHfFosG6sKOZe6R6rcF5mZcgKlXTc/ogjge/SULzgyXBU2tOAZN2u72mtE0dNEhHtZcrwGvgsTstaa2raOq/4bEMc36v57JAoGBAIrjB/Zaa0J+uAvOxM9h9wDSajHWoonqZwc7A76o2i9KxPomOrNcQ/+zuk+45KuEPIqlQ1+dbnYGJjYI29SuHl/OlbYslwWC5jShf7frRGVgjjFOjbgF0OlEVFQZus11Mk0uiYs7+GomsLkFz3Me7jWWW48cGDHIK8liq7ftEBq7";
    public ClientHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        String threadname = Thread.currentThread().getName();
        System.out.println("[" + threadname + "] Bắt đầu xử lý client: " + client.getInetAddress().getHostAddress());

        try (
            Socket autoClosableSocket = this.client;
            BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter writer = new PrintWriter(client.getOutputStream(),true)){
                String clientmessage;
                while ((clientmessage = reader.readLine()) != null) {
                    // System.out.println("[" + threadname + "] Nhận từ client " + client.getInetAddress().getHostAddress() + ": " + clientmessage);
                    // if("bye".equalsIgnoreCase(clientmessage.trim())){
                    //     System.out.println("[" + threadname + "] Client " + client.getInetAddress().getHostAddress() + " đã gửi yêu cầu ngắt kết nối.");
                    //     break;
                    // }
                    Object[] decrypted = CryptoManager.decryptClientRequest(clientmessage, SERVER_PRIVATE_KEY_B64);
                    String command = (String) decrypted[0];
                    SecretKey sessionKey = (SecretKey) decrypted[1];
                    String responsePacket = "";
                    // System.out.println(clientmessage);
                    if(command.trim().contains("GET_CINEMAS")){
                        String[] parts = command.split("\\|");
                        if(parts.length == 2){
                            String type_cinemaId = parts[1];
                            JSONObject cinemas = getcinemas(type_cinemaId);
                            if(cinemas != null){
                                // System.out.println("[" + threadname + "] Đã tìm thấy rạp cho loại " + type_cinemaId + ": " + cinemas.toString());
                                responsePacket = CryptoManager.encryptServerResponse(cinemas.toString(), sessionKey);
                                writer.println(responsePacket);
                            } else {
                                responsePacket = CryptoManager.encryptServerResponse("ERROR|Không tìm thấy rạp cho loại " + type_cinemaId, sessionKey);
                                writer.println(responsePacket);
                            }
                        } else{
                            responsePacket = CryptoManager.encryptServerResponse("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_CINEMAS|<type_cinemaId>", sessionKey);
                            writer.println(responsePacket);
                        }
                    } else if(command.trim().contains("GET_MOVIES")){
                        String[] parts = command.split("\\|");
                        if(parts.length == 2){
                            String cinemaId = parts[1];
                            JSONObject movies = getmovies(cinemaId);
                            if(movies != null){
                                System.out.println("[" + threadname + "] Đã tìm thấy phim cho rạp " + cinemaId );
                                responsePacket = CryptoManager.encryptServerResponse(movies.toString(), sessionKey);
                                writer.println(responsePacket);
                            } else {
                                responsePacket = CryptoManager.encryptServerResponse("ERROR|Không tìm thấy phim cho rạp " + cinemaId, sessionKey);
                                writer.println(responsePacket);
                            }
                        } else {
                            responsePacket = CryptoManager.encryptServerResponse("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_MOVIES|<cinemaId>", sessionKey);
                            writer.println(responsePacket);
                        }
                    } else if(command.trim().contains("GET_TRAILER")){
                        String[] parts = command.split("\\|");
                        if(parts.length == 2){
                            String ytb = parts[1];
                            String trailerLink = getlinkytb(ytb);
                            if(trailerLink != null && !"error".equals(trailerLink)){
                                System.out.println("[" + threadname + "] Đã tìm thấy link trailer cho " + ytb + ": " + trailerLink);
                                responsePacket = CryptoManager.encryptServerResponse(trailerLink, sessionKey);
                                writer.println(responsePacket);
                            } else {
                                responsePacket = CryptoManager.encryptServerResponse("ERROR|Không thể lấy link trailer cho " + ytb, sessionKey);
                                writer.println(responsePacket);
                            }
                        } else {
                            responsePacket = CryptoManager.encryptServerResponse("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_TRAILER|<youtube_url>", sessionKey);
                            writer.println(responsePacket);
                        }
                    } else if (command.trim().contains("GET_MOVIE_EXTRA")) {
                    String[] parts = command.split("\\|");
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
                        responsePacket = CryptoManager.encryptServerResponse(result.toString(), sessionKey);
                        
                        writer.println(responsePacket);
                    }
                }else {
                    responsePacket = CryptoManager.encryptServerResponse("ERROR|Lệnh không được nhận dạng. Vui lòng gửi lệnh hợp lệ.", sessionKey);
                        writer.println(responsePacket);
                }
                
                }

        } catch(Exception e){
            System.err.println("[" + threadname + "] Lỗi I/O với client " + (client != null ? client.getInetAddress().getHostAddress() : "unknown") + ": " + e.getMessage());
        }

        System.out.println("[" + threadname + "] Kết thúc xử lý client: "
            + (client != null ? client.getInetAddress().getHostAddress() : "unknown"));
    }

    public JSONObject getcinemas(String typeCinemaId) {
        try {
            LinkedHashMap<String, String> cinemas = new LinkedHashMap<>();
            String api = "https://zlp-movie-api.zalopay.vn/v2/movie/web/data/pcinemas?isReturnCinemas=true&locationId=1";
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());

            if (!json.has("data") || json.isNull("data")) {
                return new JSONObject().put("status", "error").put("message", "Không có dữ liệu");
            }

            JSONArray data = json.getJSONArray("data");
            for (int i = 0; i < data.length(); i++) {
                JSONObject cinema = data.getJSONObject(i);
                if (cinema.getInt("id") == Integer.parseInt(typeCinemaId)) {
                    JSONArray cinemaList = cinema.getJSONArray("cinemas");
                    for (int j = 0; j < cinemaList.length(); j++) {
                        JSONObject cine = cinemaList.getJSONObject(j);
                        cinemas.put(String.valueOf(cine.getInt("id")), cine.getString("name"));
                    }
                }
            }

            if (cinemas.isEmpty()) {
                return new JSONObject().put("status", "error").put("message", "Không tìm thấy rạp nào cho loại " + typeCinemaId);
            }
            return new JSONObject().put("status", "success").put("data", cinemas);
        } catch (IOException e) {
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }

    public JSONObject getmovies(String cinemaId) {
        try {
            LocalDate selectedDate = LocalDate.now();
            String api = "https://zlp-movie-api.zalopay.vn/v2/movie/web/data/sessions?cinemaId=" + cinemaId + "&date=" + selectedDate;
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());

            if (!json.has("data") || json.isNull("data")) {
                return new JSONObject().put("status", "error").put("message", "Không có dữ liệu");
            }

            JSONObject data = json.getJSONObject("data");
            JSONArray films = data.getJSONArray("films");
            JSONArray pCinemas = data.optJSONArray("pCinemas");
            JSONObject cinemaInfo = findCinemaInfo(pCinemas, cinemaId);

            for (int i = 0; i < films.length(); i++) {
                JSONObject film = films.getJSONObject(i);
                film.put("selectedDate", selectedDate.toString());
                film.put("cinemaName", cinemaInfo.optString("name", ""));
                film.put("cinemaAddress", cinemaInfo.optString("address", ""));
                film.put("providerName", cinemaInfo.optString("providerName", film.optString("publisher", "")));
                film.put("sessionGroups", filterSessionGroupsByDay(film.optJSONArray("sessionGroups"), selectedDate));
            }

            return getactor(films);
        } catch (IOException e) {
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }

    private JSONObject findCinemaInfo(JSONArray pCinemas, String cinemaId) {
        JSONObject result = new JSONObject();
        if (pCinemas == null) {
            return result;
        }

        for (int i = 0; i < pCinemas.length(); i++) {
            JSONObject provider = pCinemas.getJSONObject(i);
            JSONArray cinemas = provider.optJSONArray("cinemas");
            if (cinemas == null) {
                continue;
            }

            for (int j = 0; j < cinemas.length(); j++) {
                JSONObject cinema = cinemas.getJSONObject(j);
                if (String.valueOf(cinema.optInt("id")).equals(cinemaId)) {
                    result.put("name", cinema.optString("name", ""));
                    result.put("address", cinema.optString("address", ""));
                    result.put("providerName", provider.optString("name", ""));
                    return result;
                }
            }
        }
        return result;
    }

    private JSONArray filterSessionGroupsByDay(JSONArray sessionGroups, LocalDate selectedDate) {
        JSONArray filteredGroups = new JSONArray();
        if (sessionGroups == null) {
            return filteredGroups;
        }

        String selectedDay = selectedDate.toString();
        for (int i = 0; i < sessionGroups.length(); i++) {
            JSONObject group = sessionGroups.getJSONObject(i);
            JSONArray sessions = group.optJSONArray("sessions");
            JSONArray filteredSessions = new JSONArray();

            if (sessions != null) {
                for (int j = 0; j < sessions.length(); j++) {
                    JSONObject session = sessions.getJSONObject(j);
                    String purchaseDeadline = session.optString("purchaseDeadline", "");
                    if (purchaseDeadline.startsWith(selectedDay)) {
                        filteredSessions.put(session);
                    }
                }
            }

            if (filteredSessions.length() > 0) {
                JSONObject groupCopy = new JSONObject(group.toString());
                groupCopy.put("sessions", filteredSessions);
                filteredGroups.put(groupCopy);
            }
        }

        return filteredGroups;
    }

    public JSONObject getactor(JSONArray films) {
        try {
            String api = "https://cinestar.com.vn/_next/data/YZahHhMaxCbNZW34iKtBz/showtimes.json";
            Document doc = Jsoup.connect(api).ignoreContentType(true).method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());
            JSONArray listmovie = json.getJSONObject("pageProps").getJSONObject("res").getJSONArray("listMovie");
            
            for(int i=0; i < films.length(); i++){
                JSONObject film = films.getJSONObject(i);
                String titleEn = film.getString("nameEN");

                String mediaID = film.optString("mediaId", "");
//                System.out.println(mediaID);
                String trailerURL="https://www.youtube.com/watch?v="+mediaID;
                film.put("trailer",trailerURL);
                String titleVn = film.getString("nameVI");
                titleVn = titleVn.split("-")[0].trim();

                // --- GỌI API LẤY ĐIỂM Ở ĐÂY ---
                JSONObject ratings = getTMDBData(titleEn);
                film.put("imdbRating", ratings.getString("imdb"));
                film.put("rottenRating", ratings.getString("rotten"));

                // Logic lấy actor của ông giữ nguyên
                for(int j=0; j < listmovie.length(); j++){
                    JSONObject movie = listmovie.getJSONObject(j);
                    if (movie.optString("name_en", "").replaceAll(" ", "").contains(titleEn.toUpperCase().replaceAll(" ", "")) || movie.optString("name_vn", "").replaceAll(" ", "").contains(titleVn.toUpperCase().replaceAll(" ", ""))) {
                        film.put("actors", movie.getString("actor"));
                        break;
                    }
                }
                if (!film.has("actors")) {
                    film.put("actors", "N/A");
                }
            }

            return new JSONObject().put("data", films).put("status", "success");
        } catch (IOException e) {
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }
    public JSONObject getSessionGroups(String cinemaId, String date) {
        try {
            String api = "https://zlp-movie-api.zalopay.vn/v2/movie/web/data/sessions?cinemaId=" 
                + cinemaId + "&date=" + date;
            Document doc = Jsoup.connect(api).ignoreContentType(true)
                .method(Connection.Method.GET).execute().parse();
            JSONObject json = new JSONObject(doc.text());

            if (!json.has("data") || json.isNull("data")) {
                return new JSONObject().put("status", "error").put("message", "Không có dữ liệu");
            }

            JSONObject data = json.getJSONObject("data");
            JSONArray films = data.optJSONArray("films");
            
            if (films == null) {
                 return new JSONObject().put("status", "success").put("data", new JSONArray());
            }

            // Lọc và giữ lại các trường cần thiết
            JSONArray resultFilms = new JSONArray();
            for (int i = 0; i < films.length(); i++) {
                JSONObject film = films.getJSONObject(i);
                JSONObject item = new JSONObject();
                
                item.put("nameEN", film.optString("nameEN", ""));
                item.put("publishDate", formatDate(film.optString("publishDate", "")));

                JSONArray rawSessionGroups = film.optJSONArray("sessionGroups");
                JSONArray processedGroups = new JSONArray();

                if (rawSessionGroups != null) {
                    for (int j = 0; j < rawSessionGroups.length(); j++) {
                        JSONObject rawGroup = rawSessionGroups.getJSONObject(j);
                        JSONObject groupItem = new JSONObject();
                        
                        groupItem.put("groupName", rawGroup.optString("groupName", ""));
                        
                        JSONArray rawSessions = rawGroup.optJSONArray("sessions");
                        JSONArray processedSessions = new JSONArray();
                        
                        if (rawSessions != null) {
                            for (int k = 0; k < rawSessions.length(); k++) {
                                JSONObject rawSession = rawSessions.getJSONObject(k);
                                JSONObject sessionItem = new JSONObject();
                                
                                String rawStartTime = rawSession.optString("sessionTime", "");
                                String rawEndTime = rawSession.optString("sessionEndTime", "");
                                String rawDeadline = rawSession.optString("purchaseDeadline", "");
                                
                                // Lấy đầy đủ Giờ và Ngày Tháng Năm (VD: 18:00 17/04/2026)
                                sessionItem.put("sessionTime", formatDateTime(rawStartTime));
                                sessionItem.put("sessionEndTime", formatDateTime(rawEndTime));
                                sessionItem.put("purchaseDeadline", formatDateTime(rawDeadline));
                                
                                // Chỉ lấy giờ để hiển thị UI (VD: 18:00 ~ 20:10)
                                sessionItem.put("displayTime", formatTime(rawStartTime) + " ~ " + formatTime(rawEndTime));
                                
                                processedSessions.put(sessionItem);
                            }
                        }
                        groupItem.put("sessions", processedSessions);
                        processedGroups.put(groupItem);
                    }
                }
                item.put("sessionGroups", processedGroups);
                resultFilms.put(item);
            }

            return new JSONObject().put("status", "success").put("data", resultFilms);
        } catch (IOException e) {
            return new JSONObject().put("status", "error").put("message", "Lỗi server: " + e.getMessage());
        }
    }

    private String formatDate(String ymd) {
        try {
            if (ymd != null && ymd.length() >= 10) {
                return java.time.LocalDate.parse(ymd.substring(0, 10))
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            return ymd;
        } catch (Exception e) { 
            return ymd; 
        }
    }

    // Hàm mới: Format đầy đủ Giờ phút và Ngày/Tháng/Năm
    private String formatDateTime(String dt) {
        // "2026-04-17 20:10:00" -> "20:10 17/04/2026"
        try {
            if (dt == null || dt.isEmpty()) return dt;
            return java.time.LocalDateTime.parse(dt.replace(" ", "T"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
        } catch (Exception e) { 
            return dt; 
        }
    }

    // Hàm cũ: Chỉ lấy Giờ phút (dùng để build chuỗi displayTime)
    private String formatTime(String dt) {
        // "2026-04-17 20:10:00" -> "20:10"
        try {
            if (dt == null || dt.isEmpty()) return dt;
            return java.time.LocalDateTime.parse(dt.replace(" ", "T"))
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) { 
            return dt; 
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
                .timeout(10000)
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
                .timeout(10000)
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
                .timeout(10000)
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
    public String getlinkytb(String ytb){
        StringBuilder output = new StringBuilder();
        try{
            File exeFile = new File("resource/yt-dlp.exe");
            String path = exeFile.getAbsolutePath();
            File denoFile = new File("resource/deno.exe");
            String denoPath = denoFile.getAbsolutePath();
            ProcessBuilder builder = new ProcessBuilder(
                path,
                "--js-runtimes", "deno:" + denoPath,
                "-f", "18", // ID 136 là mp4 720p (có hình).
                "-g",
                ytb
            );
            Process process = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n"); // Lưu link vào output
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Đã lấy được link trailer: " + output.toString().trim());
                return output.toString().trim(); // Trả về link Youtube trực tiếp
            } else {
                // Đọc lỗi nếu lấy link thất bại (VD: link sai, video private...)
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                StringBuilder errorOutput = new StringBuilder();
                String errLine;
                while ((errLine = errorReader.readLine()) != null) {
                    errorOutput.append(errLine).append("\n");
                }
                System.err.println("Lỗi yt-dlp: " + errorOutput.toString());
                return "error"; // Trả về mã lỗi cho Client biết
            }
        } catch(Exception e){
            System.err.println("Lỗi khi lấy link trailer: " + e.getMessage());
            return "error"; // Trả về mã lỗi cho Client biết
        }
    }
}
