package com.lepg.keys.server;

import com.lepg.keys.KeyManager;

import java.io.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    private static Socket socket = null;                                               //Socket to handle data transfer
    private static KeyManager keyManager = new KeyManager("RSA", "SUN");
    private static ServerSocket ss = null;                                             //SS to accept connection

    private static ObjectInputStream objIn = null;
    private static FileInputStream fin = null;
    private static ObjectOutputStream objOut;
    private static OutputStream out = null;

    private static File file = null;                                                   //For referencing a file

    private static final String path = "C:\\\\Users\\Ptthappy\\";                      //Home path

    private static PrivateKey privateKey = null;                                       //Private key
    private static PublicKey publicKey = null;                                         //Public key

    private static HashMap<String, PublicKey> keys = new HashMap<>();                  //HashMap to store public keys from other sockets

    public static void main(String[] args) {
        try {
            KeyPair pair = keyManager.genKeyPair();
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
            System.out.println("Keys Generated");

            initializeConnection();

            sendKey();
            receiveKey();

            loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loop() throws Exception {
        while(true) {
            System.out.println("Waiting for requests");
            String userIn = (String) objIn.readObject();
            if(userIn.equals("exit")) {
                System.out.println("Client disconected");
                break;
            } else {
                respondClient(userIn);
            }
        }
    }

    private static void respondClient(String in) throws Exception {
        file = new File(path + in);
        System.out.println(path + in);
        if(file.exists()) {
            int x;
            int lastLen = 0;
            byte[][] datablocks = new byte[8962][117]; //Max Size 1Mb
            fin = new FileInputStream(file);

            int byteCount = 0;
            for (int i = 0; (x = fin.read()) != -1; i++) {
                datablocks[byteCount][i] = (byte) x;
                if(i == 116) {
                    i = -1;
                    byteCount++;
                }
                lastLen = i;
            }

            System.out.println(lastLen);
            System.out.println(byteCount);

            //Enviar un objeto que tenga dos números: la cantidad de bloques y la longitud del último
            objOut.writeObject(byteCount + ";" + lastLen);

            byte[][] encryptedData = new byte[byteCount + 1][128];
            for (int i = 0; i <= byteCount; i++) {
                encryptedData[i] = keyManager.encryptData(datablocks[i], keys.get("client"));
            }

            sendData(encryptedData);

        } else {
            objOut.writeObject("");
        }
    }

    private static void sendData(byte[][] data) throws Exception {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < 128; j++) {
                out.write(data[i][j]);
            }
        }

        for (int i = 0; i < 32; i++) {
            out.write(85);
        }

        System.out.println();
    }

    private static void initializeConnection() throws Exception {
        ss = new ServerSocket(2000);
        System.out.println("Waiting for client");
        socket = ss.accept();
        System.out.println("Client received");

        out = socket.getOutputStream();
        objIn = new ObjectInputStream(socket.getInputStream());
        objOut = new ObjectOutputStream(socket.getOutputStream());
    }

    @SuppressWarnings("Duplicates")
    private static void receiveKey() throws Exception {
        keys.put("client", (PublicKey) objIn.readObject());
        System.out.println("Client public key received");
    }

    @SuppressWarnings("Duplicates")
    private static void sendKey() throws Exception {
        objOut.writeObject(publicKey);
        System.out.println("Public key sent to server");
    }

    @SuppressWarnings("Duplicates")
    private static void terminate() throws Exception {
        objOut.close();
        objIn.close();
        fin.close();
        out.close();
        socket.close();
        System.out.println("Streams and objects closed");
    }
}
