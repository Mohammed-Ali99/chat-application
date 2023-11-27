import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
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
            closeEverything(socket , bufferedReader , bufferedWriter);
        }
    }


    public void sendMessage() {

        try {
            Scanner scanner = new Scanner(System.in);

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            bufferedWriter.write(password);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            String response = bufferedReader.readLine();
            if(response.equals("Authentication successful.")) {
                System.out.println("Start chatting (type 'bye' to quit)");

                File file = new File(username + ".txt");
                if(file.exists()) {
                    file.delete();
                }

                // Read the input from client and send it the server
                listenToMessage();
                while (socket.isConnected()) {
                    String msgToSend = scanner.nextLine();

                    listenToMessage();
                    bufferedWriter.write(username + ": " + msgToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    if(msgToSend.equalsIgnoreCase("bye")) {
                        beforeExit();
                        System.exit(0);
                    } else {
                        saveMessage(msgToSend , username);
                    }
                }

            }
        } catch (IOException e) {
            closeEverything(socket , bufferedReader , bufferedWriter);
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
                        closeEverything(socket , bufferedReader , bufferedWriter);
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

    public void beforeExit() {
        try (Scanner scanner = new Scanner(new File(username + ".txt"))){

            Map<String , Integer> countWords = new HashMap<>();

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split(" ");
                for (String word : words) {
                    if(countWords.containsKey(word)) {
                        int temp = countWords.get(word);
                        temp++;
                        countWords.put(word , temp);
                    } else {
                        countWords.put(word , 1);
                    }
                }
            }
            saveStatistics(username , countWords);

        } catch (IOException es) {
            es.getMessage();
        }
    }

    public void saveStatistics(String username , Map<String , Integer> countWords) {
        Map<String , Integer> oldStatistics = getOldStatistics(username);

        try (PrintWriter writer = new PrintWriter(new FileWriter(username + "_statistics.txt" , false))){
            if (!oldStatistics.isEmpty()) {
                for (Map.Entry<String , Integer> map : countWords.entrySet()) {
                    if(oldStatistics.containsKey(map.getKey())) {
                        int temp = map.getValue() + oldStatistics.get(map.getKey());
                        oldStatistics.put(map.getKey() , temp);
                    } else {
                        oldStatistics.put(map.getKey() , 1);
                    }

                }
                for (Map.Entry<String , Integer> data : oldStatistics.entrySet()) {
                    System.out.println(data.getKey() + "====>" + data.getValue());
                    writer.println(data.getKey() + ":" + data.getValue().toString());
                }
            } else {
                for (Map.Entry<String , Integer> map : countWords.entrySet()) {
                    writer.println(map.getKey() + ":" + map.getValue().toString());
                }
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }

    public Map<String, Integer> getOldStatistics(String username) {
        Map<String , Integer> oldStatistics = new HashMap<>();
        File file = new File(username + "_statistics.txt");
        try {
            if(file.exists()) {
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] parts = line.split(":");
                    if(parts.length == 2) {
                        oldStatistics.put(parts[0] , Integer.valueOf(parts[1]));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.getMessage();
        }
        return oldStatistics;
    }

    public void closeEverything(Socket socket , BufferedReader reader , BufferedWriter writer) {
        try {
            if(socket != null) socket.close();
            if(writer != null) writer.close();
            if(reader != null) reader.close();
        } catch (IOException e) {
            e.printStackTrace();
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
    }
}
