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
                    }
                    else {
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
}
