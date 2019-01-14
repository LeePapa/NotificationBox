package cn.gavinliu.notificationbox.baidutts;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cn.gavinliu.notificationbox.baidutts.control.InitConfig;
import cn.gavinliu.notificationbox.baidutts.control.MySyntherizer;
import cn.gavinliu.notificationbox.baidutts.control.NonBlockSyntherizer;
import cn.gavinliu.notificationbox.baidutts.listener.UiMessageListener;
import cn.gavinliu.notificationbox.baidutts.util.AutoCheck;
import cn.gavinliu.notificationbox.baidutts.util.OfflineResource;
import cn.gavinliu.notificationbox.hcicloud.HciCloudProxy;

import cn.gavinliu.notificationbox.utils.SettingUtils;

public class ttsProxy {

    // ================== 初始化参数设置开始 ==========================
    /**
     * 发布时请替换成自己申请的appId appKey 和 secretKey。注意如果需要离线合成功能,请在您申请的应用中填写包名。
     * 本demo的包名是com.baidu.tts.sample，定义在build.gradle中。
     */
    protected String appId = "15309625";

    protected String appKey = "qTmneqPFkGl8deda0T8vLDny";

    protected String secretKey = "MhZMCxDbbuy8Rf5YpDnT92hFIv8PST4Q";


    /**
     * 判断当前应用是否是debug状态
     */

    private boolean isApkInDebug(Context context) {
        try {
            ApplicationInfo info = context.getApplicationInfo();
            boolean debug = ((info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
            if (debug) {
                appId = "15347797";
                appKey = "hxGdw2a7uWcBmViONsgQ3ghD";
                secretKey = "yPxIj90jbghFnyd0FhcjWNH7dYlr0kEh";

            }

            return debug;
        } catch (Exception e) {
            return false;
        }
    }

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    protected TtsMode ttsMode = TtsMode.MIX;

    // 离线发音选择，VOICE_FEMALE即为离线女声发音。
    // assets目录下bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat为离线男声模型；
    // assets目录下bd_etts_common_speech_f7_mand_eng_high_am-mix_v3.0.0_20170512.dat为离线女声模型
    protected String offlineVoice = OfflineResource.VOICE_MALE;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================

    // 主控制类，所有合成控制方法从这个类开始
    protected MySyntherizer synthesizer;

    Handler mainHandler;
    Context context;


    public ttsProxy(Context context) {
        this.context = context;
        isApkInDebug(context);
        initialTts();
    }

    public void release() {
        synthesizer.release();
    }

    public void stop() {
        synthesizer.stop();
    }

    private int speaker = 0;

    private void setSpeaker(boolean next) {
        if (next) speaker++;
        else speaker = 0;

        String offlineVoice = "";
        String onlineVoide = "";

        switch (speaker % 4) {
            case 0:
                offlineVoice = OfflineResource.VOICE_FEMALE;
                onlineVoide = "0";
                break;
            case 1:
                offlineVoice = OfflineResource.VOICE_MALE;
                onlineVoide = "2";
                break;
            case 2:
                offlineVoice = OfflineResource.VOICE_DUYY;
                onlineVoide = "4";
                break;
            case 3:
                offlineVoice = OfflineResource.VOICE_DUXY;
                onlineVoide = "3";
                break;
        }
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        int result = synthesizer.loadModel(offlineResource.getModelFilename(), offlineResource.getTextFilename());
        checkResult(result, "loadModel");

        // 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        synthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, onlineVoide);
    }


    public void read(LinkedList<String[]> list){
        hciCloudProxy.speak(list);
    }


    public void read(String s, boolean new_speaker) {
        if (null == s)
            return;
        if (s.length() < 1)
            return;
/*


        if (SettingUtils.getInstance().readLang2()) {
          hciCloudProxy.speak(s);
            //  hciProxy.speak(s);
            return;
        }
*/

        if (new_speaker)
            setSpeaker(true);
        int result = -1;
        if (s.length() < ClipLength) {
            result = synthesizer.speak(s);
        } else {
            String clip = getClipString(s);

            while (clip.length() > 0) {
                clip = getClipString(clip);
            }

        }


        if (result != 0) {
            Log.i("TTS error", "error code :" + result + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }


    }

    private int ClipLength = 513;

    private String[] getClip(String in) {
        String[] out = new String[]{
                "", ""
        };
        if (null == in) {
            return out;
        }
        if (in.length() < ClipLength) {
            out[0] = in;
        } else {
            String f1 = in.substring(0, ClipLength - 1);
            String f2 = f1.replaceFirst("([。？！~…，]|\\s)[^。？！~…，]*$", "$1");
            out[0] = f2;
            out[1] = in.substring(f2.length());
        }
        return out;
    }

    private String getClipString(String in) {

        if (null == in) {
            return "";
        }
        if (in.length() < ClipLength) {
            synthesizer.speak(in);
        } else {
            String f1 = in.substring(0, ClipLength - 1);
            String f2 = f1.replaceFirst("([。？！~…，]|\\s)[^。？！~…，]*$", "$1");
            synthesizer.speak(f2);
            return in.substring(f2.length());
        }
        return "";
    }


    private void initialTts() {
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handle(msg);
            }

        };
        LoggerProxy.printable(true); // 日志打印在logcat中
        // 设置初始化参数
        // 此处可以改为 含有您业务逻辑的SpeechSynthesizerListener的实现类
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        Map<String, String> params = getParams();


        // appId appKey secretKey 网站上您申请的应用获取。注意使用离线合成功能的话，需要应用中填写您app的包名。包名在build.gradle中获取。
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);

        // 如果您集成中出错，请将下面一段代码放在和demo中相同的位置，并复制InitConfig 和 AutoCheck到您的项目中
        // 上线时请删除AutoCheck的调用
        AutoCheck.getInstance(context).check(initConfig, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        Log.i("TTS error", message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });
        synthesizer = new NonBlockSyntherizer(context, initConfig, mainHandler); // 此处可以改为MySyntherizer 了解调用过程


        // ich-cloud 灵云初始化
        if (SettingUtils.getInstance().readLang2()) {
            hciCloudProxy=new HciCloudProxy(context);
/*            hciProxy =
                    new hciProxy(context);
            hciProxy.speak("hi hi hi 测试灵云播放器，测试灵云播放器");*/
        }

    }


