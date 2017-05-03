package other.abpajc.bdiabdbikcikc.api;

import java.util.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;



/**
 * Created by HOME-PC-Acer on 2017/5/2.
 */
public class SignatureUtil {
    protected static Logger logger = LoggerFactory.getLogger();
    public static final String  APP_SECRET="13d254b216d603509189c8f53045a55f68ce9bbb";

    /**
     *
     * @param list  把一个list 用&连接后，进行加密
     * @return
     */
    public static String signature(List<String> list){
        String result=null;
        if(null==list||list.isEmpty()){
            return  result;
        }
        Collections.sort(list);
        String s= StringUtils.join(list,"&");
        result= DigestUtils.shaHex(s);
        logger.info("list2String： \""+s+"\"；signature： \""+result+"\"");
        return  result;
    }
    /**
     * 把一个map 用&连接后，进行加密
     * @param map
     * @return
     */
    public static String signature(Map<String,Object> map){
        String result=null;
        if(null==map||map.isEmpty()){
            return  result;
        }
        Iterator se;
        Map.Entry entry;
        se= map.entrySet().iterator();
        List<String> list=new ArrayList<String>();
        while(se.hasNext()) {
            entry = (Map.Entry)se.next();
            String tmp=(String)entry.getKey()+"="+ (String)entry.getValue();
            list.add(tmp);
        }
        return  signature(list);
    }
    /**
     * 把字符串进行shahex加密。
     * @param openStr
     * @return
     */
    public  static String signature(String openStr){
        String result=null;
        if(null==openStr){
            return  result;
        }
        result= DigestUtils.shaHex(openStr);
        logger.info("openStr： \""+openStr+"\"；signature： \""+result+"\"");
        return  result;
    }

    public static void main(String[] args) {

        SignatureUtil.signature("");
        ArrayList<String> testList=new ArrayList<String>();
        testList.add("id="+1000);
        testList.add("name="+"张三丰");
        String  timstamp=String.valueOf((System.currentTimeMillis() / 1000));
        testList.add("timstamp="+"10000");
        testList.add("appSecret="+SignatureUtil.APP_SECRET);
        SignatureUtil.signature(testList);
        SignatureUtil.signature("hello world!");


    }
}
