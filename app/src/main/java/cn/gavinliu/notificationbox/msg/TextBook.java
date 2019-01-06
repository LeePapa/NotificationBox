package cn.gavinliu.notificationbox.msg;

public class TextBook {
    public String group="", sender="", mainText="", ext="";
    public long time;
    public int count=0;

    public TextBook(String group, String sender, String mainText, String ext) {
        time = System.currentTimeMillis();
        this.group = group;
        this.sender = sender;
        this.mainText = mainText;
        if (null != ext)
            this.ext = ext;
    }
    public TextBook(String group,String mainText, int count) {
        time = System.currentTimeMillis();
        this.group = group;
        this.mainText = mainText;
        this.count=count;
    }

}
