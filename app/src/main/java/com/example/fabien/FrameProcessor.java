package com.example.fabien;

import java.util.Arrays;

public class FrameProcessor {

    private int data;
    private byte FRAME_HEADER=0x05;
    private byte FRAME_LENGTH=0x02;
    private byte FRAME_PAYLOAD=0x06;
    private int FRAME_CTRL= 0xF1;
    private byte FRAME_TAIL=0x04;

    private boolean mRxHeaderCaptured;
    private int mRxCrC;
    private Object mDecoderState;
    private Object RX_LENGTH_H;
    private int mTxFrameLength;
    private byte[] mTxFrame;
/*
    public boolean fromFrame(){
        switch(data){
            case FRAME_HEADER:
                mRxHeaderCaptured=true;
                mRxCrC=0;
                mDecoderState = RX_LENGTH_H;
               // mRx

            case FRAME_LENGTH:
            case FRAME_PAYLOAD:
            case FRAME_CTRL:
            case FRAME_TAIL:

        }

    }


 */
    public byte[] toFrame(byte[] c){
        byte crcOut=0;
        byte lengthH,lengthL;
        mTxFrameLength=0;
        lengthH=(byte)(c.length/256);
        lengthL=(byte)(c.length%256);
        mTxFrame[mTxFrameLength++]=FRAME_HEADER ;
        escapeByte(lengthH);
        escapeByte(lengthL);
        crcOut = (byte) ((lengthL+lengthH)%256);

        for (byte data:c){
            crcOut=(byte)((data+crcOut)%256);
            escapeByte(data);
        }
        escapeByte((byte)(-crcOut));
        mTxFrame[mTxFrameLength++]=FRAME_TAIL;
        return Arrays.copyOf(mTxFrame,mTxFrameLength);
    }

    private void escapeByte(byte lengthH) {

    }


}
