package com.lepg.keys.client;

import com.lepg.keys.KeyManager;

import java.net.Socket;
import java.io.*;
import java.security.*;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    private static Socket socket = null;                                            //Socket to handle data transfer
    private static KeyManager keyManager = new KeyManager("RSA", "SUN");

    private static Scanner s = new Scanner(System.in);                              //Scanner to privitive values
    private static Scanner s2 = new Scanner(System.in);
    private static ObjectInputStream objIn = null;//Scanner to user inputs
    private static InputStream in = null;
    private static ObjectOutputStream objOut = null;
    private static FileOutputStream fout = null;

    private static String userInput = null;
    private static File file = null;                                                //Java Object to reference the selected file

    private static final String finalPath = "C:\\\\Users\\Ptthappy\\Downloads\\";   //Path for incoming files
    private static final String path = "C:\\\\Users\\Ptthappy\\";                   //Home path
    private static byte[] buffer = new byte[32];                                    //Buffer for bytes arrays

    private static PrivateKey privateKey = null;                                    //Private key
    private static PublicKey publicKey = null;                                      //Public key

    private static HashMap<String, PublicKey> keys = new HashMap<>();               //HashMap to store public keys from other sockets

    public static void main(String[] args) {
        System.out.println("1. Connect to Server\n0. Exit");

        while(true) {
            try {
                switch(s.nextLine()) {
                    case "1":
                        KeyPair pair = keyManager.genKeyPair();
                        privateKey = pair.getPrivate();
                         publicKey = pair.getPublic();
                        System.out.println("Keys Generated");

                        connectToServer();
                        //Wait until public key is received
                        receiveKey();
                        //Send public key
                        sendKey();
                        //Loop
                        loop();

                        terminate();
                        System.exit(0);
                        break;

                    case "0":
                        System.exit(0);

                    default:
                        throw new InputMismatchException();
                }
            } catch (InputMismatchException e) {
                System.err.println("Invalid Input");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void loop() throws Exception {
        while(true) {
            System.out.println("Enter path from a file to send. Enter \"exit\" to exit program");
            userInput = s2.nextLine();
            if(userInput.equals("exit"))
                break;
            else {
                speakToServer();
            }
        }
    }

    private static void speakToServer() throws Exception {
        objOut.writeObject(userInput);
        System.out.println("Request sent. Waiting for response");
        listenToServer();
        System.out.println("Transmission complete");
    }

    private static void listenToServer() throws Exception {
        String to;
        if (userInput.lastIndexOf('/') != -1)
            to = userInput.substring(userInput.lastIndexOf('/'));
        else if (userInput.lastIndexOf('\\') != -1)
            to = userInput.substring(userInput.lastIndexOf('\\'));
        else
            to = userInput;
        String in = "";
        if(!(in = ((String)objIn.readObject())).equals("")) {
            file = new File(finalPath, to);
            file.createNewFile();
            fout = new FileOutputStream(file);

            String[] values = in.split(";");
            Integer byteCount = Integer.parseInt(values[0]);
            Integer lastLen = Integer.parseInt(values[1]);

            int x, y = 0;
            byte[][] encryptedData = new byte[byteCount][117];
            byte[][] decryptedData = new byte[byteCount][117];
            byte[] data = new byte[byteCount * 117];

            //TODO:
            //Recibir la data encriptada que viene del server
            //Almacenarlo en encryotedData

            for (int i = 0; i < byteCount; i++) {
                decryptedData[i] = keyManager.decryptData(encryptedData[i], privateKey);
            }

            int index = 0;
            for (int i = 0; i < byteCount; i++) {
                for (int j = 0; j < 117; j++) {
                    data[index] = decryptedData[i][j];
                }
            }

            //TODO:
            //Escribir data en el archivo de salida

        } else {
            //Mensaje de error como que ei mardito esa dirección no eksiste
        }

    }

    private static boolean checkBuffers(int next) {
        for (int i = 31; i > 0; i--) {
            buffer[i] = buffer[i - 1];
        }
        buffer[0] = (byte)next;

        for (int i = 0; i < 32; i++) {
            if(buffer[i] != 85)
                return false;
        }
        return true;
    }

    private static void connectToServer() throws Exception {
        socket = new Socket("localhost", 2000);
        objOut = new ObjectOutputStream(socket.getOutputStream());
        objIn = new ObjectInputStream(socket.getInputStream());
        in = socket.getInputStream();
        System.out.println("Socket connected");
    }

    @SuppressWarnings("Duplicates")
    private static void receiveKey() throws Exception {
        keys.put("server", (PublicKey) objIn.readObject());
        System.out.println("Server public key received");
    }

    @SuppressWarnings("Duplicates")
    private static void sendKey() throws Exception {
        objOut.writeObject(publicKey);
        System.out.println("Public key sent to server");
    }

    @SuppressWarnings("Duplicates")
    private static void terminate() throws Exception {
        objOut.writeObject("exit");
        objOut.close();
        objIn.close();
        in.close();
        if(fout != null) {
            fout.close();
        }
        socket.close();
        s.close();
        s2.close();
        System.out.println("Streams and objects closed");
    }
}
