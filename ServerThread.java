package socket.fx;
import java.io.*;
import java.net.Socket;
public class ServerThread implements Runnable
{
    private final Server server;
    private final Socket socket;
    
    public ServerThread(Server server, Socket socket) 
    {
        this.server = server;
        this.socket = socket;
    }   
    @Override
    public synchronized void run() 
    {
        try 
        {
            boolean checkUsr=true;
            while(true) 
            {
                //Leggo le stringhe in arrivo dai client
                DataInputStream din = new DataInputStream(socket.getInputStream());
                DataOutputStream dout = new DataOutputStream(socket.getOutputStream());
                String msg = din.readUTF();
                System.out.println(msg);
                if(checkUsr)
                        for(String s: Server.usernames.values())    //Controllo l'HashMap in ricerca di username gia usati
                        {
                            if(s.equals(msg))
                                dout.writeUTF("--ERROREUSER--");
                            else
                            {
                                Server.usernames.replace(socket, "default", msg);
                                checkUsr = false;
                                server.updateUsr();
                                break;
                            }
                        }     
                else
                {
                    System.out.println(msg);
                    //Invio le stringhe a tutti i client connessi
                    server.sendToAll(msg);
                    
                }
            }
        }
        catch (IOException ioe) {
            System.err.println(ioe);
        }
        finally 
        {
            //Elimino dall'hashmap questo client
            server.removeConnection(socket);
        }
    }
}

