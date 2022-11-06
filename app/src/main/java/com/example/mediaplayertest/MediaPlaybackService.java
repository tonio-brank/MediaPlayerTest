package com.example.mediaplayertest;

import static androidx.core.content.PackageManagerCompat.LOG_TAG;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaDescription;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.os.SystemClock;
import android.service.media.MediaBrowserService;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;
import androidx.media.session.MediaButtonReceiver;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserService {
    private static final String MY_MEDIA_ROOT_ID = "media_root_id";
    private static final String MY_EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSession mediaSession;
    private PlaybackState.Builder stateBuilder;

    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate() {
        super.onCreate();

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.test);

                // Create a MediaSessionCompat
        mediaSession = new MediaSession(getApplicationContext(), "LOG_TAG");

        // Enable callbacks from MediaButtons and TransportControls
//        mediaSession.setFlags(
//                MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
//                        MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        stateBuilder = new PlaybackState.Builder().setActions(
                        PlaybackState.ACTION_PLAY | PlaybackState.ACTION_PLAY_PAUSE |
                                PlaybackState.ACTION_PLAY_FROM_MEDIA_ID | PlaybackState.ACTION_PAUSE |
                                PlaybackState.ACTION_SKIP_TO_NEXT | PlaybackState.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackState.ACTION_SEEK_TO | PlaybackState.ACTION_FAST_FORWARD);
//                .setState(PlaybackState.STATE_PLAYING, position, speed, SystemClock.elapsedRealtime())
        mediaSession.setPlaybackState(stateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        mediaSession.setCallback(callback);

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mediaSession.getSessionToken());
//        Toast.makeText(getApplicationContext(), mediaSession.getSessionToken().toString(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid,
                                 Bundle rootHints) {

        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        return new BrowserRoot(MY_MEDIA_ROOT_ID, null);

    }

    @Override
    public void onLoadChildren(final String parentMediaId,
                               final Result<List<MediaBrowser.MediaItem>> result) {

        //  Browsing not allowed
        if (TextUtils.equals(MY_EMPTY_MEDIA_ROOT_ID, parentMediaId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowser.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MY_MEDIA_ROOT_ID.equals(parentMediaId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }

    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);



    MediaSession.Callback callback = new
            MediaSession.Callback() {
                @Override
                public void onPlay() {
                    startService(new Intent(getApplicationContext(), MediaBrowserService.class));
                    stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1);
                    mediaSession.setPlaybackState(stateBuilder.build());


                    mediaSession.setActive(true);
                    Toast.makeText(getApplicationContext(), "playing from service", Toast.LENGTH_SHORT).show();
                    // Given a media session and its context (usually the component containing the session)
                    // Create a NotificationCompat.Builder

                    // Get the session's metadata


                    MediaDescription.Builder md = new MediaDescription.Builder();
                    md.setTitle("Haircut for men");
                    md.setSubtitle("test");
                    md.setDescription("Je t'aime mon eli");

                    MediaMetadata.Builder mm = new MediaMetadata.Builder();
                    mediaSession.setMetadata(
                            new MediaMetadata.Builder()
                                    .putString(MediaMetadata.METADATA_KEY_ARTIST, "Haircut for men")
                                    .putString(MediaMetadata.METADATA_KEY_TITLE, "ching chong")
                                    .putLong(
                                            MediaMetadata.METADATA_KEY_DURATION,
                                            mediaPlayer.getDuration()
                                    )
                                    .putString(MediaMetadata.METADATA_KEY_DISPLAY_DESCRIPTION, "Test Description")

//                                    .putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, largeIcon)
                                    .build()
                    );


                    MediaController controller = mediaSession.getController();
//                    MediaMetadataCompat mediaMetadata = controller.getMetadata();
//                    MediaDescriptionCompat description = mediaMetadata.getDescription();


                    NotificationChannel chan = new NotificationChannel("mediaPlayer", "buttons", NotificationManager.IMPORTANCE_NONE);
                    chan.setLightColor(Color.BLUE);
                    chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    assert manager != null;
                    manager.createNotificationChannel(chan);
                    Notification.Builder builder = new Notification.Builder(getApplicationContext(), "mediaPlayer");

                    builder
                            // Add the metadata for the currently playing track
                            .setContentTitle("Title")
                            .setContentText("Subtitle")
                            .setSubText("Description")
//                            .setLargeIcon(description.getIconBitmap())

                            // Enable launching the player by clicking the notification
                            .setContentIntent(controller.getSessionActivity())

                            // Stop the service when the notification is swiped away
                            .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                    PlaybackStateCompat.ACTION_STOP))

                            // Make the transport controls visible on the lockscreen
                            .setVisibility(Notification.VISIBILITY_PUBLIC)

                            // Add an app icon and set its accent color
                            // Be careful about the color
                            .setSmallIcon(R.drawable.ic_launcher_background)
                            .setColor(Color.argb(255, 255, 0, 0))
                            .setColorized(true)
                            // Add a pause button
                            .addAction(new Notification.Action(
                                    R.drawable.ic_launcher_background, "pause",
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                            PlaybackState.ACTION_PLAY_PAUSE)))
                            .addAction(new Notification.Action(
                                    R.drawable.ic_launcher_foreground, "fast",
                                    MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
                                            PlaybackState.ACTION_FAST_FORWARD)))

                            // Take advantage of MediaStyle features
                            .setStyle(new Notification.DecoratedMediaCustomViewStyle()
                                    .setMediaSession(mediaSession.getSessionToken()));
                                    // Add a cancel button
//                                    .setShowCancelButton(true)
//                                    .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(getApplicationContext(),
//                                            PlaybackStateCompat.ACTION_STOP)));

//                    Notification.MediaStyle ms = new Notification.MediaStyle();
//                    ms.setMediaSession(mediaSession.getSessionToken());
//                    builder.setStyle(ms).
//                            setContentTitle("Title")
//                            .setContentText("Subtitle")
//                            .setSubText("Description");

//                            .setLargeIcon(description.getIconBitmap());
// Display the notification and place the service in the foreground



                    startForeground(1, builder.build());
                    mediaPlayer.start(); // no need to call prepare(); create() does that for you

                }

                @Override
                public void onStop() {
                    Toast.makeText(getApplicationContext(), "stopping from service", Toast.LENGTH_SHORT).show();
                    stopSelf();

                }

                @Override
                public void onPause() {
                    Toast.makeText(getApplicationContext(), "pausing from service", Toast.LENGTH_SHORT).show();
                    mediaSession.setActive(false);
                    stateBuilder.setState(PlaybackState.STATE_PAUSED, mediaPlayer.getCurrentPosition(), 0);
                    mediaSession.setPlaybackState(stateBuilder.build());
                    mediaPlayer.pause(); // no need to call prepare(); create() does that for you
                    stopSelf();

                }

                @Override
                public void onSeekTo(long pos) {
                    super.onSeekTo(pos);
                    mediaPlayer.seekTo((int) pos);
                    stateBuilder.setState(PlaybackState.STATE_PLAYING, mediaPlayer.getCurrentPosition(), 1);
                    mediaSession.setPlaybackState(stateBuilder.build());
                }

                @Override
                public void onFastForward() {
                    super.onFastForward();
                    Toast.makeText(getApplicationContext(), "fast f", Toast.LENGTH_SHORT).show();
                    PlaybackParams pp = mediaPlayer.getPlaybackParams();
                    pp.setSpeed(pp.getSpeed()*1.1f);
                    mediaPlayer.setPlaybackParams(pp);
                }
            };
}