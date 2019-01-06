package cn.gavinliu.notificationbox.msg;


import static cn.gavinliu.notificationbox.msg.Dictionary.num2words;

public class musicTextBook {


    private String group = "";
    private String out_text = "";
    private String pkg="";
    private int count=0;
    public TextBook getTextBook(TextBook old) {
        if(null!=old){
            if(old.group.equals(group) && old.mainText.equals(out_text)){
                long timeDiffer=System.currentTimeMillis()-old.time;
                if(timeDiffer<30000 && timeDiffer>300){
                    count=old.count+1;
                    if(count>2) count=0;
                }else{
                    old.count=0;
                    return old;
                }
            }else count=1;
        } else count=1;
        return new TextBook(pkg, out_text, count);
    }

    public String getString() {
        if(count==0)
            return "";
        return out_text;
    }


    public musicTextBook(String titleStr, String textStr, String pkg) {
        // 先处理固定句式
        String[] song_tag=(titleStr+","+textStr).replaceAll("([^0-9])-([^0-9])","$1 $2").split("[\\(\\)（）,~]");

        for(String tag:song_tag){
            String s=tag.trim().replaceAll("(\\s|_)+"," ");
            if(s.contains(" "))
            {
                if(s.replaceAll("\\s","").matches("[A-Z]{3,}[a-zA-Z]+")){
                    //如果全部大写了,那么有可能是错误的拼写,需要转换小写以免误读
                   s=s.toLowerCase();
                }

            }
            if(s.matches(".+\\s(ver.|Ver.|ver|Ver)$")){
                s+="sion";
            }else if(s.matches(".*(OP|ED)\\d+")){
                s=num2words(s,1);
            }

            if(s.replace(" ","").length()>0)
                out_text+=","+s;
        }

        out_text=out_text.replaceAll("\\s*(,|：)\\s",",");
        if(out_text.matches(",+"))
            out_text= "";


    }
}
