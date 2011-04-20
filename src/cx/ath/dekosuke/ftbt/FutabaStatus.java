package cx.ath.dekosuke.ftbt;

//One thread Activity for Futaba

//import android.graphics.*;

public class FutabaStatus {
    private String userName;
    private String title;
    private String text;
    private String mailTo;
    private int id;
    private String imgURL;
    //投稿時刻private date
    //画像private  

    //コンストラクタ
    FutabaStatus(){
        userName="";
        title="";
        text="";
        mailTo="";
        id=0;
    }
   
    //それぞれのgetter/setter
    public String getUserName(){ return userName; }
    public void setUserName(String str){ userName=str; }
    public String getTitle(){ return title; }
    public void setTitle(String str){ title=str; }
    public String getText(){ return text; }
    public void setText(String str){ text=str; }
    public String getMailTo(){ return mailTo; }
    public void setMailTo(String str){ mailTo=str; }
    public int getId(){ return id; }
    public void setId(int id_arg){ id=id_arg; }
    public String getImgURL(){ return imgURL; }
    public void setImgURL(String str){ imgURL=str; }
    
}
