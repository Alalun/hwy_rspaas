package other.abpajc.bdiabdbikcikc.trigger;
import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import other.abpajc.bdiabdbikcikc.api.BaseApiSupport;
import other.abpajc.bdiabdbikcikc.api.QueryResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HOME-PC-Acer on 2017/4/27.
 * 客户合并后，把客户的业务部门数据管理员添加到数据成员中，并@对应的数据管理员；
 */
public class InitiateCusDisAddAfterTrigger extends BaseApiSupport implements ScriptTrigger {
    private Logger logger = LoggerFactory.getLogger();
    //用户信息纪录
    protected Map<String,String> userMap=new HashMap<String, String>();

    //客户合并讨论 的belongId=100300201
    //资源匹配的  belongId=100292801
    //业务部门名称的 belongId=100292802
    //客户合并申请  belongId=100292503
    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        List<DataModel> list = scriptTriggerParam.getDataModelList();
        DataModel customize = list.get(0);
        String id = customize.getAttribute("id").toString();
        Long operUerId=scriptTriggerParam.getUserId();

        // 源客户ID
        Object sourceClient =list.get(0).getAttribute("customItem1");
        //被合并客户ID
        Object  incorporatedClient= list.get(0).getAttribute("customItem7");

        //獲得管理客戶對應部門的數據管理員。
        JSONArray sourceClientDmpt=getAccountRelationDmpt(sourceClient.toString());
        JSONArray incorporatedClientDmpt=getAccountRelationDmpt(incorporatedClient.toString());
        if(!sourceClientDmpt.isEmpty()&&sourceClientDmpt.size()>0){
            if(!incorporatedClientDmpt.isEmpty()&&incorporatedClientDmpt.size()>0){
                sourceClientDmpt.addAll(incorporatedClientDmpt);
            }
        }else{
            ScriptTriggerResult scriptTriggerResult = new ScriptTriggerResult();
            scriptTriggerResult.setDataModelList(scriptTriggerParam.getDataModelList());
            logger.info("InitiateCusDisAddAfterTrigger:end:" + scriptTriggerResult);
            return scriptTriggerResult;
        }
        logger.info("sourceClientDmpt:="+sourceClientDmpt.toString());
        JSONArray users=new JSONArray();
        Map<String,String> allDmptDB=getAllDmptDB();
        StringBuffer content=new StringBuffer("");
        //为避免多个部门的数据管理员是一个人，添加用户@的时候，需要顾虑重复的。
        for(int i=0;i<sourceClientDmpt.size();i++){
            JSONObject record=sourceClientDmpt.getJSONObject(i);
            JSONObject user=new JSONObject();
            String busDmptName=record.getString("customItem10");
            String userId=allDmptDB.get(busDmptName);
            logger.info("userId:="+userId);
            //去重，并且去掉当前操作员。去掉当前操作员是否要呢!!
            if(userId==null|userMap.containsKey(userId)||userId.equals(operUerId.toString())){
                continue;
            } else {
                String userName;
                JSONObject userInfo = getUserInfo(Long.parseLong(userId));
                if (userInfo != null && userInfo.containsKey("name")){
                    userName = userInfo.getString("name");
                    userMap.put(userId,userName);
                    user.put("id",userId);
                    users.add(user);
                    content.append("@").append(userName).append(" ");
                }
            }
        }
        logger.info("users"+users);
        //把客戶相關業務部門的數據管理員添加到成員中
        String addMemberResult=null;
        try {
            addMemberResult= addMemberResult(users,id);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //對這些成員數據管理員進行@ 並發送消息 XXXX发起了把 客户XXXX 合并到 客户XXXX的讨论，有问题请回复。
        //根据ID 合并企业的名称
        //https://crm.xiaoshouyi.com/json/crm_customize/save.action

        JSONObject  sourceClientJson= getBelongs(Long.parseLong(sourceClient.toString()));
        JSONObject  incorporatedClientJson= getBelongs(Long.parseLong(incorporatedClient.toString()));
        String  opUserName=getUserName(operUerId);
        String  sourceClientName=sourceClientJson.getString("name");
        String incorporatedClientName=incorporatedClientJson.getString("name");

        content.append(opUserName).append("发起了把客户：").append(incorporatedClientName).append(" 合并到客户：").
                append(sourceClientName).append(" 的讨论，有问题请回复");
        String activityRecordResultStr = null;
        try {
            activityRecordResultStr=activityRecordResult(content.toString(), id);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("activityRecordResultStr:="+activityRecordResultStr);


        ScriptTriggerResult scriptTriggerResult = new ScriptTriggerResult();
        scriptTriggerResult.setDataModelList(scriptTriggerParam.getDataModelList());
        logger.info("InitiateCusDisAddAfterTrigger:end:" + scriptTriggerResult);
        return scriptTriggerResult;
    }


    /**
     * 获得企业的资源匹配关系及對應的管理員
     * 资源匹配关系 api 名称 为 customEntity18
     * 客户customItem4
     * 业务部门customItem10
     * @return
     */
    private JSONArray getAccountRelationDmpt(String accountName) {
        StringBuffer sql = new StringBuffer();
        sql.append("select customItem4,customItem10 from customEntity18 where customItem4=").append(accountName);
        JSONArray resultArray = queryAllResult(sql.toString());
        return resultArray;
    }

    /**
     * 一次性获得所有的部门急对应的数据管理员。
     * 业务部门表 customEntity21
     * 业务部门名称name
     * 数据管理员shujuguanliyuan
     * @return  部門名稱，數據管理員ID。
     */
    public Map<String,String> getAllDmptDB(){
        StringBuffer sql = new StringBuffer();
        sql.append("select name,shujuguanliyuan from customEntity21");
        JSONArray resultArray = queryAllResult(sql.toString());

        Map<String,String> allDmptDBMap=new HashMap<String, String>();
        for (int i = 0; i < resultArray.size(); i++) {
            JSONObject record = resultArray.getJSONObject(i);
            String name = record.getString("name");
            String shujuguanliyuan = record.getString("shujuguanliyuan");
            allDmptDBMap.put(name,shujuguanliyuan);
        }
        return allDmptDBMap;
    }

    /**
     * 增加業務對象的成員
     * @param users  要添加成員的用戶JSON對象數組，衹要包含ID就可以了
     *
     * @param businessId  對應的業務對象。
     * @throws IOException
     */
    private String addMemberResult(JSONArray users,String businessId)throws IOException{
        //客户合并讨论 的belongId=100300201
        RkhdHttpClient client = new RkhdHttpClient();
        RkhdHttpData data = new RkhdHttpData();
        //添加相关员工
        data.setCallString("/data/v1/objects/group/join-related");
        data.setCall_type("post");
        JSONObject params = new JSONObject();
        params.put("belongId", "100300201");
        params.put("businessId", businessId);
        params.put("users", users);
        data.putFormData("params", params.toString());
        String addMemberResult = client.performRequest(data);
        logger.info("addMemberResult" + addMemberResult);
        return addMemberResult;
    }

    /**
     *
     * @param content
     * @param businessId
     * @return
     * @throws IOException
     */
    private String activityRecordResult(String content,String businessId)throws IOException{
        //客户合并讨论 的belongId=100300201
        RkhdHttpClient client = new RkhdHttpClient();
        RkhdHttpData data = new RkhdHttpData();
        data.setCallString("/data/v1/feed/activityRecord/create");
        data.setCall_type("post");
        JSONObject objectInfo = new JSONObject();
        objectInfo.put("belongId", "100300201");//发起客户合并讨论实体
        objectInfo.put("objectId", businessId);//数据主键
        objectInfo.put("activityTypeId", -11);
        objectInfo.put("source", 0);
        objectInfo.put("content", content);
        data.putFormData("objectInfo", objectInfo.toString());
        String activityRecordResult = client.performRequest(data);
        logger.info("activityRecordResult" + activityRecordResult);
        return activityRecordResult;
    }

    /**
     * 获得用户信息
     * @param userId
     * @return
     */
    protected String getUserName(Long userId){
        String userName = "";
        if(userId == 0) return "";

        if(userMap.containsKey(String.valueOf(userId))){
            userName= userMap.get(userId);
        } else {
            JSONObject userInfo = getUserInfo(userId);
            if (userInfo != null && userInfo.containsKey("name")){
                userName = userInfo.getString("name");
                userMap.put(String.valueOf(userId),userName);
                return userName;
            }
        }
        return userName;
    }
    public static void main(String[] args) {

         /*
         belongTypeId:100302801
         belongId:100300201
         paramMap['customItem1__105845704']:103580517
         paramMap['customItem7__105844920']:103580516
         paramMap['customItem10__105845709']:两个客户属于同一集团的不同分公司
         paramMap['dimDepart__105844907']:283934
         paramMap['customItem8__105845707']:1
         */
        InitiateCusDisAddAfterTrigger initiateCusDisAddAfterTrigger=new InitiateCusDisAddAfterTrigger();
       QueryResult queryResult=initiateCusDisAddAfterTrigger.getAllBelongs();
        System.out.println(queryResult.getRecords().toString());
        ArrayList<DataModel> testArrayList=new ArrayList<DataModel>();
        Map<String,Object>  map=new HashMap<String, Object>();
        map.put("id","103701139");
        map.put("customItem1","103580517");
        map.put("customItem7","103580516");
        map.put("customItem8","1");
        map.put("dimDepart","283934");
        map.put("customItem10","两个客户属于同一集团的不同分公司");


        DataModel newDataModel=new DataModel(map);
        testArrayList.add(newDataModel);
        ScriptTriggerParam  test=new ScriptTriggerParam(testArrayList);

        try{
            initiateCusDisAddAfterTrigger.execute(test);
        }catch (Exception e){
            e.printStackTrace();
        }


    }


}
