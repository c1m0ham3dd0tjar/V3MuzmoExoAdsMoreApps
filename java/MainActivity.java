package com.;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.media.session.PlaybackStateCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.snackbar.Snackbar;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "MainActivity";

    private ImageView playStopBtn, gifImageView;
    private DrawableImageViewTarget imageViewTarget;
    private boolean mBound = false;
    private static MusicService musicService;
    private String streamUrl = "  ";
    private static final int READ_PHONE_STATE_REQUEST_CODE = 22;
    private BroadcastReceiver broadcastReceiver;
    private Snackbar snackbar;
    private AudioManager audio;
    /////
    static int NumMusicSelected;
    ListView listview;
    final static ArrayList<AdapterListView> list_items = new ArrayList<AdapterListView>();
    TextView tv_Title, textView_item, custom_Title, text_privacy_2, custom_rate;
    ImageView  /*btn_replay,*/more_apps, btn_next, btn_pre;
    ImageView custom_pre, custom_next, custom_play;
    // Typeface typeface;
    Intent intent;
    GridView grid_view;
    private ConsentForm form;
    private AdView mAdView;
    private InterstitialAd mInterstitialAd;
    boolean listviewclicked = false;
    boolean isPlaying = false;
    String webs;
    String website;
    String baselink;
    String apps[] = {
           
    };
    static String songTitles[] = {

            

    };


    String[] sounds = {

           

    };

    public void backPressed() {
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.back_pressed, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MaDialog);
        alert.setView(alertLayout);
        alert.setCancelable(true);
        TextView yes = (TextView) alertLayout.findViewById(R.id.yes);
        TextView rate_app = (TextView) alertLayout.findViewById(R.id.rate_app);
        final AlertDialog dialog = alert.create();
        rate_app.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
                dialog.cancel();

            }
        });
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.super.onBackPressed();
                musicService.stop();
            }
        });
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        backPressed();
    }

    ////
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MusicService.MusicBinder mServiceBinder = (MusicService.MusicBinder) iBinder;
            musicService = mServiceBinder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            System.exit(0);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webs = getResources().getString(R.string.web);
        website = webs + ".org";
        baselink = "https://" + website;
        /////////////////
        MobileAds.initialize(this, "ca-app-pub-");


        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        ConsentInformation consentInformation = ConsentInformation.getInstance(getApplicationContext());
        String[] publisherIds = {"pub-"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override

            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                if (ConsentInformation.getInstance(getApplicationContext()).isRequestLocationInEeaOrUnknown()) {
                    if (consentStatus == ConsentStatus.UNKNOWN) {

                        form.load();
                    } else if (consentStatus == ConsentStatus.PERSONALIZED) {
                        ConsentInformation.getInstance(getApplicationContext())
                                .setConsentStatus(ConsentStatus.PERSONALIZED);
                        loadAdsPersonalize();

                    } else if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
                        ConsentInformation.getInstance(getApplicationContext())
                                .setConsentStatus(ConsentStatus.NON_PERSONALIZED);

                        loadAdsNonPersonalize();
                    }
                } else {
                    loadAdsPersonalize();
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {

            }
        });
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });
        URL privacyUrl = null;
        try {
            // AdRequest.BuilderTODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL("");

            // privacyUrl = new URL("https://www.blogger.com/privacy_policy");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        // Consent form loaded successfully.
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        // Consent form was closed.
                        if (consentStatus == ConsentStatus.PERSONALIZED) {
                            ConsentInformation.getInstance(getApplicationContext())
                                    .setConsentStatus(ConsentStatus.PERSONALIZED);
                            loadAdsPersonalize();
                        } else if (consentStatus == ConsentStatus.NON_PERSONALIZED) {
                            ConsentInformation.getInstance(getApplicationContext())
                                    .setConsentStatus(ConsentStatus.NON_PERSONALIZED);
                            loadAdsNonPersonalize();
                        }
                        //work here
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        // Consent form error.
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();


//////////////////
        ///
//        typeface = Typeface.createFromAsset(getAssets(), "Mali-Medium.ttf");
        //   typeface = getResources().getFont(R.font.malimedium);
        tv_Title = (TextView) findViewById(R.id.tv_Title);
        btn_pre = (ImageView) findViewById(R.id.btn_pre);
        btn_next = (ImageView) findViewById(R.id.btn_next);
        more_apps = (ImageView) findViewById(R.id.moreapps);
        playStopBtn = (ImageView) findViewById(R.id.playStopBtn);
        //btn_replay = (ImageView) findViewById(R.id.btn_replay);
        listview = (ListView) findViewById(R.id.listview);
        for (int i = 0; i < sounds.length; i++) {
            list_items.add(new AdapterListView(songTitles[i]));
        }
        final ListAdapter listAdapter = new ListAdapter(list_items);
        listview.setAdapter(listAdapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mInterstitialAd.isLoaded() & i % 3 == 0) {
                    mInterstitialAd.show();
                } else {
                    isPlaying = true;
                    musicService.play(baselink + sounds[i]);
                    NumMusicSelected = i;
                    tv_Title.setText(songTitles[i]);
                    listviewclicked = true;

                    Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    playStopBtn.startAnimation(animFadein);
                    showCustomLayoutSong();
                    // state();
                }
            }
        });
        playStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listviewclicked)
                    playAndStop();
            }
        });
     /*   btn_replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                musicService.play(website+sounds[NumMusicSelected]);
            }
        });
       */
        more_apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreApps();
            }
        });
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listviewclicked)
                    next();

            }
        });
        btn_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listviewclicked)
                    pre();
            }
        });
        gifImageView = findViewById(R.id.gifImageView);

        gifImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCustomLayoutSong();
                //state();
            }
        });

        ///
        //radioStationNowPlaying = findViewById(R.id.radioStationNowPlaying);
        //radioStationNowPlaying.setText("Now streaming   /ssmusic");

        imageViewTarget = new DrawableImageViewTarget(gifImageView);

        snackbar = Snackbar.make(findViewById(R.id.myCoordinatorLayout), "No internet connection", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("DISMISS", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });

        //processPhoneListenerPermission();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                if (tm != null) {
                    if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
                        if (musicService.isPlaying()) {
                            musicService.stop();
                            playStopBtn.setImageResource(R.drawable.ic_play);
                        }
                    }
                }

                if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    NetworkInfo networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        if (snackbar.isShown()) {
                            snackbar.dismiss();
                        }
                        playStopBtn.setEnabled(true);
                    } else if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.DISCONNECTED) {
                        playStopBtn.setEnabled(false);
                        snackbar.show();
                    }
                }

                int playerState = intent.getIntExtra("state", 0);
                if (playerState == PlaybackStateCompat.STATE_BUFFERING) {
                    Glide.with(MainActivity.this).load(R.drawable.not_playing).into(imageViewTarget);
                    playStopBtn.setImageResource(R.drawable.ic_stop_black_24dp);
                } else if (playerState == PlaybackStateCompat.STATE_PLAYING) {
                    playStopBtn.setImageResource(R.drawable.ic_stop_black_24dp);
                    Glide.with(MainActivity.this).load(R.drawable.playing).into(imageViewTarget);
                    int musicVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (musicVolume == 0) {
                        Toast.makeText(MainActivity.this, "Volume is muted", Toast.LENGTH_LONG).show();
                    }
                } else if (playerState == PlaybackStateCompat.STATE_PAUSED) {
                    playStopBtn.setImageResource(R.drawable.ic_play);
                    Glide.with(MainActivity.this).load(R.drawable.not_playing).into(imageViewTarget);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PHONE_STATE");
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, filter);

        createNotificationChannel();

        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void playAndStop() {
        if (!musicService.isPlaying()) {
            musicService.play(baselink + sounds[NumMusicSelected]);
            playStopBtn.setImageResource(R.drawable.ic_stop_black_24dp);
            custom_play.setImageResource(R.drawable.ic_stop_black_24dp);
            isPlaying = true;

        } else {
            musicService.stop();
            playStopBtn.setImageResource(R.drawable.ic_play);
            custom_play.setImageResource(R.drawable.ic_play);
            isPlaying = false;

        }
        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        playStopBtn.startAnimation(animFadein);
        gifImageView.startAnimation(animFadein);
    }


    private void pre() {
        NumMusicSelected--;
        if (NumMusicSelected < 0)
            NumMusicSelected = sounds.length - 1;
        isPlaying = true;
        musicService.play(baselink + sounds[NumMusicSelected]);
        custom_play.setImageResource(R.drawable.ic_stop_black_24dp);
        tv_Title.setText(songTitles[NumMusicSelected]);
        custom_Title.setText(songTitles[NumMusicSelected]);
    }

    private void next() {
        NumMusicSelected++;
        if (NumMusicSelected == sounds.length)
            NumMusicSelected = 0;

        isPlaying = true;
        musicService.play(baselink + sounds[NumMusicSelected]);
        custom_play.setImageResource(R.drawable.ic_stop_black_24dp);
        tv_Title.setText(songTitles[NumMusicSelected]);
        custom_Title.setText(songTitles[NumMusicSelected]);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(MainActivity.this, MusicService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        LocalBroadcastManager.getInstance(this).registerReceiver((broadcastReceiver),
                new IntentFilter("com.example.exoplayer.PLAYER_STATUS")
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (musicService != null && musicService.isPlaying()) {
            playStopBtn.setImageResource(R.drawable.ic_stop_black_24dp);
            Glide.with(MainActivity.this).load(R.drawable.playing).into(imageViewTarget);
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "activity destroy called");
//        if (mBound) {
//            unbindService(serviceConnection);
//            mBound = false;
//        }
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
        Log.i(TAG, "activity destroyed");
        super.onDestroy();
    }

    public void playStop(View view) {
        if (!musicService.isPlaying()) {
            musicService.play(streamUrl);
        } else {
            musicService.stop();
        }
        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        playStopBtn.startAnimation(animFadein);
    }

    private void processPhoneListenerPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, READ_PHONE_STATE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_PHONE_STATE_REQUEST_CODE) {
            if (!(grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(), "Permission not granted.\nThe player will not be able to pause music when phone is ringing.", Toast.LENGTH_LONG).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            String description = "Songs of " + getString(R.string.app_name);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("radio_playback_channel", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    //class AdapterListView
    public class AdapterListView {

        String titles;

        public AdapterListView(String titles) {
            this.titles = titles;
        }

    }

    class ListAdapter extends BaseAdapter {
        ArrayList<AdapterListView> songAdapters = new ArrayList<AdapterListView>();

        ListAdapter(ArrayList<AdapterListView> arrayListObjectOfSongAdapter) {
            this.songAdapters = arrayListObjectOfSongAdapter;
        }


        @Override
        public int getCount() {
            return songTitles.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            LayoutInflater layoutInflater = getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.listview_item, null);
            textView_item = (TextView) view.findViewById(R.id.title_item);
            textView_item.setText(songAdapters.get(i).titles);
            //textView_item.setTypeface(typeface);
            return view;
        }
    }

    private void showForm() {
        form.show();
    }

    private void loadAdsNonPersonalize() {
        Bundle extras = new Bundle();
        extras.putString("npa", "1");
        mAdView = findViewById(R.id.adView);
        AdRequest request = new AdRequest.Builder()
                .addNetworkExtrasBundle(AdMobAdapter.class, extras)
                .build();
        mAdView.loadAd(request);
    }


    private void loadAdsPersonalize() {
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    public void moreApps() {


        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.activity_more, null);
        // ImageView  to_store=(ImageView)alertLayout.findViewById(R.id.to_store);
        TextView custom_more = (TextView) alertLayout.findViewById(R.id.custom_more);
        custom_more.setText(R.string.more);

        custom_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/developer?id="));
                startActivity(intent);

            }
        });
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MaDialogStore);
        alert.setView(alertLayout);
        alert.setCancelable(true);

        grid_view = (GridView) alertLayout.findViewById(R.id.gridview);

        grid_view.setAdapter(new ImageAdapater(this));
        grid_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.saas.sgm." + apps[arg2]));
                startActivity(intent);
            }


        });
        AlertDialog dialog = alert.create();
        dialog.show();
    }

    public class ImageAdapater extends BaseAdapter {
        private Context CTX;
        private Integer image_id[] = {
            

        };

        public ImageAdapater(Context CTX) {

            this.CTX = CTX;
        }

        @Override
        public int getCount() {

            return apps.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            ImageView img;
            if (arg1 == null) {
                img = new ImageView(CTX);
                img.setLayoutParams(new GridView.LayoutParams(250, 250));
                img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                img.setPadding(8, 8, 8, 8);

            } else {
                img = (ImageView) arg1;
            }
            img.setImageResource(image_id[arg0]);
            return img;


            // TODO Auto-generated method stub
            // return null;
        }


    }

    public void showCustomLayoutSong() {
        //state();
        LayoutInflater inflater = getLayoutInflater();
        View alertLayout = inflater.inflate(R.layout.custom_layout_song, null);
        custom_rate = (TextView) alertLayout.findViewById(R.id.custom_rate);
        custom_Title = (TextView) alertLayout.findViewById(R.id.custom_Title);
        text_privacy_2 = (TextView) alertLayout.findViewById(R.id.text_privacy_2);
        custom_pre = (ImageView) alertLayout.findViewById(R.id.custom_pre);
        custom_next = (ImageView) alertLayout.findViewById(R.id.custom_next);
        custom_play = (ImageView) alertLayout.findViewById(R.id.custom_play);
        custom_Title.setText(songTitles[NumMusicSelected]);
        text_privacy_2.setText(getString(R.string.text_privacy_2));
        custom_rate.setText(getString(R.string.rate));
        if (isPlaying) {
            custom_play.setImageResource(R.drawable.ic_stop_black_24dp);
            //     Toast.makeText(getApplicationContext()," playing",Toast.LENGTH_SHORT).show();

        } else {
            custom_play.setImageResource(R.drawable.ic_play);
            //   Toast.makeText(getApplicationContext()," not playing",Toast.LENGTH_SHORT).show();
        }

        Animation animFadein = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        custom_play.startAnimation(animFadein);
        gifImageView.setAnimation(animFadein);

        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.MaDialog);

        alert.setView(alertLayout);
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert.setCancelable(true);
        //  alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        //         ready();
        custom_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playAndStop();
            }
        });
        custom_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                next();
            }
        });
        custom_rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName()));
                startActivity(intent);
            }
        });


        custom_pre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pre();
            }
        });

        AlertDialog dialog = alert.create();
        if (listviewclicked) {
            dialog.show();
        }
    }


}
