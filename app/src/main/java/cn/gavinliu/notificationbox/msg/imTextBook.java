package cn.gavinliu.notificationbox.msg;

import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class imTextBook {
    // 把文本进行切片，优先级最高的是行首的部分，使用输入的参数进行一次切分。
    // 然后对可以识别的特殊标签比如[图片][表情]，
    // 接下来是不同等级分层次的切片。切片之后再对内容修正和解析。
    // 比如拼音需要修正为x(yin1),以免读音异常（我认为只有聊天类型需要做这种修正。特别是群名和发言）
    // 强切片需要对称的一对括号。普通切片是不成对的标点符号。弱切片是空格。
    // 强切片代表了不同的层次，需要断开来解析


    // 如果group sender都有内容，读 group的sender说outtext
    // 如果只有group   读 group发来消息
    // 如果只有sender  读 sender说

    private String group = "";
    private String sender = "";
    private String out_text = "";
    private boolean no_conn_word = false;

    private String _group = "", _sender = "", _out_text = "";

    public TextBook getTextBook() {
        return new TextBook(group, sender, out_text, null);
    }


//"([A-Za-z][0-9][0-9a-zA-Z]*|[0-9a-z]+[A-Z][0-9a-zA-Z]*)"


    private Pattern patternVarify = Pattern.compile("(^|[^0-9a-zA-Z/\\\\])([A-Za-z][0-9]|[0-9a-z]+[A-Z])([0-9a-zA-Z]*)");


    // 是否需要切换发音人，并减少旁白
    public boolean isDifferentSpeaker(TextBook old) {

        // 排除轮空
        if (out_text.length() < 1 || null == old)
            return false;

        if (sender.length() > 0 && old.sender.equals(sender)) {
            //同一个人连续发言，不需切换音源，不需再说是谁在发言。
            _out_text = out_text;
            return false;
        }

        if (old.mainText.equals(out_text)) {
            // 复读模式
            _out_text = out_text;
            return true;
        }

        if (group.equals(old.group)) {
            // 同群发言，不需在将群名
            _sender = sender;
            _out_text = out_text;
            return true;
        }

        _group = group;
        _sender = sender;
        _out_text = out_text;
        return true;


    }


    public String getString() {
        if (_out_text.length() > 0) {
            if (no_conn_word) {
                if (_group.length() > 0 && _sender.length() > 0)
                    return _group + "的" + _sender + " " + _out_text;
                if (_group.length() > 0) return _group + " " + _out_text;
                if (_sender.length() > 0) return _sender + " " + _out_text;
            } else {
                if (_group.length() > 0 && _sender.length() > 0)
                    return _group + "的" + _sender + "说(shuo1)，" + _out_text;
                if (_group.length() > 0) return _group + "发来消息，" + _out_text;
                if (_sender.length() > 0) return _sender + "说(shuo1)，" + _out_text;

            }
            return _out_text;
        }
        return "";
    }

/*
    int count_img=0;
    int count_emo=0;

    Pattern pattern_img= Pattern.compile("\\[.{0,2}(图片|相片|照片)\\]");
    Pattern pattern_emo= Pattern.compile("\\[.{0,2}表情\\]");

    Matcher matcher = pattern.matcher(result);

  if (matcher.find()) {
        return matcher.group(1);
    }
*/

    public imTextBook(String titleStr, String textStr, String neckStr) {
        // 先处理固定句式

        if ("QQ".equals(titleStr)) {
            Log.w("detect qq","match");
            if (textStr.contains("QQ正在后台运行")) return;
            if (textStr.matches("有\\s*\\d+\\s*个联系人给你发过来\\d+条新消息")) {
                out_text = textStr.replaceFirst("有(\\s*)(\\d+)(\\s*)个联系人给你发过来(\\d+)条新消息", "q-q:收到$2个联系人的$4条消息");
                Log.w("detect qq","match2");
                return;
            }
        }else{
            Log.w("not detect qq","-"+titleStr+"-");
        }

        String[] in_text = textStr.split(neckStr, 2);
        if (in_text.length == 2 ) {
            if(in_text[0].length()<24 && !in_text[0].contains("\n")){
                //长度不能过长
                sender = in_text[0];
                // 如果存在联系人，那么文本显然不应该包含联系人了
                textStr = in_text[1];
            }
        }

        group = titleStr;

        String[] member = titleStr.split("、");
        for (String s : member) {
            if (s.length() > 0 && sender.equals(s)) {
                group = "";
                break;
            }
        }

        if(sender.length()<1 && group.length()>0){
            sender=group;
            group="";
        }

        // 我认为联系人如果是纯数字，读出来是没有意义的
        if (sender.matches("\\d{6,20}"))
            sender = "";



        String i2_text = textStr.replaceAll("\\[.{0,4}\\]", " ");
        if (i2_text.replaceAll("\\s", "").length() < 1) {
            // 没有有效文字，所以需要对表情、图片计数并播报
            no_conn_word = true;
            if (textStr.matches(".*\\[闪照].*")) {
                out_text = "发了闪照";
                return;
            }
            if (textStr.matches(".*\\[.{0,2}(图片|相片|照片)\\].*")) {
                out_text = "发了图片";
                return;
            }
            if (textStr.matches(".*\\[.{0,2}表情\\].*")) {
                out_text = "发了表情";
                return;
            }
            Log.e("imTextBook", "[.{0-4}] match textStr,but no speach. input-" + textStr);
        } else {
            // 其他括号中的内容也不在播报了。
            out_text = i2_text.replaceAll("(链接|链接地址|地址)(:|：)?\\s*((ftp|http|https)?://)?\\S+\\.\\S+[0-9A-Za-z:/\\[-\\]_#\\?=\\.&]*", "链接");

            Matcher m = patternVarify.matcher(out_text);


            while (m.find()) {
                String code = m.group().replaceFirst("[^0-9a-zA-Z]", "");
                if (code.length() > 4 && code.length() < 10)
                    out_text = out_text.replace(code, code.replaceAll("([0-9a-zA-Z])", "$1 "));
                //  System.out.println(m.group());
            }


        }


    }
}
