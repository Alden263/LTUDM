import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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
                    System.out.println("[" + threadname + "] Nhận từ client " + client.getInetAddress().getHostAddress() + ": " + clientmessage);
                    if("bye".equalsIgnoreCase(clientmessage.trim())){
                        System.out.println("[" + threadname + "] Client " + client.getInetAddress().getHostAddress() + " đã gửi yêu cầu ngắt kết nối.");
                        break;
                    }
                    String response = "Echo: " + clientmessage;
                    writer.println(response);
                }

        } catch(IOException e){
            System.err.println("[" + threadname + "] Lỗi I/O với client " + (client != null ? client.getInetAddress().getHostAddress() : "unknown") + ": " + e.getMessage());
        }
        System.out.println("[" + threadname + "] Kết thúc xử lý client: " + (client != null ? client.getInetAddress().getHostAddress() : "unknown"));
    }
}
