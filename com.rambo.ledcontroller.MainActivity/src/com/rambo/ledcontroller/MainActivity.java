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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	// Constants:
	//Suit Modes:
	private static final int WHITE = 1;
	private static final int RED = 5;
	private static final int GREEN = 6;
	private static final int BLUE = 7;
	private static final int MUSIC = 13;
	private static final int RAINBOW = 17;
	private static final int STROBE = 12;
	private static final int PIXFLASH = 14;
	private static final int SIN = 15;
	private static final int SHOOTINGRAINBOW = 16;
	
	//Flags:	
	boolean enabled_Flag = false;
	boolean open_Flag = false;
	boolean led_Flag = true;

    TextView myLabel, mode1, mode2, mode3, mode4, mode5, mode6, mode7, mode8;
    EditText myTextbox;
    SeekBar mySeekBar, seekRed, seekGreen, seekBlue;
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
        
        //Find Buttons:
        
        Button openButton = (Button)findViewById(R.id.open);
        Button sendButton = (Button)findViewById(R.id.send);
        Button closeButton = (Button)findViewById(R.id.close);
        final Button OnOffButton = (Button)findViewById(R.id.btOnOff);
        
        //Find TextViews:
        myLabel = (TextView)findViewById(R.id.label);
        mode1 = (TextView)findViewById(R.id.mode1);
        mode2 = (TextView)findViewById(R.id.mode2);
        mode3 = (TextView)findViewById(R.id.mode3);
        mode4 = (TextView)findViewById(R.id.mode4);
        mode5 = (TextView)findViewById(R.id.mode5);
        mode6 = (TextView)findViewById(R.id.mode6);
        mode7 = (TextView)findViewById(R.id.mode7);
        mode8 = (TextView)findViewById(R.id.mode8);
          
       
        
        myTextbox = (EditText)findViewById(R.id.entry);
        
        // Find SeekBars
        mySeekBar = (SeekBar) findViewById(R.id.seekBrightness);
        seekRed = (SeekBar) findViewById(R.id.seekRed);
        seekGreen = (SeekBar) findViewById(R.id.seekGreen);
        seekBlue = (SeekBar) findViewById(R.id.seekBlue);
        
        
        // TextView OnClicks:
        mode1.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(MUSIC, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        mode2.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(RAINBOW, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        mode3.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(STROBE, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        mode4.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(PIXFLASH, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        mode5.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(SIN, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        mode6.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				try {
					sendData(SHOOTINGRAINBOW, 0);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
        
        
        //Open Button
        openButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                try 
                {
                    findBT();
                    if (enabled_Flag){ // only try open if 'on'
                    	openBT();
                    }
                }
                catch (IOException ex) { }
            }
        });
        
        //Send Button
        sendButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
     
            	try 
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

        //OnOff button
        OnOffButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
            	if(led_Flag){ //turn led's off
            		try {
    					sendData(WHITE, 0);
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
            		led_Flag = false;
            		OnOffButton.setText(R.string.btOn);
            		
            	}else{ // turn led's on
            		try {
    					sendData(WHITE, 50);
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
            		led_Flag = true;
            		OnOffButton.setText(R.string.btOff);
            		
            	}

            }
        });
        
        
        
        // SeekBar listeners: 
        
        mySeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    		public void onProgressChanged(SeekBar seekBar, int progress,
    				boolean fromUser) {
    			
    			try {
					sendData(WHITE, progress);
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
    		
        seekRed.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

        	
        		public void onProgressChanged(SeekBar seekBar, int progress,
        				boolean fromUser) {
        			
        			try {
    					sendData(RED, progress);
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

        seekGreen.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    		public void onProgressChanged(SeekBar seekBar, int progress,
    				boolean fromUser) {
    			
    			try {
					sendData(GREEN, progress);
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
    
        seekBlue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
    		public void onProgressChanged(SeekBar seekBar, int progress,
    				boolean fromUser) {
    			
    			try {
					sendData(BLUE, progress);
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
    
    @Override
    public void onStart(){
    	super.onStart();
    	
    	// check if we have an active device
    	if(enabled_Flag){
    		try {
				openBT();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}else{
    		findBT();
            if (enabled_Flag){ // only try open if 'on'
            	try {
					openBT();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
    	}
    }
    
    @Override
	public void onStop() {
    	super.onStop();
    	
    	// clean up a bit
    	if (!stopWorker){
	    	stopWorker = true;
	    	try{
		        mmOutputStream.close();
		        mmInputStream.close();
		        mmSocket.close();
		        open_Flag = false;
	    	}catch(Exception ex){
	    		
	    	}
    	}
    	
    	
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
        	enabled_Flag = false;
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
                    myLabel.setText("Bluetooth Device Found: linvor");
                    break;
                }
            }
        }
        myLabel.setText("Bluetooth Device NOT Found!");
        enabled_Flag = true;
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
        open_Flag = true;
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
    	if(open_Flag)
    	{
	        String msg = myTextbox.getText().toString();
	        msg += "\n";
	        mmOutputStream.write(msg.getBytes());
	        myLabel.setText("Data Sent");
    	}
    }
    
    void sendData(int data) throws IOException
    {
    	if(open_Flag){
	        String msg = intToLZstr(data);
	        msg += "\n";
	        mmOutputStream.write(msg.getBytes());
	        myLabel.setText("Data Sent");
	    }
    }
    
    void sendData(int setting, int data) throws IOException // configure a setting with certain data on the device
    {
    	if(open_Flag){
	        String msg = intToLZstr(setting); // setting value
	        msg += ','; // delimiter
	        msg += intToLZstr(data); // data value for setting
	        msg += "\n"; // end of line delimiter
	        mmOutputStream.write(msg.getBytes());
	        myLabel.setText("LED setting: " + setting + " changed");
	    }
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
        enabled_Flag = false;
        myLabel.setText("Bluetooth Closed");
    }
}
