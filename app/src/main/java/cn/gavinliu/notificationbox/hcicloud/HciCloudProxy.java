package cn.gavinliu.notificationbox.hcicloud;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.sinovoice.hcicloudsdk.android.tts.player.TTSPlayer;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig;
import com.sinovoice.hcicloudsdk.common.tts.TtsInitParam;
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer;
import com.sinovoice.hcicloudsdk.player.TTSPlayerListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class HciCloudProxy {
    public static final String TAG = "HciCloudTTSPlayer";

    private TtsConfig ttsConfig = null;
    private TTSPlayer mTtsPlayer = null;
    Context context;


    public HciCloudProxy(Context context) {
        this.context = context;
        InitParam initParam = getInitParam();
        String strConfig = initParam.getStringConfig();
        Log.i(TAG, "\nhciInit config:" + strConfig);

        // 初始化
        int errCode = HciCloudSys.hciInit(strConfig, context);
        if (errCode != HciErrorCode.HCI_ERR_NONE && errCode != HciErrorCode.HCI_ERR_SYS_ALREADY_INIT) {
            Log.w("hcicloud", "hciInit error: " + HciCloudSys.hciGetErrorInfo(errCode));
            return;
        }

        // 获取授权/更新授权文件 :
        errCode = checkAuthAndUpdateAuth();
        if (errCode != HciErrorCode.HCI_ERR_NONE) {
            // 由于系统已经初始化成功,在结束前需要调用方法hciRelease()进行系统的反初始化
            Log.w("hcicloud", "CheckAuthAndUpdateAuth error: " + HciCloudSys.hciGetErrorInfo(errCode));
            HciCloudSys.hciRelease();
            return;
        }


        //传入了capKey初始化TTS播发器
        boolean isPlayerInitSuccess = initPlayer();
        if (!isPlayerInitSuccess) {
            Log.w("hcicloud", "播放器初始化失败");
            return;
        }


    }


    public void release() {

        if (mTtsPlayer != null) {
            mTtsPlayer.release();
        }
        HciCloudSys.hciRelease();
    }

    private long time_out_auth;

    public void speak(String s) {
        try {
            synth(s);

        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }

    }


    /**
     * 初始化播放器
     */
    private boolean initPlayer() {
        // 读取用户的调用的能力
        String capKey = "tts.cloud.kyoko";

        // 构造Tts初始化的帮助类的实例
        TtsInitParam ttsInitParam = new TtsInitParam();
        String dataPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "sinovoice"
                + File.separator + context.getPackageName() + File.separator + "data";
        HciCloudHelper.copyAssetsFiles(context, dataPath);
        ttsInitParam.addParam(TtsInitParam.PARAM_KEY_DATA_PATH, dataPath);

        // 获取App应用中的lib的路径
        //String dataPath = getBaseContext().getFilesDir().getAbsolutePath().replace("files", "lib");
        // 使用lib下的资源文件,需要添加android_so的标记
        //ttsInitParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, "android_so");

        // 此处演示初始化的能力为tts.cloud.xiaokun, 用户可以根据自己可用的能力进行设置, 另外,此处可以传入多个能力值,并用;隔开
        ttsInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS, capKey);


        mTtsPlayer = new TTSPlayer();

        // 配置TTS初始化参数
        ttsConfig = new TtsConfig();
        mTtsPlayer.init(ttsInitParam.getStringConfig(), new HciCloudProxy.TTSEventProcess());
        mTtsPlayer.setContext(context);
        if (mTtsPlayer.getPlayerState() == TTSPlayer.PLAYER_STATE_IDLE) {
            return true;
        } else {
            return false;
        }
    }

    private LinkedList<String[]> list_hciBook=new LinkedList<>();
    private String[] hciBook;

    // 云端合成,不启用编码传输(默认encode=none)
    public void speak(String text, String capKey) {
        // 读取用户的调用的能力
        if (null == text)
            return;
        if (null == capKey)
            capKey = "tts.cloud.diaoxiong";


        if (list_hciBook.size() < 1 && null == hciBook) {
            hciBook = (new String[]{
                    text, capKey
            });
            synth();
        } else {
            list_hciBook.add(new String[]{
                    text, capKey
            });
        }

    }


    public void stop(){
        list_hciBook.clear();
        hciBook=null;
        mTtsPlayer.stop();
    }


    public void speak(LinkedList<String[]> list){
        list_hciBook.addAll(list);

        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
            synth();
            Log.i("hciCloud speak", "added new list");
        }

    }


    private void synth() {

        while (null==hciBook && list_hciBook.size()>0){
            hciBook = list_hciBook.remove();
        }

        if (null == hciBook)
            return;

        String text, capKey;
        text = hciBook[0];
        capKey = hciBook[1];

        hciBook=null;

        try {
            // 配置播放器的属性。包括：音频格式，音库文件，语音风格，语速等等。详情见文档。
            ttsConfig = new TtsConfig();
            // 音频格式
            ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit");
            // 指定语音合成的能力(云端合成,发言人是XiaoKun)
            ttsConfig.addParam(TtsConfig.SessionConfig.PARAM_KEY_CAP_KEY, capKey);
            // 设置合成语速
            ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_SPEED, "5");
            // property为私有云能力必选参数，公有云传此参数无效
            ttsConfig.addParam("property", "cn_xiaokun_common");

            ttsConfig.addParam("volume", "9.99");

            if(!capKey.equals("tts.cloud.haobo"))
             ttsConfig.addParam("gainfactor","4");

            if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PLAYING
                    || mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PAUSE) {
                mTtsPlayer.stop();
            }


            if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
                mTtsPlayer.play(text, ttsConfig.getStringConfig());
             //   mTtsPlayer.play(text, ttsConfig.getStringConfig(), 5);
                Log.i("hciCloud speak", text);
            } else {
                Log.w("Hcicloud", "播放器内部状态错误");
            }
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
        }


    }


    // 云端合成,不启用编码传输(默认encode=none)
    private void synth(String text) {
        // 读取用户的调用的能力
        String capKey = "tts.cloud.diaoxiong";

        // 配置播放器的属性。包括：音频格式，音库文件，语音风格，语速等等。详情见文档。
        ttsConfig = new TtsConfig();
        // 音频格式
        ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit");
        // 指定语音合成的能力(云端合成,发言人是XiaoKun)
        ttsConfig.addParam(TtsConfig.SessionConfig.PARAM_KEY_CAP_KEY, capKey);
        // 设置合成语速
        ttsConfig.addParam(TtsConfig.BasicConfig.PARAM_KEY_SPEED, "5");
        // property为私有云能力必选参数，公有云传此参数无效
        ttsConfig.addParam("property", "cn_xiaokun_common");

        ttsConfig.addParam("volume", "9.99");

        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PLAYING
                || mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PAUSE) {
            mTtsPlayer.stop();
        }


        if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
            mTtsPlayer.play(text, ttsConfig.getStringConfig(), 5);
            Log.i("hciCloud speak", text);
        } else {
            Log.w("Hcicloud", "播放器内部状态错误");
        }
    }


    // 播放器回调
    private class TTSEventProcess implements TTSPlayerListener {

        @Override
        public void onPlayerEventPlayerError(TTSCommonPlayer.PlayerEvent playerEvent,
                                             int errorCode) {
            Log.i(TAG, "onError " + playerEvent.name() + " code: " + errorCode);
        }

        @Override
        public void onPlayerEventProgressChange(TTSCommonPlayer.PlayerEvent playerEvent,
                                                int start, int end) {
            Log.i(TAG, "onProcessChange " + playerEvent.name() + " from "
                    + start + " to " + end);
        }

        @Override
        public void onPlayerEventStateChange(TTSCommonPlayer.PlayerEvent playerEvent) {
            Log.i(TAG, "onStateChange " + playerEvent.name() + " - playerState:" + mTtsPlayer.getPlayerState());

            if (mTtsPlayer.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
                // 发送消息读取下一条朗读的内容
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        }

    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    synth();
                    break;
            }
        }
    };





    /**
     * 获取授权
     *
     * @return true 成功
     */
    private int checkAuthAndUpdateAuth() {

        // 获取系统授权到期时间
        int initResult;
        AuthExpireTime objExpireTime = new AuthExpireTime();
        initResult = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            // 显示授权日期,如用户不需要关注该值,此处代码可忽略
            Date date = new Date(objExpireTime.getExpireTime() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
                    Locale.CHINA);
            Log.i(TAG, "expire time: " + sdf.format(date));

            if (objExpireTime.getExpireTime() * 1000 > System
                    .currentTimeMillis()) {
                time_out_auth = objExpireTime.getExpireTime() * 1000;
                // 已经成功获取了授权,并且距离授权到期有充足的时间(>7天)
                Log.i(TAG, "checkAuth success");
                return initResult;
            }

        }

        // 获取过期时间失败或者已经过期
        initResult = HciCloudSys.hciCheckAuth();
        if (initResult == HciErrorCode.HCI_ERR_NONE) {
            Log.i(TAG, "checkAuth success");
            return initResult;
        } else {
            Log.e(TAG, "checkAuth failed: " + initResult);
            return initResult;
        }
    }

    /**
     * 加载初始化信息
     * <p>
     * 上下文语境
     *
     * @return 系统初始化参数
     */
    private InitParam getInitParam() {
        String authDirPath = context.getFilesDir().getAbsolutePath();

        // 前置条件：无
        InitParam initparam = new InitParam();

        // 授权文件所在路径，此项必填
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTH_PATH, authDirPath);

        // 是否自动访问云授权,详见 获取授权/更新授权文件处注释
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no");

        // 灵云云服务的接口地址，此项必填
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_CLOUD_URL, "api.hcicloud.com:8888");

        // 开发者Key，此项必填，由捷通华声提供
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_DEVELOPER_KEY, "3a826e6b54c422e8259dc9446d181aff");

        // 应用Key，此项必填，由捷通华声提供
        initparam.addParam(InitParam.AuthParam.PARAM_KEY_APP_KEY, "e45d54ab");

        // 配置日志参数
        String sdcardState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
            String sdPath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
            String packageName = context.getPackageName();

            String logPath = sdPath + File.separator + "sinovoice"
                    + File.separator + packageName + File.separator + "log"
                    + File.separator;

            // 日志文件地址
            File fileDir = new File(logPath);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }

            // 日志的路径，可选，如果不传或者为空则不生成日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_PATH, logPath);

            // 日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_COUNT, "5");

            // 日志大小，默认一个日志文件写多大，单位为K
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_FILE_SIZE, "1024");

            // 日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
            initparam.addParam(InitParam.LogParam.PARAM_KEY_LOG_LEVEL, "2");
        }

        return initparam;
    }


}
