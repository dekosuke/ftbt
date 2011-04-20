package cx.ath.dekosuke.ftbt;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class FutabaThreadParser {  
  
    private static final String FORMTAG = "form";
    private static final String BLOCKQUOTETAG = "qlockquote";
    private static final String TABLETAG = "table";
    private static final String IMGTAG = "img";
    private static final String SRCTAG = "src";

    private String urlStr;
    private String title;
    private String titleImgURL;

    private ArrayList<FutabaStatus> statuses;    
  
    public FutabaThreadParser(String urlStr) {  
        this.urlStr = urlStr;
        title="(title)";
        statuses = new ArrayList<FutabaStatus>();
    }  
 
    // メモ:ふたばのスレッドはhtml-body-2つめのformのなかにある
    // スレッドの形式:
    public void parse() {  
        XmlPullParser parser = Xml.newPullParser();  
        try {  
            //正規表現でパーズ範囲を絞り込む
            Pattern honbunPattern = 
                Pattern.compile("<form.+?>.+?</form>", Pattern.DOTALL);
            Pattern resPattern = 
                Pattern.compile("<table.*?>(.+?)</table>", Pattern.DOTALL);
            Pattern textPattern = 
                Pattern.compile("<blockquote.*?>(.+?)</blockquote>", Pattern.DOTALL);
            Pattern imgPattern = 
                Pattern.compile("<a.*?target.*?href=\"(.+?)\"", Pattern.DOTALL);
            Pattern thumbPattern = 
                Pattern.compile("<img.*?src=\"(.+?)\"", Pattern.DOTALL);
            Pattern tagPattern = Pattern.compile("<.+?>", Pattern.DOTALL);
            byte[] data = HttpClient.getByteArrayFromURL(urlStr);  
            //parser.setInput(new StringReader(new String(data, "UTF-8")));  
            Matcher mc = honbunPattern.matcher(new String(data, "Shift-JIS"));
            mc.find();
            mc.find(); //2つ目
            String honbun = mc.group(0);
            //Log.d( "ftbt", honbun );
            Matcher mcRes = resPattern.matcher(honbun);
            while( mcRes.find() ){
                Matcher mcText = textPattern.matcher(mcRes.group(1));
                //Log.d( "ftbt", mcRes.group(1) );
                mcText.find();
                FutabaStatus status = new FutabaStatus();
                String text = mcText.group(1);
                text = tagPattern.matcher(text).replaceAll(""); //タグ除去
                status.setText(text);
                Matcher mcImg = thumbPattern.matcher(mcRes.group(1));
                if( mcImg.find() ){
                    status.setImgURL(mcImg.group(1));
                    Log.d( "ftbt", mcImg.group(1) );
                }
                //Log.d( "ftbt", text );
                statuses.add(status);
            }
            Log.d( "ftbt", String.valueOf(statuses.size()) );
            /*
            parser.setInput(new StringReader(mc.group(0)));
            boolean isFinished = false;  
            int eventType=parser.next();
            int formNum=2;
            boolean threadImage = false;
            Log.d( "ftbt", "parse start" );
            */
            /*
            while (eventType != XmlPullParser.END_DOCUMENT && !isFinished) {  
                String name = null; 
                switch (eventType) {  
                case XmlPullParser.START_DOCUMENT:  
                    //list = new ArrayList<TwitterStatus>();  
                    break;  
                case XmlPullParser.START_TAG:  
                    name = parser.getName();  
                    Log.d( "ftbt", name );
                    if (name.equalsIgnoreCase(FORMTAG)) {
                        formNum++;
                    }else if(name.equalsIgnoreCase(BLOCKQUOTETAG)){
                        if(formNum==2){ //スレッドタイトル
                            title=parser.getText();
                             //スレッド追加
                            FutabaStatus status = new FutabaStatus();
                            status.setText(title);
                            status.setImgURL(titleImgURL);
                            statuses.add(status);
                        }
                    }else if(name.equalsIgnoreCase(IMGTAG)){
                        if(formNum==2 && !threadImage){ //スレ画像
                            titleImgURL = getAttributeByName(parser, SRCTAG);
                            threadImage = true;
                        }
                    }else if(name.equalsIgnoreCase(TABLETAG)){
                        if(formNum==2 && threadImage){ //ここから各レスを検索
                            addStatus(parser);
                        }
                    }
                    break;  
                case XmlPullParser.END_TAG:  
                    name = parser.getName(); 
                    break;  
                }  
                eventType = parser.next();  
            }
            */
        } catch (Exception e) { 
            Log.d( "ftbt", e.toString() ); 
            throw new RuntimeException(e);  
        }  
        //return list;  
    }

    //返信ひとつげっと
    public void addStatus(XmlPullParser parser){
        String text="";
        String imgURL="";
        try{
            int eventType = parser.next();
            while(eventType != XmlPullParser.END_DOCUMENT){
                String name = null;
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if(name.equalsIgnoreCase(BLOCKQUOTETAG)){
                            text = parser.getText(); //リプライ文
                        }else if(name.equalsIgnoreCase(IMGTAG)){
                            imgURL = getAttributeByName(parser, SRCTAG);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name=parser.getName();
                        if(name.equalsIgnoreCase(TABLETAG)){
                            //スレッド追加
                            FutabaStatus status = new FutabaStatus();
                            status.setText(text);
                            status.setImgURL(imgURL);
                            statuses.add(status);
                            return;
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);  
        }
    }

    public String getAttributeByName(XmlPullParser parser, String nameArg){
        for(int i=0;i<parser.getAttributeCount();++i){
            String name = parser.getAttributeName(i);
            if(nameArg.equalsIgnoreCase(name)){
                return parser.getAttributeValue(i);
            }
        }
        return "";
    }

    public ArrayList<FutabaStatus> getStatuses(){
        return statuses;
    }
} 
