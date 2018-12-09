package com.example.thampotter.musiccustom;

import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.example.thampotter.musiccustom.MusicPlayer.PLAYER_IDLE;
import static com.example.thampotter.musiccustom.MusicPlayer.PLAYER_PLAY;

public class PlayMusic extends AppCompatActivity implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener, MusicPlayer.onCompletionListener {

    private ListView lvSong;
    private TextView tvTitle, tvArtist, tvTimeProcess, tvTimetotal;
    private SeekBar sbProcess;
    private ImageView ivShuffle, ivPrevious, ivPlay, ivNext, ivRepeat;
    private ArrayList<String> paths;//lưu tất cả các đường dẫn của bài hát
    private int timeProcess, timeTotal;
    private PlayListAdapter adapter;
    private MusicPlayer musicPlayer;
    private boolean isRunning;
    private int UPDATE_TIME = 1;
    private int timeCurrent;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("bainhac");
        String bh = bundle.getString("lk");

//        playMusic(bh);

        //ánh xạ
        initView();

        //bắt sự kiện các nút click
        initListener();

        //thêm nội dung cho chương trình
        initComponent();
    }

    private void initComponent() {
        //đọc danh sách nhạc trong thư mục ZingMp3
        //nếu muốn đọc toàn thẻ nhớ thì tìm hiểu về MediaStore.Audio.Media
        initList();
        adapter = new PlayListAdapter(App.getContext(), paths);
//        lvSong.setAdapter(adapter);
        musicPlayer = new MusicPlayer();
        musicPlayer.setOnCompletionListener(this);
    }

    private void initList() {
        paths = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Zing Mp3";
        File file = new File(path);
        //lấy tất cả các file trong thư mục
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            //đọc tất cả cá file trong thư mục và thêm vào list nhạc
            String s = files[i].getName();
            if (s.endsWith(".mp3") || s.endsWith("wav")) {
                //kiểm tra nó có phải đuôi nhạc hay không. nếu là đuôi nhạc mới lấy

                paths.add(files[i].getAbsolutePath());

            }
        }
    }

    private void initListener() {
        /*lvSong.setOnItemClickListener(this);*/
        ivShuffle.setOnClickListener(this);
        ivPrevious.setOnClickListener(this);
        ivPlay.setOnClickListener(this);
        ivNext.setOnClickListener(this);
        ivRepeat.setOnClickListener(this);
        sbProcess.setOnSeekBarChangeListener(this);
    }

    private void initView() {
        /*lvSong = findViewById(R.id.lv_song);*/
        tvTitle = findViewById(R.id.TvTitle);
        tvArtist = findViewById(R.id.TvArtist);
        tvTimeProcess = findViewById(R.id.Time_process);
        tvTimetotal = findViewById(R.id.Time_total);
        sbProcess = findViewById(R.id.Sb_process);
        ivShuffle = findViewById(R.id.img_shuffle);
        ivPrevious = findViewById(R.id.img_prev);
        ivPlay = findViewById(R.id.img_play);
        ivNext = findViewById(R.id.img_next);
        ivRepeat = findViewById(R.id.img_repeat);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE_TIME) {
                timeCurrent = musicPlayer.getTimeCurrent();
                tvTimeProcess.setText(getTimeFormat(timeCurrent));
                sbProcess.setProgress(timeCurrent);
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

        this.position = position;
        String path = paths.get(position);
        playMusic(path);
    }

    private void playMusic(String path){

        if (musicPlayer.getState() == PLAYER_PLAY){

            musicPlayer.stop();

        }

        musicPlayer.setup(path);
        musicPlayer.play();

        ivPlay.setImageResource(R.drawable.pause);
        //set thời gian, set tên bài hát, set tên ca sĩ
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(paths.get(position));
        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        tvArtist.setText(artist);
        tvTitle.setText(title);
        isRunning = true;

        //set thời gian
        tvTimetotal.setText(getTimeFormat(musicPlayer.getTimetotal()));
        tvTimeProcess.setText(getTimeFormat(musicPlayer.getTimeCurrent()));


        /*tvTimetotal.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) timeTotal),
                TimeUnit.MILLISECONDS.toSeconds((long) timeTotal) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) timeTotal))));*/
        //process time --- setup seekbar
        sbProcess.setMax(musicPlayer.getTimetotal());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    Message message = new Message();
                    message.what = UPDATE_TIME;
                    handler.sendMessage(message);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private String getTimeFormat(long time) {
        String tm = "";
        int h, s, m;
       /* String tmp = "";


        //chuyển thời gian sang đúng định dạng

        int s = (int) ((time % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        int m = (int) (time % (1000 * 60 * 60));
        int h = (int) (time / (1000 * 60 * 60));

        if (h > 0){
            tm = h + ":";
        }
        //thêm vào số 0 nếu có 1 chữ số;
        if (s < 10){
            s =
        }*/
        //giây
        s = (int) (time % 60);
        m = (int) ((time - s) / 60);
        if (m >= 60) {
            h = m / 60;
            m = m % 60;
            if (h > 0) {
                if (h < 10) {
                    tm += "0" + h + ":";
                } else
                    tm += h + ":";
            }
        }

        if (m < 10) {
            tm += "0" + m + ":";
        } else
            tm += m + ":";
        if(s < 10){
            tm += "0"+s;
        }else{
            tm += s;
        }
        return tm;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_next:
                nextMusic();
                break;
            case  R.id.img_play:
                if (musicPlayer.getState() == PLAYER_PLAY){
                    ivPlay.setImageResource(R.drawable.play);
                    musicPlayer.pause();
                }else {
                    ivPlay.setImageResource(R.drawable.pause);
                    musicPlayer.play();
                }
                break;
            case R.id.img_prev:
                previousMusic();
                break;

            default:
                break;
        }

    }

    private void previousMusic() {
        position--;
        if (position < 0){
            position = paths.size() - 1;
        }
        String path = paths.get(position);
        playMusic(path);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (timeCurrent != progress && timeCurrent != 0)
            musicPlayer.seek(sbProcess.getProgress() * 1000);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onEndMusic() {
        //khi kết thúc bài hát sẽ vào đây
        nextMusic();
        //như vậy khi kết thuc bài hát nó có thể next bài tiếp theo
        //nếu hết danh sách bài hát nó sẽ quay lại từ đầu
    }

    private void nextMusic() {
        if (musicPlayer.getState() == PLAYER_PLAY){

            musicPlayer.stop();

        }
        musicPlayer.stop();
        position++;
        if (position >= paths.size()){
            position =0;
        }
        String path = paths.get(position);
        playMusic(path);
    }
}
