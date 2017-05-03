package other.abpajc.bdiabdbikcikc.trigger;

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
import other.abpajc.bdiabdbikcikc.api.BaseApiSupport;
import other.abpajc.bdiabdbikcikc.api.RSAPIUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by HOME-PC-Acer on 2017/5/2.
 * 新增省份后触发，要去软素新增，并把对应得ID更新到记录中。
 */
public class ProvinceAddAfterTrigger extends BaseApiSupport implements ScriptTrigger {

	private Logger logger = LoggerFactory.getLogger();

	/*  省份
belongTypeId:100309601
belongId:100307201
paramMap['name__105956202']:测试省份
paramMap['dimDepart__105956205']:283934
paramMap['provinceid__105956219']:9999121312
paramMap['province4rsID__105956004']:232323232332
 */
	@Override
	public ScriptTriggerResult execute(ScriptTriggerParam scriptTriggerParam) throws ScriptBusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<DataModel> list = scriptTriggerParam.getDataModelList();
		Object provincename = list.get(0).getAttribute("name");
		Object provinceid = list.get(0).getAttribute("provinceid");
		Object province4rsID = list.get(0).getAttribute("province4rsID");

		Object id = list.get(0).getAttribute("id");
		logger.info("id:="+id);
		paramMap.put("ProvinceName", provincename);
		try {
			String result = RSAPIUtils.opEntityPost(paramMap, RSAPIUtils.ACCOUNT_ENTITYURL,RSAPIUtils.INSERT_OPSTR);
			logger.debug("insertAccount's result:"+result);
			if (result != null&&result.indexOf("id")>0) {
				JSONObject jsonobject = JSONObject.fromObject(result);

				String id4newProv=jsonobject.getString("id");
				JSONObject newProvJson=new JSONObject();
				newProvJson.put("id",id);
				newProvJson.put("province4rsID",id4newProv);

				newProvJson=updateBelongs(newProvJson);
				logger.info("updateBelongs:"+newProvJson.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
		}

		ScriptTriggerResult scriptTriggerResult = new ScriptTriggerResult();
		scriptTriggerResult.setDataModelList(scriptTriggerParam.getDataModelList());
		return scriptTriggerResult;
	}

}
