package com.rambo.ledcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    TextView myLabel;
    EditText myTextbox;
    SeekBar mySeekBar;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        myLabel = (TextView)findViewById(R.id.label);
        myTextbox = (EditText)findViewById(R.id.entry);
        mySeekBar = (SeekBar) findViewById(R.id.seekBar1);
        
        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try 
                {
                    findBT();
                    openBT();
                }
                catch (IOException ex) { }
            }
        });
        
        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
     
            	\try 
                {
                    sendData();
                }
                catch (IOException ex) { }
            }
        });
        
        //Close button
        closeButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try 
                {
                    closeBT();
                }
                catch (IOException ex) { }
            }
        });
        
        mySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    		public void onProgressChanged(SeekBar seekBar, int progress,
    				boolean fromUser) {
    			
    			try {
    				int white = 1;
					sendData(white, progress);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    			
    		}

    		public void onStartTrackingTouch(SeekBar seekBar) {
    			// TODO Auto-generated method stub
    		}

    		public void onStopTrackingTouch(SeekBar seekBar) {
    			// TODO Auto-generated method stub
    		}
    	});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    void findBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null)
        {
        	Toast.makeText(this, "Bluetooth is not available.",
					Toast.LENGTH_LONG).show();
			finish();
            myLabel.setText("No bluetooth adapter available");
            return;
        }
        
        if(!mBluetoothAdapter.isEnabled())
        {
        	//crashes app??
            // Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBluetooth, 0);
        	Toast.makeText(this,
					"Please enable your BT and re-run this program.",
					Toast.LENGTH_LONG).show();
			return;
        }
        
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("linvor")) 
                {
                    mmDevice = device;
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device Found");
    }
    
    void openBT() throws IOException
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);        
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        mmInputStream = mmSocket.getInputStream();
        
        beginListenForData();
        
        myLabel.setText("Bluetooth Opened");
        Toast.makeText(this, "Connected!! :D",
				Toast.LENGTH_LONG).show();
    }
    
    void beginListenForData()
    {
        final Handler handler = new Handler(); 
        final byte delimiter = 10; //This is the ASCII code for a newline character
        
        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {                
               while(!Thread.currentThread().isInterrupted() && !stopWorker)
               {
                    try 
                    {
                        int bytesAvailable = mmInputStream.available();                        
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            myLabel.setText(data);
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } 
                    catch (IOException ex) 
                    {
                        stopWorker = true;
                    }
               }
            }
        });

        workerThread.start();
    }
    
    
    void sendData() throws IOException
    {
        String msg = myTextbox.getText().toString();
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }
    
    void sendData(int data) throws IOException
    {
        String msg = intToLZstr(data);
        msg += "\n";
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("Data Sent");
    }
    
    void sendData(int setting, int data) throws IOException // configure a setting with certain data on the device
    {
        String msg = intToLZstr(setting); // setting value
        msg += ','; // delimiter
        msg += intToLZstr(data); // data value for setting
        msg += "\n"; // end of line delimiter
        mmOutputStream.write(msg.getBytes());
        myLabel.setText("LED setting: " + setting + " changed");
    }
    
	public String intToLZstr(int text) { // should convert an int to a 3 digit string, 000 - 999?

		String tmpSTR = "";
		if (text < 100) {
			tmpSTR = "0" + text;
		}
		if (text < 10) {
			tmpSTR = "00" + text;
		}
		if (text > 99) {
			tmpSTR = "" + text;
		}

		return tmpSTR;
	}
    
    void closeBT() throws IOException
    {
        stopWorker = true;
        mmOutputStream.close();
        mmInputStream.close();
        mmSocket.close();
        myLabel.setText("Bluetooth Closed");
    }
}
