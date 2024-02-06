import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    //runnable means that the other instances that are also runnable can run alongside with this class
    public static void main(String[] args) {
        Server server = new Server();
        server.run();

    }
    private ArrayList<ConnectionHandler> connections;
    private ServerSocket serverSocket;
    private boolean done;
    private ExecutorService poolExecutorService;

    public Server() {
        connections = new ArrayList<>();
        done = false;
    }
    @Override
    public void run() {
        //code that runs when the runnable class is executed.
        //Unlike python, we cannot pass functions in java,because they are first class entities.
        //In java, we pass the whole class and the class has to be runnable.
        try {
             serverSocket = new ServerSocket(5418);
             poolExecutorService = Executors.newCachedThreadPool();
             while(!done) {
                Socket clientSocket = serverSocket.accept();
                ConnectionHandler handler = new ConnectionHandler(clientSocket);
                connections.add(handler);
                poolExecutorService.execute(handler);
             }
            
        } catch (Exception e) {
            shutdown();
             
        }
        
    }

    public void broadcast(String messageString) {
        for(ConnectionHandler ch : connections) {
            if(ch != null) {
                ch.sendMessage(messageString);
            }
        }
    }

    public void shutdown() {
        try {
            done = true;
            poolExecutorService.shutdown();
            if(!serverSocket.isClosed()) {
            serverSocket.close();
            for(ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        }
        } catch (Exception e) {
            // ignore
        }
        
    }

    class ConnectionHandler implements Runnable {
        
        private Socket clientSocket;
        private BufferedReader in;
        private PrintWriter out;
        private String nicknameString;

        public ConnectionHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // out.println("Hello");
                // in.readLine();
                out.println("Please enter a nickname: ");
                nicknameString = in.readLine();
                System.out.println(nicknameString + " connected!");
                System.out.println(nicknameString + " joined the chat ^ ^");
                String messageString;
                while((messageString = in.readLine()) != null) {
                    if(messageString.startsWith("/nick")) {
                       
                        String[] messageSplit = messageString.split(" ",2);
                        if(messageSplit.length == 2) {
                            broadcast(nicknameString+ " renamed themselves to " + messageSplit[1]);
                            System.out.println(nicknameString+ " renamed themselves to " + messageSplit[1]);
                            nicknameString = messageSplit[1];
                            out.println("Successfully changed nickname to " + nicknameString);
                        }else {
                            out.println("No nickname provided :( ");
                        }

                    }else if(messageString.startsWith("/quit")) {
                        broadcast(nicknameString + " left the chat.");
                       shutdown();
                    }else {
                        broadcast(nicknameString + ": " + messageString);
                    }
                }
            } catch (IOException e) {
               shutdown();
            }
        }

        public void sendMessage(String messageString) {
            out.println(messageString);
        }

        public void shutdown() {
            try {
            in.close();
            out.close();
            if(!clientSocket.isClosed()) {
                clientSocket.close();
            }
        }catch(IOException e){
            //ignore
            }
        }
       
    }
}
