import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Server {

    private ServerSocket serverSocket;
    private static Map<String, String> userDatabase = new HashMap<>();
    private static final String USER_FILE_PATH = "user_credentials.txt";


    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        //userDatabase = new HashMap<>();
//        userDatabase.put("user1" , "password1");
//        userDatabase.put("user2" , "password2");
    }

    public void startServer() {

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("A new client has connected");

                ClientHandler clientHandler = new ClientHandler(clientSocket , userDatabase);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void loadUserDataBase() {
        try (Scanner scanner = new Scanner((new File(USER_FILE_PATH)))){
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split(":");
                if(parts.length == 2) {
                    userDatabase.put(parts[0] , parts[1]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("User database file not found. Creating a new file.");
            saveUserDatabase();
            loadUserDataBase();
        }
    }

    public void saveUserDatabase() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USER_FILE_PATH))){
            writer.println("user1:password1");
            writer.println("user2:password2");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = new ServerSocket(9000);
        Server server = new Server(serverSocket);
        server.loadUserDataBase();
        server.startServer();
    }

}
