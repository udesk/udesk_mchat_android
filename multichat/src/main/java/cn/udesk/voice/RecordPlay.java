package cn.udesk.voice;import android.content.Context;import android.media.AudioManager;import android.media.MediaPlayer;import android.media.MediaPlayer.OnCompletionListener;import android.media.MediaPlayer.OnErrorListener;import android.media.MediaPlayer.OnPreparedListener;import android.text.TextUtils;import java.io.IOException;import cn.udesk.UdeskUtil;import cn.udesk.muchat.bean.ReceiveMessage;public class RecordPlay implements RecordFilePlay, OnCompletionListener,		OnPreparedListener, OnErrorListener {	private volatile boolean isPlaying = false;	private Context mContent;	int mPosition;	String mMediaFilePath;	RecordPlayCallback mCallbak;	MediaPlayer mediaPlayer;	ReceiveMessage mCurrentMessage;	public RecordPlay(Context content) {		this.mContent = content;	}	@Override	public synchronized void click(ReceiveMessage message,			RecordPlayCallback callback) {		try {			if (mediaPlayer == null) {                init();            }			if (mCurrentMessage != message) {                // 停止旧文件的播放                if (isPlaying) {                    recycleRes();                }                // 对旧数据 进行回调                if (mCurrentMessage != null) {                    mCurrentMessage.isPlaying = false;                    if (mCallbak != null) {                        mCallbak.onPlayEnd(mCurrentMessage);                        mCurrentMessage = null;                    }                }                // 新旧数据更新                mCallbak = callback;                // 新文件的播放                String mediaFilePath = "";                if(!TextUtils.isEmpty(message.getLocalPath())){                    mediaFilePath = message.getLocalPath();                }else if(UdeskUtil.audiofileIsDown(mContent,UdeskUtil.objectToString(message.getContent()))){                    mediaFilePath = UdeskUtil.getDownAudioPath(mContent,UdeskUtil.objectToString(message.getContent()));                }else{                    mediaFilePath =  UdeskUtil.objectToString(message.getContent());                }                mCurrentMessage = message;                try {                    init();                    startPlayer(mediaFilePath);                } catch (Exception e) {                    e.printStackTrace();                }                mMediaFilePath = mediaFilePath;            } else {                toggle();            }		} catch (Exception e) {			e.printStackTrace();		}	}	private void startPlayer(final String mediaFilePath) throws IOException {		try {			mPosition = 0;			mediaPlayer.reset();			mediaPlayer.setDataSource(mediaFilePath);			mediaPlayer.setLooping(false);			mediaPlayer.prepareAsync();// player只有调用了onpraparre（）方法后才会调用onstart（）		} catch (IOException e) {			e.printStackTrace();		} catch (IllegalArgumentException e) {			e.printStackTrace();		} catch (SecurityException e) {			e.printStackTrace();		} catch (IllegalStateException e) {			e.printStackTrace();		}	}	public String getMediaPath() {		return mMediaFilePath;	}	@Override	public void recycleRes() {		if (mediaPlayer != null) {			if (isPlaying) {				try {					mediaPlayer.stop();				} catch (Exception e) {					e.printStackTrace();				}			}			try {				mediaPlayer.release();			} catch (Exception e) {				e.printStackTrace();			}		}		isPlaying = false;		if (mCurrentMessage != null) {			mCurrentMessage.isPlaying = false;		}	}	public void recycleCallback() {		try{			recycleRes();			mCurrentMessage = null;			mCallbak = null;		}catch (Exception e){			e.printStackTrace();		}	}	@Override	public void toggle() {		if (isPlaying) {			isPlaying = false;			try {				mPosition = mediaPlayer.getCurrentPosition();			} catch (Exception e) {				e.printStackTrace();			}			try {				mediaPlayer.pause();			} catch (Exception e) {				e.printStackTrace();			}			if (mCallbak != null) {				mCallbak.onPlayPause(mCurrentMessage);				mCallbak = null;			}			if (mCurrentMessage != null) {				mCurrentMessage.isPlaying = false;			}		} else {			init();			try {				startPlayer(mMediaFilePath);			} catch (Exception e) {				e.printStackTrace();			}		}	}	@Override	public void onCompletion(MediaPlayer mp) {		mPosition = 0;		try {			mp.stop();			isPlaying = false;			if (mCurrentMessage != null) {				mCurrentMessage.isPlaying = false;			}			if (mCallbak != null) {				mCallbak.endAnimation();				mCallbak.onPlayEnd(mCurrentMessage);				mCurrentMessage = null;				mCallbak = null;			}		} catch (Exception e) {			e.printStackTrace();		}	}	@Override	public void onPrepared(MediaPlayer arg0) {		try {			mediaPlayer.start();			isPlaying = true;			if (mCallbak != null) {				mCallbak.onPlayStart(mCurrentMessage);			}			if (mCurrentMessage != null) {				mCurrentMessage.isPlaying = true;			}		} catch (IllegalStateException e) {			e.printStackTrace();		}	}	private void init() {		mediaPlayer = new MediaPlayer();		mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);		mediaPlayer.setOnCompletionListener(this);		mediaPlayer.setOnPreparedListener(this);		mediaPlayer.setOnErrorListener(this);	}	@Override	public boolean onError(MediaPlayer arg0, int arg1, int arg2) {		arg0.reset();		isPlaying = false;		if (mCurrentMessage != null) {			mCurrentMessage.isPlaying = false;		}		return true;	}	@Override	public ReceiveMessage getPlayAduioMessage() {		if(mCurrentMessage != null){			return mCurrentMessage;		}		return null;	}}