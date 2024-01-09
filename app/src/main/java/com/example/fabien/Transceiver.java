package com.example.fabien;

public abstract class Transceiver {
    protected TransceiverListener mTransceiverListener;


    protected FrameProcessor mFrameProcessor;

    public int getState() {
        return state;
    }
    public void setState(int state) {
        this.state = state;
        if (mTransceiverListener!=null){
            mTransceiverListener.onTransceiverStateChanged(state);
        }
    }

    private int state;

    public abstract void send(byte[] data);


    public static final int STATE_NOT_CONNECTED=0;
    public static final int STATE_CONNECTING=1;
    public static final int STATE_CONNECTED=2;

    public void attachFrameProcessor(FrameProcessor fp){
        mFrameProcessor=fp;
//1
    }
    public void detachFrameProcessor(){
//2
    mFrameProcessor=null;
    }
    public void setTransceiverListener(TransceiverListener nTransceiverListener){
        mTransceiverListener=nTransceiverListener;

    }
    public void connectionLost(){
        if(mTransceiverListener!=null){
            mTransceiverListener.onTransceiverConnectionLost();
        }
    }
    public void connectionFailed(){
        if(mTransceiverListener!=null){
            mTransceiverListener.onTransceiverUnableToConnect();
        }
    }

    public abstract void connect(String id);

    public abstract void disconnect();
}
interface TransceiverListener {
    void onTranceiverDataReceived(String data);
    void onTransceiverConnectionLost();
    void onTransceiverUnableToConnect();

    void onTransceiverStateChanged(int state);
}