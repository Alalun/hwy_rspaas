package other.abpajc.bdiabdbikcikc.api;

import com.rkhd.platform.sdk.http.CommonData;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HOME-PC-Acer on 2017/5/2.
 * 软素API调用的接口工具类。
 */
public class RSAPIUtils {
    protected static Logger logger = LoggerFactory.getLogger();
    public static final  String RSDomainURL="https://rp14.crmatmobile.com/api/vi/";
    public static final String ACCOUNT_ENTITYURL="account";
    public static final String PROVINCE_ENTITYURL="province";
    public static final String CITY_ENTITYURL="city";
    public static final String COUNTY_ENTITYURL="county";
    public static final String INSERT_OPSTR="/insert";
    public static final String UPDATE_OPSTR="/update";
    public static final String DELETE_OPSTR="/delete";
    public static final String LIST_OPSTR="/list";

    /**
     * 软素客户信息新增的API调用
     * @param body  请求报文的格式。
     *  @param entityUrl 对象实体串 ACCOUNT_ENTITYURL|PROVINCE_ENTITYURL|CITY_ENTITYURL。
     *  @param opStr  对象操作串。INSERT_OPSTR|UPDATE_OPSTR|DELETE_OPSTR|LIST_OPSTR
     * @return  返回报文请求。
     * @throws IOException
     */
    public static String opEntityPost(Map<String, Object> body,String entityUrl,String opStr) throws IOException {
        if(body==null||body.isEmpty()){
            return null;
        }
        StringBuffer callString=new StringBuffer(RSDomainURL);
        String timeStampStr=String.valueOf( System.currentTimeMillis());
        callString=callString.append(entityUrl).append(opStr);//.append("?timestamp=").append(timeStampStr).append("&appSecret=").append(SignatureUtil.APP_SECRET);
        logger.info("callString:="+callString);
        //统一添加时间戳
        body.put("timestamp", timeStampStr);
        //签名
        String signature=SignatureUtil.signature(body);
        //统一添加签名
        body.put("signature", signature);
        String s=callApiPost(body,callString.toString());
        logger.info("opCompanyPost:="+s);
        return s;
    }



    /**
     * 根据拼接得url 和对应得map  提交http+json。
     *
     * @param body   key  value map
     * @param callStr   请求地址。
     * @return  返回调用得串。
     * @throws IOException
     */
    public static String callApiPost(Map<String,Object> body,String callStr) throws IOException {
        CommonHttpClient commHttpClient = new CommonHttpClient();
        CommonData rkhdHttpData = new CommonData();
        rkhdHttpData.setCallString(callStr);
        rkhdHttpData.setCall_type("POST");
        rkhdHttpData.putFormDataAll(body);
        String s = commHttpClient.performRequest(rkhdHttpData);
        logger.info("callApiPost:="+s);
        return s;
    }

    public static void main(String[] args) {

        Map<String,Object> body=new HashMap<String, Object>();
        /*
        {
"AccountNumber": "1000089",
"AccountName": "北京九州通医药有限公司"
"ProvinceId": "2c9f9a7159850eba01598598e5c40003",
"CityId": "2c9f9a7159850eba015986286747000e",
"CountyId": "2c9f9a715991c22c01599332a9d50001",
"AccountKind": "1",
"AccountType": "11",
"OldName": "",
"timestamp": 1491984013,
"signature"："8F7E517C3E2B4C3A8DE22E6980FBDB88"
}
         */
        body.put("AccountNumber","1000089");
        body.put("AccountName","北京九州通医药有限公司");
        body.put("ProvinceId","2c9f9a7159850eba01598598e5c40003");
        body.put("CityId","2c9f9a7159850eba015986286747000e");
        body.put("CountyId","2c9f9a715991c22c01599332a9d50001");
        body.put("AccountKind","1");
        body.put("AccountType","11");
        body.put("timestamp",String.valueOf(System.currentTimeMillis()));
        String  signature=SignatureUtil.signature(body);
        body.put("signature",signature);
        try {
            RSAPIUtils.opEntityPost(body, RSAPIUtils.ACCOUNT_ENTITYURL,RSAPIUtils.INSERT_OPSTR);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
