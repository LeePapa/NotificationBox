package cn.gavinliu.notificationbox.msg;

public class TextBook {
    public String group, sender, mainText, ext;
    public long time;

    public TextBook(String group, String sender, String mainText, String ext) {
        time = System.currentTimeMillis();
        this.group = group;
        this.sender = sender;
        this.mainText = mainText;
        if (null != ext)
            this.ext = ext;
        else ext = "";
    }


}
