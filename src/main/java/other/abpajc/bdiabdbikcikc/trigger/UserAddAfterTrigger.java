package other.abpajc.bdiabdbikcikc.trigger;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rkhd.platform.sdk.ScriptTrigger;
import com.rkhd.platform.sdk.exception.ScriptBusinessException;
import com.rkhd.platform.sdk.http.CommonData;
import com.rkhd.platform.sdk.http.CommonHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpClient;
import com.rkhd.platform.sdk.http.RkhdHttpData;
import com.rkhd.platform.sdk.log.Logger;
import com.rkhd.platform.sdk.log.LoggerFactory;
import com.rkhd.platform.sdk.model.DataModel;
import com.rkhd.platform.sdk.param.ScriptTriggerParam;
import com.rkhd.platform.sdk.param.ScriptTriggerResult;

import net.sf.json.JSONObject;

public class UserAddAfterTrigger implements ScriptTrigger {

	private Logger logger = LoggerFactory.getLogger();

	@Override
	public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<DataModel> list = scriptTriggerParam.getDataModelList();
		Object name = list.get(0).getAttribute("name");
		Object email = list.get(0).getAttribute("email");
		Object mobile = list.get(0).getAttribute("phone");
		Object hrCode = list.get(0).getAttribute("employeeCode");
		Object id = list.get(0).getAttribute("id");

		paramMap.put("username", name);
		paramMap.put("hrCode", hrCode);
		paramMap.put("name", name);
		paramMap.put("email", email);
		paramMap.put("mobile", mobile);
		paramMap.put("sxyId", 1);
		paramMap.put("departmentId", 5);
		paramMap.put("roleId", 5);

		try {
			String result = insert(paramMap);
			logger.debug(result);

			if (result != null) {
				JSONObject jsonobject = JSONObject.fromObject(result);
				Map<String, Object> updateMap = new HashMap<String, Object>();
				updateMap.put("position", jsonobject.get("id"));
				updateMap.put("id", id);
				Map<String,Object> upMap = new HashMap<String,Object>();
				upMap.put("record", updateMap);
				String updateResult = updateXSY(JSONObject.fromObject(upMap).toString());
				logger.debug(updateResult);
			}
			// {"record" :
			// {"id":"59675","email":"yaya19880110@163.com","phone":"15121212122","name":"yhtceshi20151016","joinAtStr":"2015-10-10","employeeCode":"123584654","positionName":"��������ʦ","userManagerId":"20016","gender":"1","departId":"10401"}}

		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		ScriptTriggerResult scriptTriggerResult = new ScriptTriggerResult();
		scriptTriggerResult.setDataModelList(scriptTriggerParam.getDataModelList());
		return scriptTriggerResult;
	}

	private String insert(Map<String,Object> body) throws IOException {
		CommonHttpClient commHttpClient = new CommonHttpClient();

		CommonData rkhdHttpData = new CommonData();
		rkhdHttpData.setCallString("https://rp14.crmatmobile.com/api//v1/user/insert?timestamp="
				+ System.currentTimeMillis() + "&appSecret=13d254b216d603509189c8f53045a55f68ce9bbb");
		rkhdHttpData.setCall_type("POST");
		rkhdHttpData.putFormDataAll(body);
//		rkhdHttpData.setBody(body);
		String s = commHttpClient.performRequest(rkhdHttpData);
		return s;
	}

	private String updateXSY(String body) throws IOException {
		RkhdHttpClient rkhdHttpClient = new RkhdHttpClient();

		RkhdHttpData rkhdHttpData = new RkhdHttpData();
		rkhdHttpData.setCallString("/data/v1/objects/user/update");
		rkhdHttpData.setCall_type("POST");
		rkhdHttpData.setBody(body);

		String s = rkhdHttpClient.performRequest(rkhdHttpData);
		return s;
	}
}
