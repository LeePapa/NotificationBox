package cn.gavinliu.notificationbox.msg;


import android.provider.SyncStateContract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.regex.Matcher;

import static cn.gavinliu.notificationbox.msg.Dictionary.HciLanguageString;
import static cn.gavinliu.notificationbox.msg.Dictionary.num2words;

public class musicTextBook {


    private String group = "";
    private String out_text = "";
    private String pkg = "";
    private int count = 0;

    public TextBook getTextBook(TextBook old) {
        if (null != old) {
            if (old.group.equals(group) && old.mainText.equals(out_text)) {
                long timeDiffer = System.currentTimeMillis() - old.time;
                if (timeDiffer < 30000 && timeDiffer > 300) {
                    count = old.count + 1;
                    if (count > 2) count = 0;
                } else {
                    old.count = 0;
                    return old;
                }
            } else count = 1;
        } else count = 1;
        return new TextBook(pkg, out_text, count);
    }

    public String getString() {
        return out_text;
    }

    public boolean notNeedRead(){
        return (count == 0);
    }

    // 是否有中文。
    private boolean has_chs = false;


    // 语种和内容的缓存。如果此次语种与前次相同，压入缓存；否则将缓存压入列表，重新赋值缓存。结束时将缓存压入列表。
    // 额外的，如果内容为空，取消压入动作。
    // 因此无需第二次整理内容
    private int pre_lang = 0;
    private String pre_text = "";


    // 外语的语种（只支持1种外语）
    private int lang_2 = -1;

    // 分解的tag和语言列表
    private LinkedList<String> list_text = new LinkedList<>();
    private LinkedList<Integer> list_lang = new LinkedList<>();


    private LinkedList<String[]> list_MixedText = new LinkedList<>();


    public boolean has_2nd_language() {
        return lang_2 > 0;
    }

    public boolean isHas_chs() {
        return has_chs;
    }

    public LinkedList<String[]> getList_MixedText() {
        return list_MixedText;
    }

    public musicTextBook(String titleStr, String textStr, String pkg) {
        // 先处理固定句式
        String[] song_tag = (titleStr + "," + textStr).replaceAll("([^0-9])-([^0-9])", "$1 $2").split("[\\(\\)【】《》〖〗『』「」（）,~/]");
        ArrayList<String> reviewed=new ArrayList<>();
        for (String tag : song_tag) {
            String s = tag.trim().replaceAll("(\\s|_)+", " ");
            if (s.contains(" ")) {
                if (s.replaceAll("\\s", "").matches("[A-Z]{3,}[a-zA-Z]+")) {
                    //如果全部大写了,那么有可能是错误的拼写,需要转换小写以免误读
                    s = s.toLowerCase();
                }

            }
            if (s.matches("\\s+"))
                continue;

            if(s.equals("TVアニメ"))
                continue;

            if (s.matches(".+\\s(ver.|Ver.|ver|Ver)$")) {
                s = s.substring(0, s.lastIndexOf("er") - 1) + "version";
                //    s+="sion";
            } else if (s.matches(".*(OP|ED|op|ed|Op|Ed|OST)\\d+")) {
                s = num2words(s, 1).toUpperCase();
            }

            if(reviewed.contains(s))
                continue;


            out_text += "," + s;

            {
                int tag_lang = detLanguage(s);
                if (tag_lang == pre_lang || tag_lang == 0) {
                    pre_text += "," + s;
                } else if (pre_lang == 0) {
                    pre_lang = tag_lang;
                    lang_2 = tag_lang;
                    pre_text += "," + s;
                } else {
                    if (!pre_text.matches(",*")) {
                        list_MixedText.add(new String[]{pre_text, HciLanguageString(pre_lang)});
//                        list_lang.add(pre_lang);
//                        list_text.add(pre_text);
                    }
                    pre_text = s;
                    pre_lang = tag_lang;

                    if (tag_lang < 0) {
                        has_chs = true;
                    } else if (tag_lang > 0) {
                        lang_2 = tag_lang;
                    }
                }

            }
        }

        if (!pre_text.matches(",*")) {
            list_MixedText.add(new String[]{pre_text, HciLanguageString(pre_lang)});
//            list_lang.add(pre_lang);
//            list_text.add(pre_text);
        }


        out_text = out_text.replaceAll("\\s*(,|：)\\s", ",");
        if (out_text.matches(",+"))
            out_text = "";


    }


    private int detLanguage(String s) {
        // 空字符按照英语算法处理
        if (s.length() < 1)
            return 0;


        String in = s.replaceAll("[0-9a-zA-Z\u3000-\u303f\\s?\\-\\.,!]", "");
        int q = in.length();

        // 替换为空，石锤英文
        if (q == 0)
            return 0;


        int q_ri = in.replaceAll("[\u3040-\u30ff\u31f0-\u31ff]+", "").length();
        q = Math.min(q_ri, q);
        int q_de = in.replaceAll("[ÄäÖöÜüß]+", "").length();
        q = Math.min(q_de, q);
        int q_fa = in.replaceAll("[áéóàèòâêôöç]+", "").length();
        q = Math.min(q_ri, q);
        int q_chao = in.replaceAll("[\uAC00-\uD7AF]+", "").length();
        q = Math.min(q_chao, q);


        // 非空，又没有被替换掉，认定为汉字，包括日本汉字——但是至少可以直读。
        if (q == in.length())
            return -1;

        // 介于日语存在大量汉字可以塞翻，需要汉字比假名少时，才输出日语
        if (q == q_ri && q*2 < in.length())
            return 1;
        if (q == q_de)
            return 2;
        if (q == q_fa)
            return 3;
        if (q == q_chao)
            return 4;


        return -1;
    }
}
