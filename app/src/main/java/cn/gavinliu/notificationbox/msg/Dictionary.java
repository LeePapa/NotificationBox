package cn.gavinliu.notificationbox.msg;

import android.util.Log;

import java.util.ArrayList;

import static java.util.Arrays.asList;

public class Dictionary {



   static public String HciLanguageString(int lang){
      switch (lang){
         case 1:
            // 日语
            return "tts.cloud.kyoko";
         case 2:
            // 德语
            return "tts.cloud.anna";
         case 3:
            // 法语
            return  "tts.cloud.audrey-ml";
         case 4:
            // 韩语
            return "tts.cloud.narae";

         default:
            return "tts.cloud.haobo";

      }



   }

   static public ArrayList<String> dic_msg_tag=new ArrayList<String>(asList(
           "[图片]","[表情图片]","[魔法表情]","[表情]"
   ));

   static public String  num2words(String input,int mode){
      // 从文本数字混合转换为汉字。通过这种方式纠正数字读音，避免ED2 读为ED two 支持前后缀，但是如果出现两段数字，无法正确解析

      String number="";
      switch (mode){
         case 0:
             number =input.replaceAll("[^0-9]*","");
             break;
         case 1:
            number=input.replaceFirst("^.*[^0-9]([0-9])$","$1");
            break;
         case -1:
            number=input.replaceFirst("^([0-9])[^0-9]$","$1");
            break;
      }

      String words="";
      switch (number){
         case "0":
            words="零";
            break;
         case "1":
            words="一";
            break;
         case "2":
            words="二";
            break;
         case "3":
            words="三";
            break;
         case "4":
            words="四";
            break;
         case "5":
            words="五";
            break;
         case "6":
            words="六";
            break;
         case "7":
            words="七";
            break;
         case "8":
            words="八";
            break;
         case "9":
            words="九";
            break;
         default:
            Log.w("TextTool_num2words","oped error -"+number+"-"+words);

      }

      return input.replace(number,words);

   }

   static public ArrayList<String> dic_char_german=new ArrayList<>(asList(
           "Ä","ä","Ö","ö","Ü","ü","ß"
   ));

   static public ArrayList<String> dic_char_farnce=new ArrayList<>(asList(
           //闭音符（áéó）和软音符（ç）：先按’（单引号），再按字母
           "á","é","ó"
           ,"ç"
           //开音符（àèò）：先按`（数字1旁边那个）然后按字母
           ,"à","è","ò"
           //长音符（âêô）：这个麻烦一点，shift+6（也就是^），然后按字母
           ,"â","ê","ô"
           //分音符（ö）：shift+’（也就是双引号），然后按字母
           ,"ö"
           , "ê","é","è","ù"
   ));

/*
   通常用下面这段javascript正则表达式即可验证是否韩文:　　
           /^([\uAC00-\uD7AF])*$/gi　　
*/

   static public ArrayList<String> dic_char_japanese=new ArrayList<>(asList(
           "ァ",
           "ア",
           "あ",
           "ィ",
           "イ",
           "い",
           "ゥ",
           "ウ",
           "う",
           "ェ",
           "エ",
           "え",
           "ォ",
           "オ",
           "お",
           "カ",
           "か",
           "ガ",
           "が",
           "キ",
           "き",
           "ギ",
           "ぎ",
           "ク",
           "く",
           "グ",
           "ぐ",
           "ケ",
           "け",
           "ゲ",
           "げ",
           "コ",
           "こ",
           "ゴ",
           "ご",
           "サ",
           "さ",
           "ザ",
           "ざ",
           "シ",
           "し",
           "ジ",
           "じ",
           "ス",
           "す",
           "ズ",
           "ず",
           "セ",
           "せ",
           "ゼ",
           "ぜ",
           "ソ",
           "そ",
           "ゾ",
           "ぞ",
           "タ",
           "た",
           "ダ",
           "だ",
           "チ",
           "ち",
           "ヂ",
           "ぢ",
           "ッ",
           "ツ",
           "つ",
           "ヅ",
           "づ",
           "テ",
           "て",
           "デ",
           "で",
           "ト",
           "と",
           "ド",
           "ど",
           "ナ",
           "な",
           "ニ",
           "に",
           "ヌ",
           "ぬ",
           "ネ",
           "ね",
           "ノ",
           "の",
           "ハ",
           "は",
           "バ",
           "ば",
           "パ",
           "ぱ",
           "ヒ",
           "ひ",
           "ビ",
           "び",
           "ピ",
           "ぴ",
           "フ",
           "ふ",
           "ブ",
           "ぶ",
           "プ",
           "ぷ",
           "ヘ",
           "へ",
           "ベ",
           "べ",
           "ペ",
           "ぺ",
           "ホ",
           "ほ",
           "ボ",
           "ぼ",
           "ポ",
           "ぽ",
           "マ",
           "ま",
           "ミ",
           "み",
           "ム",
           "む",
           "メ",
           "め",
           "モ",
           "も",
           "ャ",
           "ゃ",
           "ヤ",
           "や",
           "ュ",
           "ゅ",
           "ユ",
           "ゆ",
           "ョ",
           "ょ",
           "ヨ",
           "よ",
           "ラ",
           "ら",
           "リ",
           "り",
           "ル",
           "る",
           "レ",
           "れ",
           "ロ",
           "ろ",
           "ワ",
           "わ",
           "ヲ",
           "を",
           "ン",
           "ん",
           "ー"
   ));




