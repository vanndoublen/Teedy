package com.sismics.docs.core.dao;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sismics.docs.core.constant.AuditLogType;
import com.sismics.docs.core.dao.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.criteria.DocumentCriteria;
import com.sismics.docs.core.dao.dto.AuditLogDto;
import com.sismics.docs.core.dao.dto.DocumentDto;
import com.sismics.docs.core.model.jpa.AuditLog;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.QueryParam;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.util.context.ThreadLocalContext;

import jakarta.persistence.EntityManager;
import java.sql.Timestamp;
import java.util.*;

/**
 * Audit log DAO.
 * 
 * @author bgamard
 */
public class AuditLogDao {
    /**
     * Creates a new audit log.
     * 
     * @param auditLog Audit log
     * @return New ID
     */
    public String create(AuditLog auditLog) {
        // Create the UUID
        auditLog.setId(UUID.randomUUID().toString());
        
        // Create the audit log
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        auditLog.setCreateDate(new Date());
        em.persist(auditLog);
        
        return auditLog.getId();
    }
    

    public void findByCriteria(PaginatedList<AuditLogDto> paginatedList, AuditLogCriteria criteria, SortCriteria sortCriteria) {
        Map<String, Object> parameterMap = new HashMap<>();
        
        StringBuilder baseQuery = new StringBuilder("select l.LOG_ID_C c0, l.LOG_CREATEDATE_D c1, u.USE_USERNAME_C c2, l.LOG_IDENTITY_C c3, l.LOG_CLASSENTITY_C c4, l.LOG_TYPE_C c5, l.LOG_MESSAGE_C c6 from T_AUDIT_LOG l ");
        baseQuery.append(" join T_USER u on l.LOG_IDUSER_C = u.USE_ID_C ");
        List<String> queries = Lists.newArrayList();
        
        // Adds search criteria
        if (criteria.getDocumentId() != null) {
            queries.add(baseQuery + " where l.LOG_IDENTITY_C = :documentId ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select f.FIL_ID_C from T_FILE f where f.FIL_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select c.COM_ID_C from T_COMMENT c where c.COM_IDDOC_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select a.ACL_ID_C from T_ACL a where a.ACL_SOURCEID_C = :documentId) ");
            queries.add(baseQuery + " where l.LOG_IDENTITY_C in (select r.RTE_ID_C from T_ROUTE r where r.RTE_IDDOCUMENT_C = :documentId) ");
            parameterMap.put("documentId", criteria.getDocumentId());
        }
        
        if (criteria.getUserId() != null) {
            if (criteria.isAdmin()) {
                // For admin users, display all logs except ACL logs
                queries.add(baseQuery + " where l.LOG_CLASSENTITY_C != 'Acl' ");
            } else {

                queries.add(baseQuery + " where l.LOG_IDUSER_C = :userId and l.LOG_CLASSENTITY_C != 'Acl' ");
                parameterMap.put("userId", criteria.getUserId());
            }
        }
        
        QueryParam queryParam = new QueryParam(Joiner.on(" union ").join(queries), parameterMap);
        List<Object[]> l = PaginatedLists.executePaginatedQuery(paginatedList, queryParam, sortCriteria);
        
        List<AuditLogDto> auditLogDtoList = new ArrayList<>();
        for (Object[] o : l) {
            int i = 0;
            AuditLogDto auditLogDto = new AuditLogDto();
            auditLogDto.setId((String) o[i++]);
            auditLogDto.setCreateTimestamp(((Timestamp) o[i++]).getTime());
            auditLogDto.setUsername((String) o[i++]);
            auditLogDto.setEntityId((String) o[i++]);
            auditLogDto.setEntityClass((String) o[i++]);
            auditLogDto.setType(AuditLogType.valueOf((String) o[i++]));
            auditLogDto.setMessage((String) o[i++]);
            auditLogDtoList.add(auditLogDto);
        }

        paginatedList.setResultList(auditLogDtoList);
    }