    private HciCloudProxy hciCloudProxy;

    protected void handle(Message msg) {
/*        int what = msg.what;
        switch (what) {
            case PRINT:
                print(msg);
                break;
            case UI_CHANGE_INPUT_TEXT_SELECTION:
                if (msg.arg1 <= mInput.getText().length()) {
                    mInput.setSelection(0, msg.arg1);
                }
                break;
            case UI_CHANGE_SYNTHES_TEXT_SELECTION:
                SpannableString colorfulText = new SpannableString(mInput.getText().toString());
                if (msg.arg1 <= colorfulText.toString().length()) {
                    colorfulText.setSpan(new ForegroundColorSpan(Color.GRAY), 0, msg.arg1, Spannable
                            .SPAN_EXCLUSIVE_EXCLUSIVE);
                    mInput.setText(colorfulText);
                }
                break;
            default:
                break;
        }*/
    }

/*
    protected void toPrint(String str) {
        Message msg = Message.obtain();
        msg.obj = str;
        mainHandler.sendMessage(msg);
    }
*/


    /**
     * 合成的参数，可以初始化时填写，也可以在合成前设置。
     *
     * @return
     */
    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>();
        // 以下参数均为选填
        // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        params.put(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_VOLUME, "15");
        // 设置合成的语速，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        params.put(SpeechSynthesizer.PARAM_PITCH, "5");

        params.put(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
        // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
        // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
        // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

        // 离线资源文件， 从assets目录中复制到临时目录，需要在initTTs方法前完成
        OfflineResource offlineResource = createOfflineResource(offlineVoice);
        // 声学模型文件路径 (离线引擎使用), 请确认下面两个文件存在
        params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, offlineResource.getTextFilename());
        params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE,
                offlineResource.getModelFilename());
        return params;
    }


    protected OfflineResource createOfflineResource(String voiceType) {
        OfflineResource offlineResource = null;
        try {
            offlineResource = new OfflineResource(context, voiceType);
        } catch (IOException e) {
            // IO 错误自行处理
            e.printStackTrace();
            Log.i("TTS error", "【error】:copy files from assets failed." + e.getMessage());
        }
        return offlineResource;
    }


    /**
     * speak 实际上是调用 synthesize后，获取音频流，然后播放。
     * 获取音频流的方式见SaveFileActivity及FileSaveListener
     * 需要合成的文本text的长度不能超过1024个GBK字节。
     * 这是示例的方法，目前已经改用read()了
     * <p>
     * private void speak(String text) {
     * //    String text="消息来了" ;
     * //=  mInput.getText().toString();
     * // 需要合成的文本text的长度不能超过1024个GBK字节。
     * if (null==text) {
     * text = "百度语音，面向广大开发者永久免费开放语音合成技术。";
     * }
     * // 合成前可以修改参数：
     * // Map<String, String> params = getParams();
     * // synthesizer.setParams(params);
     * int result = synthesizer.speak(text);
     * checkResult(result, "speak");
     * }
     */
    private void checkResult(int result, String method) {
        if (result != 0) {
            Log.i("TTS error", "error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

}
