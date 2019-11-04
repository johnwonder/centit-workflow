package com.centit.workflow.dao;

import com.alibaba.fastjson.JSONArray;
import com.centit.framework.core.dao.CodeBook;
import com.centit.framework.jdbc.dao.BaseDaoImpl;
import com.centit.framework.jdbc.dao.DatabaseOptUtils;
import com.centit.support.database.orm.OrmDaoUtils;
import com.centit.support.database.utils.PageDesc;
import com.centit.support.database.utils.QueryAndNamedParams;
import com.centit.support.database.utils.QueryUtils;
import com.centit.workflow.po.FlowInfo;
import com.centit.workflow.po.FlowInfoId;
import com.centit.workflow.po.LastVersionFlowDefine;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class FlowInfoDao extends BaseDaoImpl<FlowInfo,FlowInfoId> {
    public Map<String, String> getFilterField() {
        if( filterField == null){
            filterField = new HashMap<>();
            filterField.put("flowCode" , "flowCode= :flowCode");
            filterField.put("version" , "version= :version");
            filterField.put("flowName" , CodeBook.LIKE_HQL_ID);
            filterField.put("flowState" , CodeBook.LIKE_HQL_ID);
            filterField.put("flowDesc" , CodeBook.LIKE_HQL_ID);
            filterField.put("optId" , CodeBook.EQUAL_HQL_ID);
            filterField.put(CodeBook.ORDER_BY_HQL_ID , "version DESC,flowPublishDate DESC,flowCode DESC ");
        }
        return filterField;
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public long getLastVersion(String flowCode){
        String sql = "select max(t.VERSION) from WF_FLOW_DEFINE t where t.FLOW_CODE = ?";
        return this.getJdbcTemplate().queryForObject(sql,
                new Object[]{flowCode} ,Long.class);
    }

    @Transactional(propagation= Propagation.MANDATORY)
    public long getNextNodeId(){
        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public long getNextTransId(){
        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public long getNextStageId(){
        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public long getNextRoleId(){
        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public long getNextVariableDefId(){
        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
//        return DatabaseOptUtils.getSequenceNextValue(this,"S_FLOWDEFNO");
    }

    @Transactional(propagation= Propagation.MANDATORY)
    public FlowInfo getObjectWithReferences(FlowInfoId flowInfoId){
        return jdbcTemplate.execute(
          (ConnectionCallback<FlowInfo>) conn ->
            OrmDaoUtils.getObjectWithReferences(conn, flowInfoId, FlowInfo.class));
      /*FlowInfo flowInfo = super.getObjectById(flowInfoId);
        return super.fetchObjectReferences(flowInfo);*/
    }

    @SuppressWarnings({ "unchecked", "deprecation" })
    @Transactional(propagation= Propagation.MANDATORY)
    public List<FlowInfo> getAllLastVertionFlows(Map<String,Object> filterMap){

        String sql =  "SELECT * FROM F_V_LASTVERSIONFLOW WHERE 1=1 [:optId| and opt_id = :optId ] " ;
        QueryAndNamedParams sqlAndParams = QueryUtils.translateQuery(sql,filterMap);
        return this.listObjectsBySql(sqlAndParams.getQuery(),sqlAndParams.getParams());
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public List<FlowInfo> getAllVersionFlowsByCode(String wfCode, PageDesc pageDesc){
        return this.listObjectsByFilterAsJson("where FLOW_CODE = ? order by version desc",
                new Object[]{wfCode},pageDesc).toJavaList(FlowInfo.class);
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public FlowInfo getLastVersionFlowByCode(String flowCode){
        long lVer = getLastVersion(flowCode);
        return this.getObjectById(new FlowInfoId(lVer, flowCode));
    }
    @Transactional(propagation= Propagation.MANDATORY)
    public FlowInfo getFlowDefineByID(String flowCode, Long version)
    {
        return this.getObjectWithReferences(new FlowInfoId(version, flowCode));
    }

    @SuppressWarnings("unchecked")
    @Transactional(propagation= Propagation.MANDATORY)
    public List<FlowInfo> getFlowsByState(String wfstate)
    {
        String sql="SELECT * FROM F_V_LASTVERSIONFLOW WHERE FLOW_STATE = ? ORDER BY VERSION";
        return  this.getJdbcTemplate().query(sql,
                new Object[]{wfstate} ,new BeanPropertyRowMapper<FlowInfo>(FlowInfo.class));
    }

    @SuppressWarnings("deprecation")
    @Transactional(propagation= Propagation.MANDATORY)
    public List<FlowInfo> getAllLastVertionFlows(
            Map<String, Object> filterMap, PageDesc pageDesc) {

        String sql =  "select VERSION,FLOW_CODE,FLOW_NAME,FLOW_CLASS,FLOW_STATE,FLOW_DESC,FLOW_XML_DESC," +
                "FLOW_PUBLISH_DATE,OPT_ID,TIME_LIMIT " +
                " from F_V_LASTVERSIONFLOW " +
            " where 1=1  [:(like)flowName| and FLOW_NAME like :flowName ] " +
            " [:flowCode| and FLOW_Code = :flowCode ] order by OS_ID ,VERSION desc " ;
        QueryAndNamedParams queryAndNamedParams = QueryUtils.translateQuery(sql,filterMap);
        JSONArray dataList = DatabaseOptUtils.listObjectsByNamedSqlAsJson(this,
                queryAndNamedParams.getQuery(),queryAndNamedParams.getParams(),pageDesc);
        List<LastVersionFlowDefine> ls = new ArrayList<>();
        if(dataList != null) {
            ls = JSONArray.parseArray(dataList.toJSONString(),LastVersionFlowDefine.class);
        }
        List<FlowInfo>all=new ArrayList<FlowInfo>();
        if(ls != null && ls.size() > 0) {
            for (LastVersionFlowDefine s : ls) {
                all.add(s.toWfFlowDefine());
            }
        }
        return all;
    }

    @Transactional(propagation= Propagation.MANDATORY)
    public void deleteObjectByFlowCode(String flowCode){
        String sql="delete from wf_flow_define where flow_code=?";
        this.getJdbcTemplate().update(sql,flowCode);
    }

}
