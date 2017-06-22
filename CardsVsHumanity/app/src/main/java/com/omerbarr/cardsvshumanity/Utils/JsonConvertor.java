package com.omerbarr.cardsvshumanity.Utils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.omerbarr.cardsvshumanity.BusinessLogic.DataTransferred;
import com.omerbarr.cardsvshumanity.BusinessLogic.GameData;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by omer on 12/02/2017.
 */

public class JsonConvertor {

    final static int COMMAND = 1;
    final static int CONTENT = 2;

    public static String createJsonWithCommand(int command,String jsonContent){
        Map<Integer,String> map = new HashMap<>();
        map.put(COMMAND,String.valueOf(command));
        map.put(CONTENT,jsonContent);
        String jsonString = convertToJson(map);
        isJSONValid(jsonString);
        return  jsonString;
    }

    public static int getCommand(String jsonString){
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer,String>>(){}.getType();
        Map<Integer,String> map = gson.fromJson(jsonString, type);
        return Integer.valueOf(map.get(COMMAND));
    }

    public static String getJsonContent(String jsonString){
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer,String>>(){}.getType();
        Map<Integer,String> map = gson.fromJson(jsonString, type);
        return map.get(CONTENT);
    }

    public static String convertToJson(Object content){
        if ( content == null )
            return null;
        String jsonString = new Gson().toJson(content);
        return jsonString;
    }

    public static DataTransferred.RoundData JsonToRoundData(String jsonString){
        Gson gson = new Gson();
        Type type = new TypeToken<DataTransferred.RoundData>(){}.getType();
        return gson.fromJson(jsonString, type);
    }

    public static DataTransferred.CzarData JsonToCzarData(String jsonString){
        Gson gson = new Gson();
        Type type = new TypeToken<DataTransferred.CzarData>(){}.getType();
        return gson.fromJson(jsonString, type);
    }
    public static DataTransferred.PlayerData JsonToPlayerData(String jsonString){
        Gson gson = new Gson();
        Type type = new TypeToken<DataTransferred.PlayerData>(){}.getType();
        return gson.fromJson(jsonString, type);
    }

    public static boolean isJSONValid(String jsonContent) {
        try {
            getCommand(jsonContent);
            getJsonContent(jsonContent);
        } catch (Exception ex) {
            String TAG = "DEBUG: "+ JsonConvertor.class.getSimpleName();
            Log.e(TAG,"Error in JsonConvertor: \n ex.getMessage()");
        }
        return true;
    }
}