   static public ArrayList<String> dic_pinyin= new ArrayList<String>(asList(

           "a"
           ,"ai"
           ,"an"
           ,"ang"
           ,"ao"
           ,"ba"
           ,"bai"
           ,"ban"
           ,"bang"
           ,"bao"
           ,"bei"
           ,"ben"
           ,"beng"
           ,"bi"
           ,"bian"
           ,"biao"
           ,"bie"
           ,"bin"
           ,"bing"
           ,"bo"
           ,"bu"
           ,"ca"
           ,"cai"
           ,"can"
           ,"cang"
           ,"cao"
           ,"ce"
           ,"cen"
           ,"ceng"
           ,"cha"
           ,"chai"
           ,"chan"
           ,"chang"
           ,"chao"
           ,"che"
           ,"chen"
           ,"cheng"
           ,"chi"
           ,"chong"
           ,"chou"
           ,"chu"
           ,"chua"
           ,"chuai"
           ,"chuan"
           ,"chuang"
           ,"chui"
           ,"chun"
           ,"chuo"
           ,"ci"
           ,"cong"
           ,"cou"
           ,"cu"
           ,"cuan"
           ,"cui"
           ,"cun"
           ,"cuo"
           ,"da"
           ,"dai"
           ,"dan"
           ,"dang"
           ,"dao"
           ,"de"
           ,"den"
           ,"dei"
           ,"deng"
           ,"di"
           ,"dia"
           ,"dian"
           ,"diao"
           ,"die"
           ,"ding"
           ,"diu"
           ,"dong"
           ,"dou"
           ,"du"
           ,"duan"
           ,"dui"
           ,"dun"
           ,"duo"
           ,"e"
           ,"ei"
           ,"en"
           ,"eng"
           ,"er"
           ,"fa"
           ,"fan"
           ,"fang"
           ,"fei"
           ,"fen"
           ,"feng"
           ,"fo"
           ,"fou"
           ,"fu"
           ,"ga"
           ,"gai"
           ,"gan"
           ,"gang"
           ,"gao"
           ,"ge"
           ,"gei"
           ,"gen"
           ,"geng"
           ,"gong"
           ,"gou"
           ,"gu"
           ,"gua"
           ,"guai"
           ,"guan"
           ,"guang"
           ,"gui"
           ,"gun"
           ,"guo"
           ,"ha"
           ,"hai"
           ,"han"
           ,"hang"
           ,"hao"
           ,"he"
           ,"hei"
           ,"hen"
           ,"heng"
           ,"hong"
           ,"hou"
           ,"hu"
           ,"hua"
           ,"huai"
           ,"huan"
           ,"huang"
           ,"hui"
           ,"hun"
           ,"huo"
           ,"ji"
           ,"jia"
           ,"jian"
           ,"jiang"
           ,"jiao"
           ,"jie"
           ,"jin"
           ,"jing"
           ,"jiong"
           ,"jiu"
           ,"ju"
           ,"juan"
           ,"jue"
           ,"jun"
           ,"ka"
           ,"kai"
           ,"kan"
           ,"kang"
           ,"kao"
           ,"ke"
           ,"ken"
           ,"keng"
           ,"kong"
           ,"kou"
           ,"ku"
           ,"kua"
           ,"kuai"
           ,"kuan"
           ,"kuang"
           ,"kui"
           ,"kun"
           ,"kuo"
           ,"la"
           ,"lai"
           ,"lan"
           ,"lang"
           ,"lao"
           ,"le"
           ,"lei"
           ,"leng"
           ,"li"
           ,"lia"
           ,"lian"
           ,"liang"
           ,"liao"
           ,"lie"
           ,"lin"
           ,"ling"
           ,"liu"
           ,"long"
           ,"lou"
           ,"lu"
           ,"lü"
           ,"luan"
           ,"lue"
           ,"lüe"
           ,"lun"
           ,"luo"
           ,"m"
           ,"ma"
           ,"mai"
           ,"man"
           ,"mang"
           ,"mao"
           ,"me"
           ,"mei"
           ,"men"
           ,"meng"
           ,"mi"
           ,"mian"
           ,"miao"
           ,"mie"
           ,"min"
           ,"ming"
           ,"miu"
           ,"mo"
           ,"mou"
           ,"mu"
           ,"na"
           ,"nai"
           ,"nan"
           ,"nang"
           ,"nao"
           ,"ne"
           ,"nei"
           ,"nen"
           ,"neng"
           ,"ng"
           ,"ni"
           ,"nian"
           ,"niang"
           ,"niao"
           ,"nie"
           ,"nin"
           ,"ning"
           ,"niu"
           ,"nong"
           ,"nou"
           ,"nu"
           ,"nü"
           ,"nuan"
           ,"nüe"
           ,"nuo"
           ,"nun"
           ,"o"
           ,"ou"
           ,"pa"
           ,"pai"
           ,"pan"
           ,"pang"
           ,"pao"
           ,"pei"
           ,"pen"
           ,"peng"
           ,"pi"
           ,"pian"
           ,"piao"
           ,"pie"
           ,"pin"
           ,"ping"
           ,"po"
           ,"pou"
           ,"pu"
           ,"qi"
           ,"qia"
           ,"qian"
           ,"qiang"
           ,"qiao"
           ,"qie"
           ,"qin"
           ,"qing"
           ,"qiong"
           ,"qiu"
           ,"qu"
           ,"quan"
           ,"que"
           ,"qun"
           ,"ran"
           ,"rang"
           ,"rao"
           ,"re"
           ,"ren"
           ,"reng"
           ,"ri"
           ,"rong"
           ,"rou"
           ,"ru"
           ,"ruan"
           ,"rui"
           ,"run"
           ,"ruo"
           ,"sa"
           ,"sai"
           ,"san"
           ,"sang"
           ,"sao"
           ,"se"
           ,"sen"
           ,"seng"
           ,"sha"
           ,"shai"
           ,"shan"
           ,"shang"
           ,"shao"
           ,"she"
           ,"shei"
           ,"shen"
           ,"sheng"
           ,"shi"
           ,"shou"
           ,"shu"
           ,"shua"
           ,"shuai"
           ,"shuan"
           ,"shuang"
           ,"shui"
           ,"shun"
           ,"shuo"
           ,"si"
           ,"song"
           ,"sou"
           ,"su"
           ,"suan"
           ,"sui"
           ,"sun"
           ,"suo"
           ,"ta"
           ,"tai"
           ,"tan"
           ,"tang"
           ,"tao"
           ,"te"
           ,"teng"
           ,"ti"
           ,"tian"
           ,"tiao"
           ,"tie"
           ,"ting"
           ,"tong"
           ,"tou"
           ,"tu"
           ,"tuan"
           ,"tui"
           ,"tun"
           ,"tuo"
           ,"wa"
           ,"wai"
           ,"wan"
           ,"wang"
           ,"wei"
           ,"wen"
           ,"weng"
           ,"wo"
           ,"wu"
           ,"xi"
           ,"xia"
           ,"xian"
           ,"xiang"
           ,"xiao"
           ,"xie"
           ,"xin"
           ,"xing"
           ,"xiong"
           ,"xiu"
           ,"xu"
           ,"xuan"
           ,"xue"
           ,"xun"
           ,"ya"
           ,"yan"
           ,"yang"
           ,"yao"
           ,"ye"
           ,"yi"
           ,"yin"
           ,"ying"
           ,"yo"
           ,"yong"
           ,"you"
           ,"yu"
           ,"yuan"
           ,"yue"
           ,"yun"
           ,"za"
           ,"zai"
           ,"zan"
           ,"zang"
           ,"zao"
           ,"ze"
           ,"zei"
           ,"zen"
           ,"zeng"
           ,"zha"
           ,"zhai"
           ,"zhan"
           ,"zhang"
           ,"zhao"
           ,"zhe"
           ,"zhei"
           ,"zhen"
           ,"zheng"
           ,"zhi"
           ,"zhong"
           ,"zhou"
           ,"zhu"
           ,"zhua"
           ,"zhuai"
           ,"zhuan"
           ,"zhuang"
           ,"zhui"
           ,"zhun"
           ,"zhuo"
           ,"zi"
           ,"zong"
           ,"zou"
           ,"zu"
           ,"zuan"
           ,"zui"
           ,"zun"
           ,"zuo"
   ));
}
