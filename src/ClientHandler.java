import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
            //this.clientUsername = bufferedReader.readLine();
            //this.clientPassword = bufferedReader.readLine();
            //System.out.println(clientUsername + " : " + clientPassword);
            //connectedClients.add(this);
           // broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
            this.userDatabase = userDatabase;
//            userDatabase.put("user1", "password1"); // Sample user and password, replace with your own database
//            userDatabase.put("user2", "password2"); // Sample user and password, replace with your own database
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try {
            //bufferedWriter.write("enter your name:");
            this.clientUsername = bufferedReader.readLine();
               // System.out.println(clientUsername);
            //bufferedWriter.write("enter your password:");
            this.clientPassword = bufferedReader.readLine();



            if(authenticate(clientUsername, clientPassword)) {
                bufferedWriter.write("Authentication successful. You can chat now.");
                bufferedWriter.newLine();
                bufferedWriter.flush();

                connectedClients.add(this);
                System.out.println(this.clientUsername);
                broadcastMessage("SERVER: " + clientUsername + " has joined the chat");

                String msgFromClient;
                while (socket.isConnected()) {
                    msgFromClient = bufferedReader.readLine();
                    if (msgFromClient.equalsIgnoreCase("bye")) {
                        break;
                    }

                    broadcastMessage(msgFromClient);
                }
            } else {
                bufferedWriter.write("Authentication failed. Disconnecting...");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean authenticate(String username , String password) {
        return userDatabase.containsKey(username) && userDatabase.get(username).equals(password);
    }

    public void broadcastMessage(String messageToSend) {
        //System.out.println(messageToSend);
        //System.out.println(clientHandlers.size());
        //System.out.println(connectedClients.get(0).clientUsername + " " + connectedClients.get(1).clientUsername);
        for (ClientHandler clientHandler : connectedClients) {
            System.out.println(clientHandler.clientUsername);
        }

        for(ClientHandler clientHandler : connectedClients) {
            try {
                if(!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
