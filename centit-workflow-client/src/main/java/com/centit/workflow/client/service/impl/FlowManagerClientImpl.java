package com.centit.workflow.client.service.impl;

import com.centit.framework.appclient.AppSession;
import com.centit.framework.appclient.RestfulHttpRequest;
import com.centit.workflow.client.service.FlowManagerClient;
import com.centit.workflow.po.NodeInstance;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chen_rj on 2017/7/28.
 */
@Service
public class FlowManagerClientImpl implements FlowManagerClient {

    @Value("${workflow.server:}")
    private String workFlowServerUrl;

    @Value("${workflow.server.login:}")
    private String workFlowServerLoginUrl;

    public FlowManagerClientImpl() {

    }
    private AppSession appSession;



    public CloseableHttpClient allocHttpClient() throws Exception {
        return appSession.allocHttpClient();
    }

    public void releaseHttpClient(CloseableHttpClient httpClient) {
        appSession.releaseHttpClient(httpClient);
    }

    public void setWorkFlowServerUrl(String workFlowServerUrl) {
        this.workFlowServerUrl = workFlowServerUrl;
    }

    public void makeAppSession() {
        appSession = new AppSession(workFlowServerUrl,false,null,null);
        appSession.setAppLoginUrl(workFlowServerLoginUrl);
    }


    @PostConstruct
    public void init(){
        //this.setWorkFlowServerUrl(workFlowServerUrl);
        makeAppSession();
    }

    @Override
    public List<NodeInstance> listFlowInstNodes(String wfinstid) throws Exception{
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("flowInstId",String.valueOf(wfinstid));

        return RestfulHttpRequest.getResponseObjectList(appSession,
            "/flow/engine/listFlowInstNodes",
            paramMap,NodeInstance.class);
    }

    @Override
    public void stopAndChangeInstance(String flowInstId,String userCode,String desc) throws Exception{
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("flowInstId",flowInstId);
        paramMap.put("userCode",userCode);
        paramMap.put("desc",desc);
        RestfulHttpRequest.formPost(appSession,
                "/flow/manager/stopAndChangeInstance",paramMap);
    }

    @Override
    public String reStartFlow(String flowInstId,String userCode) {
        return RestfulHttpRequest.jsonPut(appSession,
            "/flow/manager/reStartFlow/"+flowInstId+"/"+userCode,null);
    }

    @Override
    public String stopInstance(String flowInstId,String userCode) {
        return RestfulHttpRequest.jsonPut(appSession,
            "/flow/manager/stopInstance/"+flowInstId+"/"+userCode,null);
    }

}
