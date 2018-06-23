package socket.fx;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.*;
import java.util.Optional;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCode;
import static socket.fx.Client.tableUsers;

public class Client extends Application 
{
    static DataInputStream in  = null;
    static DataOutputStream    out = null;
    static TextArea tArea=new TextArea(); 
    static String username;
    static String[] parts ;
    static TableView <String> tableUsers = new TableView<>();
    static final int port=8189;
    static Socket sock = null;
    static boolean ctrl = false;
    static Label lbl = new Label("Benvenuto, connettiti al server"); 
    static Button conn=new Button("Connetti"), disc=new Button("Disconnetti"), inv=new Button("Invia");; 
    static String host="127.0.0.1", roundWhiteBut="-fx-background-color: linear-gradient(#00FFFF, #4682B4); -fx-background-radius: 30; -fx-background-insets: 0; -fx-text-fill: white;", roundRedBut="-fx-background-color: linear-gradient(#ff5400, #be1d00); -fx-background-radius: 30; -fx-background-insets: 0; -fx-text-fill: white;",roundGreenBut="-fx-background-color: linear-gradient(#32CD32, #228B22); -fx-background-radius: 30; -fx-background-insets: 0; -fx-text-fill: white;",roundTextFieldRed="-fx-focus-color: red; -fx-background-radius: 10 10 10 10;", roundTextFieldGreen="-fx-focus-color: green ; -fx-background-radius: 10 10 10 10;";
    @Override
    public void start(Stage primaryStage) 
    {     
        
        primaryStage.setResizable(false);
        primaryStage.setTitle("Chat");
        primaryStage.setMaxHeight(720);
        primaryStage.setMaxWidth(980);
        primaryStage.setResizable(false);
        
        
        AnchorPane root=new AnchorPane();
        
        lbl.setLayoutX(40); lbl.setLayoutY(15);     
        lbl.setStyle("-fx-font: 30 arial; -fx-text-fill: BLACK;");
        //Pulsante connetti 
        conn.setLayoutX(lbl.getLayoutX()+620);  conn.setLayoutY(10);                                       
        conn.setPrefSize(80,40);                                                           
        conn.setStyle(roundGreenBut); 
        //Pulsante disconnetti non visibile
        
        
        //Text Area dei messaggi
        tArea.setLayoutX(40); tArea.setLayoutY(60);
        tArea.setPrefSize(700,500);
        tArea.setEditable(false);
        tableUsers.setLayoutX(tArea.getLayoutX()+720); tableUsers.setLayoutY(60);
        tableUsers.setPrefSize(200,500);
        TableColumn <String,String> col = new TableColumn<>("Utenti connessi");
        col.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));
        tableUsers.getColumns().add(col);
        tableUsers.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TextField tField = new TextField();             //Text Field per scrittura
        tField.setLayoutX(40); tField.setLayoutY(600);
        tField.setPrefSize(600,20);
        disc.setLayoutX(tField.getLayoutX()+620);  disc.setLayoutY(10);                                       
        disc.setPrefSize(80,40);                                                           
        disc.setStyle(roundRedBut);
        disc.setVisible(false);
        
        //Pulsante connetti 
        inv.setLayoutX(tField.getLayoutX()+620);  inv.setLayoutY(600);                                       
        inv.setPrefSize(80,20);                                                           
        inv.setStyle(roundWhiteBut);
        
        root.getChildren().addAll(conn,disc,tArea,tField,inv,lbl,tableUsers);
        Scene scene=new Scene(root,980,720);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e ->{ if(!conn.isVisible())disconnessione();});
       
        conn.setOnAction(e ->
        {
            //Richiamo finestra inserimento username
            window();
        });
        disc.setOnAction(e ->
        {
            //Eseguo disconnessione
            disconnessione();
        }); 
        inv.setOnAction(e ->
        {
            //Invio messaggio dopo pressione del button
            if(!conn.isVisible() && !tField.getText().equals(""))
            {
                write(tField.getText());
                tField.clear();
            } 
        });
        tField.setOnKeyPressed(e ->
        {
            //Invio messaggio dopo pressione invio
            if(!conn.isVisible() && !tField.getText().equals(""))
                if(e.getCode()==KeyCode.ENTER)
                {
                    write(tField.getText());
                    tField.clear();
                }
        });
    }
    public static void disconnessione()
    {
        try
        {
            tableUsers.getItems().clear();
            in.close();
            out.close();
            sock.close();
            disc.setVisible(false);
            conn.setVisible(true);
            System.out.println("\n\tTerminata la connessione!\n");
            lbl.setText("Benvenuto, connettiti al server");
        } 
        catch (IOException ex) 
        {
            System.out.println(ex);
        }
    }
    public static void window()
    {
        //Finestra inserimento Username
        TextInputDialog dialog = new TextInputDialog();
        dialog.getEditor().setPromptText("Username");
        dialog.setTitle("Inserisci un Username");
        dialog.setHeaderText("Che username vorresti usare?");
        dialog.setContentText("Perfavore inseriscilo: (Min 3 caratteri)");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) 
        {
            if (!dialog.getResult().equals(null)&&dialog.getResult().length()>=3)
            {
                username=dialog.getResult().trim();
                connessione();
            }
            else
                alertWindow("Username non valido","Username Error","Error");
        }
    }
    public static void alertWindow(String textContent,String textHeader,String textTitle)
    {
        //Finestra di errore
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(textTitle);
        alert.setHeaderText(textHeader);
        alert.setContentText(textContent);

        alert.showAndWait();
    }
    public static void connessione()
    {
        try
        {
            sock = new Socket(host,port);
            conn.setVisible(false);
            disc.setVisible(true);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            
            try{out.writeUTF(username);} catch (Exception ex) {System.out.print(ex);} //Scrivo al server il mio username
            lbl.setText("Benvenuto, "+username);
            System.out.println(username.toUpperCase()+" CONNESSO!");
            
            new Thread(new readThread()).start(); //Avvio thread lettura
        }
        catch(ConnectException ec)
        {
            System.out.println("Host remoto "+host+" NON avviabile !");
            alertWindow("Host remoto "+host+" NON avviabile !","Error in socket Connection","Socket Error");
            return;
        }
        catch(IOException exIO)
        {
            System.out.println("Provo a connettermi con time-out di 1 sec. a " + host);
            alertWindow("Provo a connettermi con time-out di 1 sec. a " + host,"Error in socket Connection","Socket Error");
        }
    }
    public static void write(String text)
    {
        try 
        {
            out.writeUTF(username+": "+text);
        } 
        catch (Exception ex) {System.out.print(ex);}
    }
    public static void main(String[] args) {launch(args);}
}
class readThread extends Thread 
{
    //Thread di lettura
    @Override
    public synchronized void run()
    {
        String received;
        while(!Client.conn.isVisible())
        {
            try 
            {
                received = Client.in.readUTF();
                if(received.equals("--ERROREUSER--"))   //Controllo se l'username é gia stato usato
                {
                    Client.disconnessione();
                    System.out.println("Username not valid");
                }
                else
                    if(received.equals("--UPDATEUSR--"))    //Controllo la possibile connessione di nuovi client e aggiorno la tableView a destra
                    {
                        Client.tableUsers.getItems().clear();
                        String s = Client.in.readUTF();
                        Client.parts = s.split("-");
                        for(int i=0; i<Client.parts.length; i++)
                            Client.tableUsers.getItems().add(Client.parts[i]);
                    }        
                    else
                        Client.tArea.appendText(received+"\n");       
            }catch (IOException ex) {System.out.println(ex);}
        }
    }
}

