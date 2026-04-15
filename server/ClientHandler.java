package server;
import java.io.BufferedReader;
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
                    Object[] decrypted = CryptoManager.decryptClientRequest(clientmessage, SERVER_PRIVATE_KEY_B64);
                    String command = (String) decrypted[0];
                    SecretKey sessionKey = (SecretKey) decrypted[1];
                    // System.out.println(clientmessage);
                    if(command.trim().contains("GET_CINEMAS")){
                        String[] parts = command.split("\\|");
                        String responsePacket = "";
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
                        String responsePacket = "";
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
                    }
                    else {
                        String responsePacket = CryptoManager.encryptServerResponse("ERROR|Lệnh không được nhận dạng. Vui lòng gửi lệnh hợp lệ.", sessionKey);
                        writer.println(responsePacket);
                    }
                }

        } catch(Exception e){
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
