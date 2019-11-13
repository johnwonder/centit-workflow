package com.centit.workflow.client.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.framework.appclient.AppSession;
import com.centit.framework.appclient.HttpReceiveJSON;
import com.centit.framework.appclient.RestfulHttpRequest;
import com.centit.support.database.utils.PageDesc;
import com.centit.support.network.UrlOptUtils;
import com.centit.workflow.client.service.FlowDefineClient;
import com.centit.workflow.po.FlowInfo;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Created by chen_rj on 2018-5-2.
 */
@Service
public class FlowDefineClientImpl implements FlowDefineClient {
    @Value("${workflow.server:}")
    private String workFlowServerUrl;
    @Value("${workflow.server.login:}")
    private String workFlowServerLoginUrl;

    public FlowDefineClientImpl() {

    }

    private AppSession appSession;


    private CloseableHttpClient allocHttpClient() throws Exception {
        return appSession.allocHttpClient();
    }

    private void makeAppSession() {
        appSession = new AppSession(workFlowServerUrl,false,null,null);
        appSession.setAppLoginUrl(workFlowServerLoginUrl);
    }

    @PostConstruct
    public void init(){
        //this.setWorkFlowServerUrl(workFlowServerUrl);
        makeAppSession();
    }

    @Override
    public List<FlowInfo> listLastVersionFlow(Map<String, Object> filterMap,
                                              PageDesc pageDesc) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            UrlOptUtils.appendParamsToUrl(
                UrlOptUtils.appendParamsToUrl("/flow/define/listFlow",
                filterMap), (JSONObject)JSON.toJSON(pageDesc)));
        pageDesc.copy(receiveJSON.getDataAsObject("pageDesc", PageDesc.class));
        return receiveJSON.getDataAsArray("objList",FlowInfo.class);
    }


    /**
     * 获取 流程信息
     *
     * @param flowCode 流程代码
     * @param version  流程版本
     * @return 流程信息
     */
    @Override
    public FlowInfo getFlowInfo(String flowCode, long version) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            "/flow/define/"+String.valueOf(version)+"/"+flowCode);
        return receiveJSON.getDataAsObject(FlowInfo.class);
    }

    /**
     * 获取 流程所有版本
     *
     * @param flowCode 流程代码
     * @return 流程所有版本
     */
    @Override
    public List<FlowInfo> listFlowInfoVersion(String flowCode, PageDesc pageDesc) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            UrlOptUtils.appendParamsToUrl("/flow/define/lastversion/"+flowCode,
                (JSONObject)JSON.toJSON(pageDesc)));
        pageDesc.copy(receiveJSON.getDataAsObject("pageDesc", PageDesc.class));
        return receiveJSON.getDataAsArray("objList",FlowInfo.class);
    }

    /**
     * 获取流程所有办件角色
     *
     * @param flowCode 流程代码
     * @param version  流程版本
     * @return 办件角色代码 办件角色名称
     */
    @Override
    public Map<String, String> listFlowItemRole(String flowCode, long version) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            "/flow/define/itemrole/"+flowCode +"/"+String.valueOf(version));
        return receiveJSON.getDataAsMap(String.class);
    }

    /**
     * 获取流程所有变量
     *
     * @param flowCode 流程代码
     * @param version  流程版本
     * @return 变量 代码 变量 名称
     */
    @Override
    public Map<String, String> listFlowVariable(String flowCode, long version) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            "/flow/define/variable/"+flowCode +"/"+String.valueOf(version));
        return receiveJSON.getDataAsMap(String.class);
    }

    /**
     * 获取流程所有阶段
     *
     * @param flowCode 流程代码
     * @param version  流程版本
     * @return 办件角色代码 办件角色名称
     */
    @Override
    public Map<String, String> listFlowStage(String flowCode, long version) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            "/flow/define/stage/"+flowCode +"/"+String.valueOf(version));
        return receiveJSON.getDataAsMap(String.class);
    }

    @Override
    public JSONArray listItemRoleFilter(String flowCode, long version, String itemRoleCode) {
        HttpReceiveJSON receiveJSON = RestfulHttpRequest.getResponseData(appSession,
            "/flow/define/itemRoleFilter/"+flowCode+"/"+version+"/"+itemRoleCode);
        return receiveJSON.getJSONArray();
    }


    public void setWorkFlowServerUrl(String workFlowServerUrl) {
        this.workFlowServerUrl = workFlowServerUrl;
    }
}
