import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> connectedClients = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    String clientUsername;
    String clientPassword;
    private Map<String, String> userDatabase = new HashMap<>();

    public ClientHandler(Socket socket , Map<String , String>userDatabase) {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.userDatabase = userDatabase;
        } catch (IOException e) {
            closeEverything(socket , bufferedWriter , bufferedReader);
        }
    }


    @Override
    public void run() {

        try {
            this.clientUsername = bufferedReader.readLine();
            this.clientPassword = bufferedReader.readLine();

            if(authenticate(clientUsername, clientPassword)) {
                System.out.println("A new client has connected");
                bufferedWriter.write("Authentication successful.");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                connectedClients.add(this);
                broadcastMessage("SERVER: " + clientUsername + " has joined the chat");

                String msgFromClient;
                while (socket.isConnected()) {
                    msgFromClient = bufferedReader.readLine();
                    if (msgFromClient.equalsIgnoreCase("bye")) {
                        System.exit(0);
                    }

                    broadcastMessage(msgFromClient);
                }
            } else {
                bufferedWriter.write("Authentication failed. Disconnecting...");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            closeEverything(socket , bufferedWriter , bufferedReader);
        }
    }


    public boolean authenticate(String username , String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    public void broadcastMessage(String messageToSend) {
        for(ClientHandler clientHandler : connectedClients) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket , bufferedWriter , bufferedReader);
            }
        }
    }

    public void removeClient() {
        connectedClients.remove(this);
        broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
    }

    public void closeEverything(Socket socket , BufferedWriter writer , BufferedReader reader) {
        removeClient();
        try {
            if(socket != null) socket.close();
            if(writer != null) writer.close();
            if(reader != null) reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
