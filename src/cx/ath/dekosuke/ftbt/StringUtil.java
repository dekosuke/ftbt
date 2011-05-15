package cx.ath.dekosuke.ftbt;

import java.util.ArrayList;

public class StringUtil {
	static String safeCut(String str, int length){
		if(str.length()>length){
			return str.substring(0, length)+"...";
		}
		return str;
	}

	//これ汎用じゃない・・
	static String[] nonBlankSplit(String str, String[] addition){
		String[] elems = str.split("\n");
		ArrayList<String> filtered_elems = new ArrayList<String>();
		for(int i=0;i<elems.length;++i){
			//if(elems[i])
			if(elems[i].length()>0){
				filtered_elems.add(elems[i]);
			}
		}
		if(elems.length>1){
			for(int i=0;i<addition.length;++i){
				filtered_elems.add(addition[i]);
			}
		}
		//FLog.d("length="+filtered_elems.size());
		return (String[])filtered_elems.toArray(new String[0]);
	}
}
