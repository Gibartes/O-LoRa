/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.team_olora.olora_beta;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class Service_BluetoothChatService implements Serializable {
    // Debugging
    private static final String TAG = "Service_BluetoothChatService";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothChatSecure";
    private static final String NAME_INSECURE = "BluetoothChatInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    public static int mState;
    private int mNewState;

    // Service_Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    Service_packet packet = null;


    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public Service_BluetoothChatService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        mState = getState();
        mNewState = mState;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Service_Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {

        /*
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        */

        // Start the thread to connect with the given device
        if (mConnectThread == null) {
            mConnectThread = new ConnectThread(device, secure);
            mConnectThread.start();
        }

    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {

        /*
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        */

        // Start the thread to manage the connection and perform transmissions
        if (mConnectedThread == null) {
            mConnectedThread = new ConnectedThread(socket, socketType);
            mConnectedThread.start();
        }

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(Service_Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Service_Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        mState = STATE_NONE;
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and noify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity

        mState = STATE_NONE;

        // Start the service over to restart listening mode
        Service_BluetoothChatService.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        mState = STATE_NONE;

        // Start the service over to restart listening mode
        Service_BluetoothChatService.this.start();
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread implements Serializable {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice

            // 안드로이드 롤리팝 이후로 프로토콜 스택이 바뀌었다고 함.
            // 리플렉션으로 메서드를 다시 불러와서 인자값 추가함으로써 해결.
            try {
                if (secure) {
                    Method method;

                    method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    tmp = (BluetoothSocket) method.invoke(device, 1);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (Exception e) {
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception

                // 연결 실패와 연결 끊김 부분이 문제임.
                // 경우를 나눠 처리해줘보자
                if (mState != STATE_CONNECTED)
                    mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Service_BluetoothChatService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    protected class ConnectedThread extends Thread implements Serializable {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        public ConnectedThread(BluetoothSocket socket, String socketType) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // 패킷 핸들 객체
            packet = new Service_packet();
            //

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run() {
            byte[] buffer = new byte[1008];
            int bytes;

            // Keep listening to the InputStream while connected
            while (mState == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // 패킷변환해서 바로보냄
                    // send result 는 걸러내기

                    byte[] param = new byte[1];
                    param[0] = buffer[39];

                    long dest = 0;
                    for (int i = 0; i < 8; i++) {
                        dest = buffer[i + 8];
                        dest <<= 8;
                    }

                    if (dest != 0) {
                        int command = packet.getCmd(buffer[39], param);

                        switch (command) {
                            case 0: //getNI
                                break;
                            case 1: // setNI
                                mHandler.obtainMessage(Service_Constants.MESSAGE_SET_NI, bytes, -1, buffer).sendToTarget();
                                break;
                            case 2: // set Disc time
                                break;
                            case 3: // discovery
                                // 디스커버리 와드 1

                                Log.d("finalTest", "--\n\n-----------------Start Read_Discovery Packet -----------------"
                                        +"\n"+"msg send : "+packetHandler.byteArrayToHexString(buffer)
                                        +"\n"+"src : "+packetHandler.getHeaderString(buffer,0,packetHandler.LEN_SRC)
                                        +"\n"+"dest : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_DST,packetHandler.LEN_DST)
                                        +"\n"+"cm : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_CM,packetHandler.LEN_CM)
                                        +"\n"+"hp : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_HP,packetHandler.LEN_HP)
                                        +"\n"+"proto : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_PROTO,packetHandler.LEN_PROTO)
                                        +"\n"+ "id : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_ID,packetHandler.LEN_ID)
                                        +"\n"+ "flags : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_FLAGS,packetHandler.LEN_FLAGS)
                                        +"\n"+ "frag : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_FRAG,packetHandler.LEN_FRAG)
                                        +"\n"+ "seq : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_SEQ,packetHandler.LEN_SEQ)
                                        +"\n"+ "tms : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_TMS,packetHandler.LEN_TMS)
                                        +"\n"+ "len : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_LEN,packetHandler.LEN_LEN)
                                        +"\n"+ "ttl : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_TTL,packetHandler.LEN_TTL)
                                        +"\n"+ "param : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_PARAM,packetHandler.LEN_PARAM)
                                        +"\n"+ "dc : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_DC,packetHandler.LEN_DC)
                                        +"\n"+ "#####################   END Read_Discovery Packet #################### \n\n");

                                int dataLen=packetHandler.getMsgLen(buffer);
                                Log.d("finalTest", "len2 : "+dataLen);
                                Log.d("finalTest", "data : "+packetHandler.getHeaderString(buffer,packetHandler.MASK_DATA,dataLen));

                                byte[] discoverData = packetHandler.getHeaderOffset(buffer,packetHandler.MASK_DATA,dataLen);
                                mHandler.obtainMessage(Service_Constants.MESSAGE_DISCOVERY, bytes, -1, discoverData).sendToTarget();
                                // buffer = Arrays.copyOfRange(buffer, 0, 39);
                                break;
                            case 4: // unicast
                                mHandler.obtainMessage(Service_Constants.MESSAGE_READ, bytes, -1, packet.handleRead(buffer))
                                        .sendToTarget();
                                break;
                            case 5: // broadcast
                                mHandler.obtainMessage(Service_Constants.MESSAGE_READ, bytes, -1, packet.handleRead(buffer))
                                        .sendToTarget();
                                break;
                            case 6: // set channel

                                byte[] ch = new byte[3];
                                ch = Arrays.copyOfRange(buffer, 56, 59);

                                mHandler.obtainMessage(Service_Constants.MESSAGE_CHANNELSET, bytes, -1, ch).sendToTarget();
                                break;

                            case 7:
                                Message msg = mHandler.obtainMessage(Service_Constants.MESSAGE_TOAST);
                                Bundle bundle = new Bundle();
                                bundle.putString(Service_Constants.TOAST, "send result!!");
                                msg.setData(bundle);
                                mHandler.sendMessage(msg);
                                break;
                        }
                    }

                } catch (IOException e) {
                    connectionLost();

                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {

            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Service_Constants.MESSAGE_WRITE, -1, -1, buffer)
                        .sendToTarget();
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

}