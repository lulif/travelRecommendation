package com.gdxx.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdxx.dao.SubwayStationsDao;
import com.gdxx.dao.SubwayTransferDao;
import com.gdxx.entity.SubwayStations;
import com.gdxx.entity.SubwayTransfer;
import com.gdxx.service.JedisOperationService;
import com.gdxx.utils.JedisUtil;

@Service
public class JedisOperationServiceImpl implements JedisOperationService {

    @Autowired
    private SubwayTransferDao subwayTransferDao;
    @Autowired
    private SubwayStationsDao subwayStationsDao;
    @Autowired
    private JedisUtil.Strings jedisStrings;
    @Autowired
    private JedisUtil.Keys jedisKeys;

    private ObjectMapper mapper = new ObjectMapper();
    private final String BYSTNAME_KEY = "_byStationName";
    private final String BYCTNAME_KEY = "_byCityName";
    private final String TRANSFER_KEY = "_allTransfer";
    private final String BYLINAME_KEY = "_byLineId";
    private final String ALLSTATION_KEY = "_allStation";
    private final String SUPPORT_KEY = "_forSupport";

    public List<SubwayStations> getStationsByName(String stationName) {
        List<SubwayStations> resList = null;
        if (!jedisKeys.exists(stationName + BYSTNAME_KEY)) {
            resList = subwayStationsDao.querySubwayStationByName(stationName);
            try {
                String jsonString = mapper.writeValueAsString(resList);
                jedisStrings.set(stationName + BYSTNAME_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(stationName + BYSTNAME_KEY);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, SubwayStations.class);
            try {
                resList = mapper.readValue(jsonString, javaType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resList;
    }

    public List<SubwayTransfer> getTransferList() {
        List<SubwayTransfer> resList = null;
        if (!jedisKeys.exists(TRANSFER_KEY)) {
            resList = subwayTransferDao.querySubwayTransferList();
            try {
                String jsonString = mapper.writeValueAsString(resList);
                jedisStrings.set(TRANSFER_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(TRANSFER_KEY);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, SubwayTransfer.class);
            try {
                resList = mapper.readValue(jsonString, javaType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resList;
    }

    @Override
    public Set<String> getStationNamesForSupport(String cityName) {
        Set<String> resSet = null;
        if (!jedisKeys.exists(cityName + SUPPORT_KEY)) {
            resSet = subwayStationsDao.queryStationsNameForSupport(cityName);
            try {
                String jsonString = mapper.writeValueAsString(resSet);
                jedisStrings.set(cityName + SUPPORT_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(cityName + SUPPORT_KEY);
            try {
                resSet = mapper.readValue(jsonString, Set.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resSet;
    }

    public List<SubwayStations> getStationsByLineId(Integer lineId) {
        List<SubwayStations> resList = null;
        if (!jedisKeys.exists(lineId + BYLINAME_KEY)) {
            resList = subwayStationsDao.querySubwayStationByLineId(lineId);
            try {
                String jsonString = mapper.writeValueAsString(resList);
                jedisStrings.set(lineId + BYLINAME_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(lineId + BYLINAME_KEY);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, SubwayStations.class);
            try {
                resList = mapper.readValue(jsonString, javaType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resList;
    }

    public List<SubwayStations> getStationsByCityName(String cityName) {
        List<SubwayStations> resList = null;
        if (!jedisKeys.exists(cityName + BYCTNAME_KEY)) {
            resList = subwayStationsDao.querySubwayStationByCityName(cityName);
            try {
                String jsonString = mapper.writeValueAsString(resList);
                jedisStrings.set(cityName + BYCTNAME_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(cityName + BYCTNAME_KEY);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, SubwayStations.class);
            try {
                resList = mapper.readValue(jsonString, javaType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resList;
    }

    public List<SubwayStations> getAllStation() {
        List<SubwayStations> resList = null;
        if (!jedisKeys.exists(ALLSTATION_KEY)) {
            resList = subwayStationsDao.querySubwayStationsList();
            try {
                String jsonString = mapper.writeValueAsString(resList);
                jedisStrings.set(ALLSTATION_KEY, jsonString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            String jsonString = jedisStrings.get(ALLSTATION_KEY);
            JavaType javaType = mapper.getTypeFactory().constructParametricType(ArrayList.class, SubwayStations.class);
            try {
                resList = mapper.readValue(jsonString, javaType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return resList;
    }

}
