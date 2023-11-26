import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    private String password;


    public Client(Socket socket , String username , String password) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.username = username;
            this.password = password;
        } catch (IOException e) {
            e.getMessage();
        }
    }


    public void sendMessage() {

        try {
            Scanner scanner = new Scanner(System.in);
            //System.out.println(bufferedReader.readLine()); // Enter your username
            //System.out.println("enter your username : ");
            //String username = scanner.nextLine();


            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            //System.out.println(bufferedReader.readLine()); // Enter your password
           // System.out.println("enter your password : ");
            //String password = scanner.nextLine();

            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String response = bufferedReader.readLine();
            System.out.println(response);
           // Scanner scanner = new Scanner(System.in);
            if(response.equals("Authentication successful. You can chat now.")) {
                System.out.println("Start chatting (type 'bye' to quit)");

                // Read the input from client and send it the server
                listenToMessage();
                while (socket.isConnected()) {
                    String msgToSend = scanner.nextLine();
                    listenToMessage();
                    bufferedWriter.write(username + ": " + msgToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();



                    if(msgToSend.equalsIgnoreCase("bye")) {
                        System.exit(0);
                    } else {
                        saveMessage(msgToSend , username);
                    }
                }
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public void listenToMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (true) {
                    try {
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    } catch (IOException e) {
                        e.getMessage();
                    }
                }
            }
        }).start();
    }

    public void saveMessage(String message , String username) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(username + ".txt" , true))){
            writer.println(message);
        } catch (IOException e) {
            e.getMessage();
        }
    }


    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your username for the group chat : ");
        String username = scanner.nextLine();

        System.out.println("Enter your password for the group chat : ");
        String password = scanner.nextLine();

        Socket socket = new Socket("localhost" , 9000);
        Client client = new Client(socket , username , password);

        client.sendMessage();
        //client.listenToMessage();




    }
}
