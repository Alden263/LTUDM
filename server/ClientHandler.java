package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private final Socket client;

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
            PrintWriter writer = new PrintWriter(client.getOutputStream(), true)
        ) {
            String clientmessage;
            while ((clientmessage = reader.readLine()) != null) {
                if (clientmessage.trim().contains("GET_CINEMAS")) {
                    String[] parts = clientmessage.split("\\|");
                    if (parts.length == 2) {
                        String typeCinemaId = parts[1];
                        JSONObject cinemas = getcinemas(typeCinemaId);
                        writer.println(cinemas != null ? cinemas.toString() : "ERROR|Không tìm thấy rạp cho loại " + typeCinemaId);
                    } else {
                        writer.println("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_CINEMAS|<cinemaId>");
                    }
                } else if (clientmessage.trim().contains("GET_MOVIES")) {
                    String[] parts = clientmessage.split("\\|");
                    if (parts.length == 2) {
                        String cinemaId = parts[1];
                        JSONObject movies = getmovies(cinemaId);
                        writer.println(movies != null ? movies.toString() : "ERROR|Không tìm thấy phim cho rạp " + cinemaId);
                    } else {
                        writer.println("ERROR|Yêu cầu không hợp lệ. Cú pháp: GET_MOVIES|<cinemaId>");
                    }
                } else {
                    writer.println("ERROR|Lệnh không được nhận dạng. Vui lòng gửi lệnh hợp lệ.");
                }
            }
        } catch (IOException e) {
            System.err.println("[" + threadname + "] Lỗi I/O với client "
                + (client != null ? client.getInetAddress().getHostAddress() : "unknown") + ": " + e.getMessage());
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

            for (int i = 0; i < films.length(); i++) {
                JSONObject film = films.getJSONObject(i);
                String titleEn = film.optString("nameEN", "");
                for (int j = 0; j < listmovie.length(); j++) {
                    JSONObject movie = listmovie.getJSONObject(j);
                    if (movie.optString("name_en", "").contains(titleEn.toUpperCase())) {
                        film.put("actors", movie.optString("actor", "N/A"));
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
}
