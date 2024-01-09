package com.example.fabien;

import androidx.annotation.NonNull;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;

public class ByteRingBuffer {
    private byte [] buff;
    private int wPos;
    private int rPos;
    private boolean full = false;

    /**
     *   Constructeur
     *   @param size : taille du buffer circulaire en octet
     */
    public ByteRingBuffer(int size) {
        buff = new byte[size];
        wPos = 0;
        rPos = 0;
    }
    /**
     *   Ecriture des octets d'un tableau dans le buffer
     *   @param bArray : tableau d'octets à écrire
     */
    public synchronized void put(byte[] bArray) throws BufferOverflowException {
        for (byte b : bArray) {
            put(b);
        }
    }

    /**
     *   Nombre d'octets disponibles dans le buffer
     *   @return nombre d'octets présents dans le buffer
     */
    public synchronized int bytesToRead(){
        if (full) {
            return buff.length;
        } else {
            return (wPos - rPos + buff.length) % buff.length;
        }
    }

    /**
     *   Ajout d'un octet dans le buffer
     *   @param b : octet à ajouter
     */
    public synchronized void put(byte b) throws BufferOverflowException {
        if (full) {
            throw new BufferOverflowException();
        }
        buff[wPos] = b;
        wPos = (wPos + 1) % buff.length;
        if (wPos == rPos) {
            full = true;
        }
    }


    /**
     *   Lecture de tous les octets présents dans le buffer
     *   @return tableau d'octets lu
     */
    public synchronized byte[] getAll(){
        byte[] result = new byte[bytesToRead()];
        for (int i = 0; i < result.length; i++) {
            result[i] = get();
        }
        return result;
    }

    /**
     *   Lecture d'un octet du buffer
     *   @return octet lu
     */
    public synchronized byte get() throws BufferUnderflowException {
        if (!full && wPos == rPos) {
            throw new BufferUnderflowException();
        }
        byte result = buff[rPos];
        rPos = (rPos + 1) % buff.length;
        full = false;
        return result;
    }

    /**
     *   Indication d'information sur le buffer (utilisé principalement pour le débuggage)
     *   @return Chaine contenant des informations d'état du buffer (taille, nombre d'éléments présents, position des pointeurs ...)
     */
    @NonNull
    @Override
    public String toString() {
        return "ByteRingBuffer{" +
                "buff=" + Arrays.toString(buff) +
                ", wPos=" + wPos +
                ", rPos=" + rPos +
                ", full=" + full +
                '}';

    }



}
