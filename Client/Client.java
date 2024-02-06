import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
       try {
        clientSocket = new Socket("127.0.0.1",5418);
        out = new PrintWriter(clientSocket.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        InputHandler inputHandler = new  InputHandler();
        Thread thread = new Thread(inputHandler);
        thread.start();

        String inMessage;
        while((inMessage = in.readLine()) != null) {
            System.out.println(inMessage);
        }
       } catch (IOException e) {
       
        shutdown();
       }
        
    }

    public void shutdown() {
        done = true;
        try {
            in.close();
            out.close();
            if(!clientSocket.isClosed()) {
                clientSocket.close();
            }
        }catch(IOException e) {
            //ignore
        }
    }

class InputHandler implements Runnable {

    @Override
    public void run() {
       
        try {
            BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
            while(!done) {
                String messageString = inReader.readLine();
                if(messageString.equals("/quit")) {
                    out.println(messageString);
                    inReader.close();
                    shutdown();
                }else {
                    out.println(messageString);
                }
            }
            
        } catch(IOException e) {
            
            shutdown();
        }
    
    
}
    
}
}
