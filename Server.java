package socket.fx;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server 
{
    //Salvo in una hashmap i client connessi
    private static final HashMap<Socket, DataOutputStream> outputStreams = new HashMap<>();
    public static HashMap<Socket,String> usernames = new HashMap<>();

    public void listen()
    {
        System.out.println("\n\tSERVER\n\n");

        try
        {
            //Creo una serversocket sulla porta 8189
            ServerSocket serverSocket = new ServerSocket(8189);
            while(true)
            {
                Socket socket = serverSocket.accept();
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());                  
                outputStreams.put(socket, dout);
                usernames.put(socket, "default");       //HashMap per il controllo di username uguali
                new Thread(new ServerThread(this, socket)).start();
                System.out.println("Client connesso");
                
            }    
        }
        catch(Exception e)
        {
            System.out.println(e+" err2");
        }
    }
    void sendToAll(String msg) {
        //Utilizzo synchronized per evitare l'accesso all'hashmap contemporaneo all'eliminazione
        synchronized(outputStreams)
        {
            //Scorro l'hashmap per inviare a tutti i client il messaggio
            for (Map.Entry<Socket, DataOutputStream> e :outputStreams.entrySet())
            {
                DataOutputStream dout = e.getValue();
                try 
                {
                    dout.writeUTF(msg); 
                }
                catch(IOException ioe) {System.err.println(ioe);}
            }
        }
    }
    void updateUsr()
    {
        synchronized(outputStreams)
        {
            //Scorro l'hashmap per inviare a tutti i client il messaggio
            for (Map.Entry<Socket, DataOutputStream> e :outputStreams.entrySet())
            {
                DataOutputStream dout = e.getValue();
                try 
                {
                    dout.writeUTF("--UPDATEUSR--");
                    String s = "";
                    for(Map.Entry<Socket, String> str: usernames.entrySet())
                    {
                        s += str.getValue()+"-";
                    }
                    System.out.println(s);
                    dout.writeUTF(s);
                }
                catch(IOException ioe) {System.err.println(ioe);}
            }
        }
    }
    void removeConnection(Socket sock) {
        //Synchronized sempre per evitare accesso contemporaneo all'invio
        synchronized(outputStreams) 
        {
            outputStreams.remove(sock); //Rimuovo dall'hashmap il socket che si é disconnesso
            usernames.remove(sock);     //Rimuovo username
            try 
            {
                System.out.println("Connection removed\n"+sock);
                sock.close();
                updateUsr();
            }
            catch (IOException ioe) {
                System.err.println(ioe);
            }
        }
    }
    public Server()
    {
        listen();
    }
    public static void main(String[] args)
    {  
        new Server();
    }
} 
