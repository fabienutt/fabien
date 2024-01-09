package com.example.fabien;



import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BTManager extends Transceiver {
    // Unique UUID for this application (set the SPP UUID because expected incomming connection are of this type)
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    // reference vers l'adaptateur
    private final BluetoothAdapter mAdapter;
    private ConnectThread ct;
    private TextView labelEtat;
    // réferences vers les Threads
    private BluetoothSocket mSocket = null;
    private ConnectThread mConnectThread = null;
    //private ReadingThread mReadingThread = null;
    private WritingThread mWritingThread = null;

    // Constructeur par défaut
    public BTManager() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        setState(STATE_NOT_CONNECTED);
    }



 /* TODO 2 : (Q3.7-4) Réimplementation de connect (action devant s'exécuter en tâche de fond)
          1 - Annuler les éventuelles demandes de connexion en cours
          2 - Créer un Thread de connexion
          3 - Placer le label d'état en "Connexion en cours"
          4 - Lancer le thread

*/

    @Override
    public void connect(String id) {
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(id);
        disconnect();
        mConnectThread = new ConnectThread(device);
        setState(STATE_CONNECTING);
        mConnectThread.start();


    }


    /* TODO 3 : (Q3.7-4) Réimplementation de disconnect
          1 - Fermer le socket (Attention : génère une exception checkée)
          2 - Libérer les références de thread (la boucle des threads devra checker ces refs
          comme condition d'arrêt du thread).
*/
    public void cancel() {
        try {
            // Fermer le socket
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e("ConnectThread", "Error during socket close", e);
        }
    }

    @Override
    public void disconnect() {
        // Fermer le socket
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mSocket = null;
        }

        // Libérer les références de thread
        mConnectThread = null;
       // mReadingThread = null;
        mWritingThread = null;
        setState(STATE_NOT_CONNECTED);
    }


    /**
     * TODO 7 : Conversion des commandes en trames puis transmission par buffer circulaire
     * 		Réimplémenter la méthode send pour qu'elle réalise la conversion des commandes en trames puis qu'elle
     * les insère dans le buffer circulaire à l'aide de la méthode write du thread d'écriture.
     */
     @Override
     public void send(byte[] data) {
         byte[] frame = convertToFrame(data);
        // mWritingThread.write(frame);
     }



    private byte[] convertToFrame(byte[] command) {
        byte[] frame = new byte[command.length + 2];

        //revoir

        System.arraycopy(command, 0, frame, 0, command.length);
        frame[command.length] = '\r';
        frame[command.length + 1] = '\n';
        return frame;
    }

    /*TODO 1 : (Q3.7-3) Création d'un thread de connexion
            1 - Récupération d'un socket Bluetooth pour le périphérique visé.
            Attention :  la méthode createRfcommSocketToServiceRecord(UUID) lance un
            exeption IOException checkée : il est donc nécessaire de l'intercepter.
            2 - Implémentation de la méthode Run qui doit :
                a - Eventuellement stopper une découverte déjà lancée
                b - se connecter au socket (idem concernant les exceptions)
                c - instancier et lancer les threads de communication WritingThread et ReadingThread
     */


    public class ConnectThread extends Thread {
        private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
        private BluetoothSocket mSocket;
        private BluetoothAdapter mAdapter;

        public BluetoothSocket getmSocket() {
            return mSocket;
        }

        public void setmSocket(BluetoothSocket mSocket) {
            this.mSocket = mSocket;
        }

        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
            mSocket = tmp;
        }

        @SuppressLint("MissingPermission")
        public void run() {
            if (mAdapter != null) {
                mAdapter.cancelDiscovery();
            }
            mWritingThread = new WritingThread(mSocket);
            //mReadingThread = new ReadingThread();

            try {
                mSocket.connect();
                //mWritingThread.start();
                //mReadingThread.start();
                if(mSocket!=null){
                    setState(STATE_CONNECTED);
                }
            } catch (IOException e) {
                disconnect();
                connectionFailed();
            }

        }

    }


    /************************************************************************************
     /
     /                          THREADS de COMMUNICATION
     /
     /***********************************************************************************/


 /* TODO 5 : Créez ici une classe héritée de Thread permettant de gérer de manière non
              bloquante l'envoi des commande vers l'oscilloscope.
               1 - Dans le constructeur, récupérer la référence sur le flux sortant du socket via
                getOutputStream (Attention : getInputStream lance une exeption Checkée) et initialiser
                un buffer circulaire (de 1024 octets par exemple).
               2 - écrire la méthode run qui prend les octets disponibles dans le buffer circulaire
                   et les transmet via la méthode write d'OutputStream (peu importe si c'est bloquant),
               3 - définir une méthode write qui écrit un tableau d'octets dans le buffer circulaire

*/

    public class WritingThread extends Thread {

        public OutputStream getOutputStream() {
            return outputStream;
        }

        private OutputStream outputStream;
        private ByteRingBuffer circularBuffer;
        private BluetoothSocket mSocket;
        public WritingThread(BluetoothSocket mSocket) {
            this.mSocket = mSocket;
            this.circularBuffer = new ByteRingBuffer(1024);

             try {
                 outputStream = mSocket.getOutputStream();
             } catch (IOException e) {
                 e.printStackTrace();
             }
        }

        public void run() {
            while (mSocket != null) {
                if (circularBuffer.bytesToRead() != 0) {
                    try {
                        outputStream.write(circularBuffer.getAll());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }


        public void write( byte[] data) {
            circularBuffer.put(data);
        }

        public void stopThread() {
            interrupt();
        }
  }





 /* TODO 6 : Créez ici une classe héritée de Thread permettant de gérer de manière non
              bloquante la lecture des datas de l'oscilloscope.
               1 - Dans le constructeur récupérer la référence sur le flux entrant du socket via
               getInputStream (Attention : getInputStream lance une exeption Checkée)
               2 - écrire la méthode run qui lit un octet du flux (peu importe si c'est bloquant),
                   et l'envoi dans l'interpreteur de trame jusqu'à ce qu'une trame soit constituée et
                   décodée.
               3 - lorsque c'est le cas, le listener du transceiver doit traiter la trame.


    public class ReadingThread extends Thread {



        private final InputStream mInStream;


        public ReadingThread(BluetoothSocket Bsocket) {
            InputStream tmpIn = null;
            try{
                tmpIn= mSocket.getInputStream();
            }
            catch (IOException e) {
            }
            mInStream=tmpIn;
        }

        @Override
        public void run() {
            while (mSocket!=null){
                try{
                    if (FrameProcessor.fromFrame((byte) mInStream.read())
                }
            }
        }

        private boolean isFrameComplete(byte[] frameBuffer, int frameIndex) {
            return frameIndex >= 2 && frameBuffer[frameIndex - 2] == '\r' && frameBuffer[frameIndex - 1] == '\n';
        }

        private String decodeFrame(byte[] frameBuffer, int frameIndex) {
            return new String(frameBuffer, 0, frameIndex - 2);
        }
    }


}*/
        /*
 public class ReadingThread extends Thread {}
*/
}