package com.example.fabien;

public class OscilloManager implements TransceiverListener{
    private static OscilloManager instance;

    private String currentAddress;
    private Transceiver mTransceiver;

    public void attachTransceiver (Transceiver transceiver){
        mTransceiver=transceiver;
        mTransceiver.setTransceiverListener(this);
    }
    public OscilloEventsListener getmOscilloEventsListener() {
        return mOscilloEventsListener;
    }

    public void setmOscilloEventsListener(OscilloEventsListener mOscilloEventsListener) {
        this.mOscilloEventsListener = mOscilloEventsListener;
    }

    private OscilloEventsListener mOscilloEventsListener;

    public OscilloManager() {
    }

    public static OscilloManager getInstance() {
        if (instance == null) {
            instance = new OscilloManager();
        }
        return instance;
    }

    public void connect(String address) {
        mTransceiver.connect(address);
    }

    @Override
    public void onTranceiverDataReceived(String data) {

    }

    @Override
    public void onTransceiverConnectionLost() {

    }

    @Override
    public void onTransceiverUnableToConnect() {

    }

    @Override
    public void onTransceiverStateChanged(int state) {
        mOscilloEventsListener.onStateChanged(state);
    }
}


interface OscilloEventsListener{
    void onConnectionEstablished(String address);
    void onConnectionLost();
    void onConnectionError(String errorMessage);
    void onStateChanged(int state);

}