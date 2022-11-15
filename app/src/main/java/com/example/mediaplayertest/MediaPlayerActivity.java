package com.example.mediaplayertest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MediaPlayerActivity extends AppCompatActivity {
    private MediaBrowserCompat mediaBrowser;
    MediaControllerCompat mediaController;
    Button playPause;

    MediaPlaybackService mps = new MediaPlaybackService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ...
        // Create MediaBrowserServiceCompat
        setContentView(R.layout.activity_main);

        mediaBrowser = new MediaBrowserCompat(this,
                new ComponentName(this, MediaPlaybackService.class),
                connectionCallbacks,
                null); // optional Bundle



    }

    @Override
    public void onStart() {
        super.onStart();
        mediaBrowser.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop() {
        super.onStop();
        // (see "stay in sync with the MediaSession")
        if (MediaControllerCompat.getMediaController(MediaPlayerActivity.this) != null) {
            MediaControllerCompat.getMediaController(MediaPlayerActivity.this).unregisterCallback(controllerCallback);
        }
        mediaBrowser.disconnect();
    }

    private final MediaBrowserCompat.ConnectionCallback connectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {

                    // Get the token for the MediaSession
                    MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

                    // Create a MediaControllerCompat
                    mediaController =
                            new MediaControllerCompat(MediaPlayerActivity.this, // Context
                                    token);

                    // Save the controller
                    MediaControllerCompat.setMediaController(MediaPlayerActivity.this, mediaController);

                    // Finish building the UI
                    buildTransportControls();
                    Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                    Toast.makeText(getApplicationContext(), "disconnected", Toast.LENGTH_SHORT).show();

                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                    Toast.makeText(getApplicationContext(), "connexion refused", Toast.LENGTH_SHORT).show();

                }
            };

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {}

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                }
            };




    void buildTransportControls()
    {
        // Attach a listener to the button
        playPause = findViewById(R.id.playPause);


        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Since this is a play/pause button, you'll need to test the current state
                // and choose the action accordingly

                Intent intent =  new Intent(getApplicationContext(), MediaPlayerActivity.class);
                intent.putExtra("url", "https://locki.oeilabsolu.ch/streams/63727a6795f0836043d34088.m3u8");

                mediaController.sendCommand("new-song", intent.getExtras(), null);

//                int pbState = MediaControllerCompat.getMediaController(MediaPlayerActivity.this).getPlaybackState().getState();
//                if (pbState == PlaybackStateCompat.STATE_PLAYING) {
//                    MediaControllerCompat.getMediaController(MediaPlayerActivity.this).getTransportControls().pause();
//                } else {
//                    MediaControllerCompat.getMediaController(MediaPlayerActivity.this).getTransportControls().play();
//                }
            }
            });

            MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(MediaPlayerActivity.this);

            // Display the initial state
            MediaMetadataCompat metadata = mediaController.getMetadata();
            PlaybackStateCompat pbState = mediaController.getPlaybackState();

            // Register a Callback to stay in sync
            mediaController.registerCallback(controllerCallback);

    };
}

