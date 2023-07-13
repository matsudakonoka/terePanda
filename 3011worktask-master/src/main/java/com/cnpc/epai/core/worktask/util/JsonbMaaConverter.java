package com.cnpc.epai.core.worktask.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.sessions.Session;
import org.postgresql.util.PGobject;
import org.eclipse.persistence.mappings.converters.Converter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class JsonbMaaConverter implements Converter {
	private static final long serialVersionUID = 1L;

	@Override

	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		try {
			PGobject out = new PGobject();
			out.setType("jsonb");
			out.setValue(JSONObject.toJSONString(objectValue));
			return out;
		} catch (SQLException e) {
			throw new IllegalArgumentException("Unable to serialize to json field ", e);
		}
	}

	@Override
	public List<Map> convertDataValueToObjectValue(Object dataValue, Session session) {
		List<Map> rtnList = null;
		try {
			if(dataValue instanceof PGobject && ((PGobject) dataValue).getType().equals("jsonb")) {
				Object json  = JSONObject.parse(dataValue.toString());
				if(json instanceof JSONObject){
//    				JSONObject jsonObject = (JSONObject)json;
					rtnList = Arrays.asList(JSONObject.parseObject(dataValue.toString(), Map.class));
				}else if (json instanceof JSONArray){
//    				JSONArray jsonArray = (JSONArray)json;
					rtnList = JSONArray.parseArray(dataValue.toString(), Map.class);
				}
			}
		} catch (JSONException e) {

		}
		return rtnList;
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public void initialize(DatabaseMapping databaseMapping, Session session) {

	}

}
