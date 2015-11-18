package edu.nju.ics.btsona2;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.nju.ics.btsona2.player.AudioPlayer;
import edu.nju.ics.btsona2.recorder.AudioRecorder;

@SuppressLint("NewApi")
public class MainActivity extends ActionBarActivity implements OnClickListener
// ,AudioManager.OnAudioFocusChangeListener
// , BluetoothProfile.ServiceListener
{

	public static final String folderName = "microphone-sona";

	/**
	 * Buttons
	 */
	private Button button_record_start, button_record_stop;
	private AudioManager m_amAudioManager;
	/**
	 * Recorder and Player
	 */
	private AudioPlayer audioPlayer;
	private AudioRecorder audioRecorderMic;

	private BluetoothHeadset bhs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		this.button_record_start = (Button) findViewById(R.id.button_start);
		this.button_record_stop = (Button) findViewById(R.id.button_stop);
		this.button_record_start.setOnClickListener(this);
		this.button_record_stop.setOnClickListener(this);

		this.button_record_start.setClickable(true);
		this.button_record_stop.setClickable(false);

		this.m_amAudioManager = (AudioManager) this.getBaseContext()
				.getSystemService(Context.AUDIO_SERVICE);

		this.audioPlayer = new AudioPlayer();
		this.audioRecorderMic = new AudioRecorder();

		// BluetoothAdapter bad = BluetoothAdapter.getDefaultAdapter();
		// // bad.getProfileProxy(this, this, BluetoothProfile.HEADSET);
		// bad.

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// private Object mutex = new Object();

	private void turnOnBluetooth() {

		final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				int state = intent.getIntExtra(
						AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
				if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
					System.err.println("bluetooth connected");
					unregisterReceiver(this);

				} else if (AudioManager.SCO_AUDIO_STATE_DISCONNECTED == state) {
					System.err.println("bluetooth disconnected");

				}
			}
		};

		// registerReceiver(broadcastReceiver, new IntentFilter(
		// AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
		registerReceiver(broadcastReceiver, new IntentFilter(
				AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED));
		// Start the timer

		try {

			// m_amAudioManager.setMode(AudioManager.MODE_INVALID);
			// Android 2.2 onwards supports BT SCO for non-voice call
			// use
			// case
			// Check the Android version whether it supports or not.

			if (m_amAudioManager.isBluetoothScoAvailableOffCall()) {
				m_amAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
				if (m_amAudioManager.isBluetoothScoOn()) {
					m_amAudioManager.stopBluetoothSco();
					m_amAudioManager.startBluetoothSco();
					System.err.println("Bluetooth SCO On!");
				} else {
					System.err.println("Bluetooth Sco Off!");
					m_amAudioManager.startBluetoothSco();
				}

			} else {
				System.err.println("Bluetooth SCO not available");
			}
		} catch (Exception e) {
			System.err.println("sco elsepart startBluetoothSCO " + e);
			unregisterReceiver(broadcastReceiver);
		}
	}

	private void turnOffBluetooth() {
		this.m_amAudioManager.setMode(AudioManager.MODE_NORMAL);
		this.m_amAudioManager.stopBluetoothSco();
		this.m_amAudioManager.setBluetoothScoOn(false);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.button_start:

			this.turnOnBluetooth();
			audioPlayer.startPlay(m_amAudioManager);
			audioRecorderMic.startRecording(folderName, "record"
					+ AudioRecorder.AUDIO_RECORDER_FILE_EXT_WAV,
					MediaRecorder.AudioSource.MIC);
			this.button_record_start.setClickable(false);
			this.button_record_stop.setClickable(true);

			this.button_record_start.setBackgroundColor(Color.LTGRAY);
			this.button_record_stop.setBackgroundColor(Color.YELLOW);

			break;
		case R.id.button_stop:
			this.button_record_start.setClickable(true);
			this.button_record_stop.setClickable(false);

			this.button_record_start.setBackgroundColor(Color.YELLOW);
			this.button_record_stop.setBackgroundColor(Color.LTGRAY);

			this.audioPlayer.stopPlay();
			this.audioRecorderMic.stopRecording();
			this.turnOffBluetooth();
			break;
		}
	}
	//
	// @Override
	// public void onServiceConnected(int proxy, BluetoothProfile profile) {
	// System.err.println("start voice recognition");
	// if (proxy == BluetoothProfile.HEADSET) {
	// this.bhs = (BluetoothHeadset) profile;
	// this.bhs.startVoiceRecognition(this.bhs.getConnectedDevices()
	// .get(0));
	// }
	// }
	//
	// @Override
	// public void onServiceDisconnected(int arg0) {
	// // TODO Auto-generated method stub
	//
	// }

}
