package com.netease.nim.demo.avchat;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.netease.nim.demo.R;


/**
 * SoundPool 铃声尽量不要超过1M
 * 在不同的系统下 SoundPool 表现可能存在不一致
 */
public class SoundPlayer {

    public enum RingerTypeEnum {
        CONNECTING,
        NO_RESPONSE,
        PEER_BUSY,
        PEER_REJECT,
        RING,
        ;
    }

    private Context context;


    private SoundPool soundPool;
    private AudioManager audioManager;
    private int streamId;
    private int soundId;
    private boolean loop;

    private static SoundPlayer instance = null;

    public static SoundPlayer instance(Context context) {
        if(instance == null) {
            synchronized (SoundPlayer.class) {
                if(instance == null) {
                    instance = new SoundPlayer(context);
                }
            }
        }
        return instance;
    }



    private SoundPlayer(Context context) {
        this.context = context;
    }


    public synchronized void play(RingerTypeEnum type) {

        int ringId = 0;
        switch (type) {
        case NO_RESPONSE:
            ringId = R.raw.avchat_no_response;
            loop = false;
            break;
        case PEER_BUSY:
            ringId = R.raw.avchat_peer_busy;
            loop = false;
            break;
        case PEER_REJECT:
            ringId = R.raw.avchat_peer_reject;
            loop = false;
            break;
        case CONNECTING:
            ringId = R.raw.avchat_connecting;
            loop = true;
            break;
        case RING:
            ringId = R.raw.avchat_ring;
            loop = true;
            break;
        }

        if(ringId != 0) {
            play(ringId);
        }

    }

    public void stop() {
        if (soundPool != null) {
            if (streamId != 0) {
                soundPool.stop(streamId);
                streamId = 0;
            }
            if (soundId != 0) {
                soundPool.unload(soundId);
                soundId = 0;
            }
        }
    }

    private void play(int ringId) {
        initSoundPool();
        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
            soundId = soundPool.load(context, ringId, 1);
        }
    }

    private void initSoundPool() {
        stop();
        if (soundPool == null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_RING, 0);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                    if (soundId != 0 && status == 0) {
                        if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                            int curVolume = audioManager.getStreamVolume(AudioManager.STREAM_RING);
                            streamId = soundPool.play(soundId, curVolume, curVolume, 1, loop ? -1 : 0, 1f);
                        }
                    }
                }
            });

            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
    }
}
