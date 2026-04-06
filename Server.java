import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class Server {
    private int PORT;
    private static final int n_threads = 10;

    public Server(int PORT) {
        this.PORT = PORT;
    }
    public void getip() throws IOException{
        Socket server = new Socket("thongtindaotao.sgu.edu.vn", 80);
        String localip = server.getLocalAddress().toString().substring(1);
        String api = "https://retoolapi.dev/lKNfWn/data/1";
        String jsondata = "{\"ip\":\"" + localip + "\"}";
        Jsoup.connect(api).ignoreContentType(true).ignoreHttpErrors(true).header("Content-Type", "application/json").requestBody(jsondata).method(Connection.Method.PUT).execute();
    }
    public void start(){
        ExecutorService pool = Executors.newFixedThreadPool(n_threads);
        try(ServerSocket server  = new ServerSocket(PORT)) {
            System.out.println("Server đã lắng nghe trên cổng " + PORT);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Đang shut down ExecutorService...");
                shutdownExecutor(pool);
            }));
            while (!pool.isShutdown()) {
                try{
                    System.out.println("Đang chờ client kết nối...");
                    Socket client = server.accept();
                    System.out.println("Client kết nối: " + client.getInetAddress().getHostAddress());
                    ClientHandler handler = new ClientHandler(client);
                    pool.execute(handler);
                } catch(IOException e){
                    if(pool.isShutdown()){
                        System.out.println("Server socket đã đóng do executor shutdown.");
                        break;
                    }
                    System.err.println("Lỗi khi chấp nhận kết nối client: " + e.getMessage());
                }
            }
            
        } catch(IOException e){
            System.err.println("Không thể khởi động server trên cổng " + PORT + ": " + e.getMessage());
            shutdownExecutor(pool);
        } finally{
            if(!pool.isTerminated()){
                System.out.println("Thực hiện shutdown cuối cùng cho ExecutorService...");
                shutdownExecutor(pool);
            }
            System.out.println("Server đã tắt.");
        }
    }
    public void shutdownExecutor(ExecutorService pool){
        pool.shutdown();
        try{
            if(!pool.awaitTermination(60, TimeUnit.SECONDS)){
                pool.shutdownNow();
                if(!pool.awaitTermination(60, TimeUnit.SECONDS)){
                    System.err.println("ExecutorService không thể dừng hẳn.");
                }
            }
        } catch(InterruptedException e){
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    public static void main(String[] args){
        Server server = new Server(4000);
        try {
            server.getip();
        } catch (IOException e) {
            System.err.println("Lỗi khi lấy IP: " + e.getMessage());
        }
        server.start();
    }
}
