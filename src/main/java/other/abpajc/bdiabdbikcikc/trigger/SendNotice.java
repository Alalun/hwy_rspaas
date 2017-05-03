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

import java.io.IOException;

/**
 * @author jiangly
 * @date 2017/4/26
 * @aim
 */
public class SendNotice implements ScriptTrigger {

    private Logger logger = LoggerFactory.getLogger();

    @Override
    public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
        try {
            DataModel customize = scriptTriggerParam.getDataModelList().get(0);
            String id = customize.getAttribute("id").toString();
            RkhdHttpClient client = new RkhdHttpClient();
            RkhdHttpData data = new RkhdHttpData();
            //添加相关员工
            data.setCallString("/data/v1/objects/group/join-related");
            data.setCall_type("post");
            JSONObject params = new JSONObject();
            params.put("belongId", "100300201");
            params.put("businessId", id);
            JSONArray users = new JSONArray();
            JSONObject user = new JSONObject();
            user.put("id", 673702L);//默认加 公用账号 为相关员工
            users.add(user);
            params.put("users", users);
            data.putFormData("params", params.toString());
            String addMemberResult = client.performRequest(data);
            logger.info("addMemberResult" + addMemberResult);

            //发起@
            data.setCallString("/data/v1/feed/activityRecord/create");
            data.setCall_type("post");
            JSONObject objectInfo = new JSONObject();
            objectInfo.put("belongId", "100300201");//发起客户合并讨论实体
            objectInfo.put("objectId", id);//数据主键
            objectInfo.put("activityTypeId", -11);
            objectInfo.put("source", 0);
            objectInfo.put("content", "@姜良友 @公用账号 欢迎欢迎");
            data.putFormData("objectInfo", objectInfo.toString());
            String activityRecordResult = client.performRequest(data);
            logger.info("activityRecordResult" + activityRecordResult);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return new ScriptTriggerResult(scriptTriggerParam.getDataModelList());
    }
}