    /**
     * Gets activity counts by user over time.
     *
     * @param days Number of days to look back
     * @return Map of username to activity count by day
     */
    public Map<String, Map<String, Long>> getUserActivityOverTime(int days) {
        Map<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("daysAgo", new Date(System.currentTimeMillis() - days * 86400000L));


        StringBuilder sb = new StringBuilder("select u.USE_USERNAME_C, FORMATDATETIME(l.LOG_CREATEDATE_D, 'yyyy-MM-dd') as log_date, count(l.LOG_ID_C) as count ");
        sb.append("from T_AUDIT_LOG l ");
        sb.append("join T_USER u on l.LOG_IDUSER_C = u.USE_ID_C ");
        sb.append("where l.LOG_CREATEDATE_D > :daysAgo ");
        sb.append("group by u.USE_USERNAME_C, FORMATDATETIME(l.LOG_CREATEDATE_D, 'yyyy-MM-dd') ");
        sb.append("order by log_date");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        List<Object[]> resultList = em.createNativeQuery(sb.toString())
                .setParameter("daysAgo", parameterMap.get("daysAgo"))
                .getResultList();

        Map<String, Map<String, Long>> result = new HashMap<>();
        for (Object[] row : resultList) {
            String username = (String) row[0];
            String date = (String) row[1];
            Long count = ((Number) row[2]).longValue();

            if (!result.containsKey(username)) {
                result.put(username, new HashMap<>());
            }
            result.get(username).put(date, count);
        }

        return result;
    }


    /**
     * Gets activity counts by type.
     *
     * @return Map of activity type to count
     */
    public Map<String, Long> getActivityCountByType() {
        StringBuilder sb = new StringBuilder("select l.LOG_TYPE_C, count(l.LOG_ID_C) as count ");
        sb.append("from T_AUDIT_LOG l ");
        sb.append("group by l.LOG_TYPE_C");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        List<Object[]> resultList = em.createNativeQuery(sb.toString())
                .getResultList();

        Map<String, Long> result = new HashMap<>();
        for (Object[] row : resultList) {
            String type = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            result.put(type, count);
        }

        return result;
    }


    public List<Map<String, Object>> getMostActiveDocuments(int limit) {

        //get the active docs only
        EntityManager em = ThreadLocalContext.get().getEntityManager();
        List<Object[]> documentList = em.createNativeQuery(
                        "SELECT d.DOC_ID_C, d.DOC_TITLE_C FROM T_DOCUMENT d WHERE d.DOC_DELETEDATE_D IS NULL")
                .getResultList();

        //count the audit log for each doc
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] doc : documentList) {
            String documentId = (String) doc[0];
            String documentTitle = (String) doc[1];


            AuditLogCriteria criteria = new AuditLogCriteria();
            criteria.setDocumentId(documentId);


            PaginatedList<AuditLogDto> logs = PaginatedLists.create(Integer.MAX_VALUE, 0);
            findByCriteria(logs, criteria, null);


            if (logs.getResultList().size() > 0) {
                Map<String, Object> documentActivity = new HashMap<>();
                documentActivity.put("id", documentId);
                documentActivity.put("title", documentTitle);
                documentActivity.put("count", (long) logs.getResultList().size());
                result.add(documentActivity);
            }
        }


        result.sort((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")));


        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }






    /**
     * Gets recent activity with details.
     *
     * @param limit Maximum number of activities to return
     * @return List of activity details
     */
    public List<Map<String, Object>> getRecentActivity(int limit) {
        StringBuilder sb = new StringBuilder("select l.LOG_ID_C, l.LOG_CREATEDATE_D, u.USE_USERNAME_C, ");
        sb.append("l.LOG_TYPE_C, l.LOG_CLASSENTITY_C, l.LOG_IDENTITY_C, l.LOG_MESSAGE_C, ");
        sb.append("d.DOC_TITLE_C ");
        sb.append("from T_AUDIT_LOG l ");
        sb.append("join T_USER u on l.LOG_IDUSER_C = u.USE_ID_C ");
        sb.append("left join T_DOCUMENT d on (l.LOG_CLASSENTITY_C = 'Document' and l.LOG_IDENTITY_C = d.DOC_ID_C) ");
        sb.append("order by l.LOG_CREATEDATE_D desc ");
        sb.append("limit :limit");

        EntityManager em = ThreadLocalContext.get().getEntityManager();
        List<Object[]> resultList = em.createNativeQuery(sb.toString())
                .setParameter("limit", limit)
                .getResultList();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : resultList) {
            Map<String, Object> activity = new HashMap<>();
            int i = 0;
            activity.put("id", (String) row[i++]);
            activity.put("date", ((Timestamp) row[i++]).getTime());
            activity.put("username", (String) row[i++]);
            activity.put("type", (String) row[i++]);
            activity.put("entityClass", (String) row[i++]);
            activity.put("entityId", (String) row[i++]);
            activity.put("message", (String) row[i++]);
            activity.put("documentTitle", row[i]); // may be null

            result.add(activity);
        }

        return result;
    }


}
