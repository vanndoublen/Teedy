package com.sismics.docs.rest.resource;

import com.google.common.base.Strings;
import com.sismics.docs.core.constant.PermType;
import com.sismics.docs.core.dao.AclDao;
import com.sismics.docs.core.dao.AuditLogDao;
import com.sismics.docs.core.dao.criteria.AuditLogCriteria;
import com.sismics.docs.core.dao.dto.AuditLogDto;
import com.sismics.docs.core.util.SecurityUtil;
import com.sismics.docs.core.util.jpa.PaginatedList;
import com.sismics.docs.core.util.jpa.PaginatedLists;
import com.sismics.docs.core.util.jpa.SortCriteria;
import com.sismics.docs.rest.constant.BaseFunction;
import com.sismics.rest.exception.ForbiddenClientException;
import com.sismics.util.JsonUtil;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Audit log REST resources.
 * 
 * @author bgamard
 */
@Path("/auditlog")
public class AuditLogResource extends BaseResource {
    /**
     * Returns the list of all logs for a document or user.
     *
     * @api {get} /auditlog Get audit logs
     * @apiDescription If no document ID is provided, logs for the current user will be returned.
     * @apiName GetAuditlog
     * @apiGroup Auditlog
     * @apiParam {String} [document] Document ID
     * @apiSuccess {String} total Total number of logs
     * @apiSuccess {Object[]} logs List of logs
     * @apiSuccess {String} logs.id ID
     * @apiSuccess {String} logs.username Username
     * @apiSuccess {String} logs.target Entity ID
     * @apiSuccess {String="Acl","Comment","Document","File","Group","Tag","User","RouteModel","Route"} logs.class Entity type
     * @apiSuccess {String="CREATE","UPDATE","DELETE"} logs.type Type
     * @apiSuccess {String} logs.message Message
     * @apiSuccess {Number} logs.create_date Create date (timestamp)
     * @apiError (client) ForbiddenError Access denied
     * @apiError (client) NotFound Document not found
     * @apiPermission user
     * @apiVersion 1.5.0
     *
     * @return Response
     */
    @GET
    public Response list(@QueryParam("document") String documentId) {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }
        
        // On a document or a user?
        PaginatedList<AuditLogDto> paginatedList = PaginatedLists.create(20, 0);
        SortCriteria sortCriteria = new SortCriteria(1, false);
        AuditLogCriteria criteria = new AuditLogCriteria();
        if (Strings.isNullOrEmpty(documentId)) {
            // Search logs for a user
            criteria.setUserId(principal.getId());
            criteria.setAdmin(SecurityUtil.skipAclCheck(getTargetIdList(null)));
        } else {
            // Check ACL on the document
            AclDao aclDao = new AclDao();
            if (!aclDao.checkPermission(documentId, PermType.READ, getTargetIdList(null))) {
                throw new NotFoundException();
            }
            criteria.setDocumentId(documentId);
        }
        
        // Search the logs
        AuditLogDao auditLogDao = new AuditLogDao();
        auditLogDao.findByCriteria(paginatedList, criteria, sortCriteria);
        
        // Assemble the results
        JsonArrayBuilder logs = Json.createArrayBuilder();
        for (AuditLogDto auditLogDto : paginatedList.getResultList()) {
            logs.add(Json.createObjectBuilder()
                    .add("id", auditLogDto.getId())
                    .add("username", auditLogDto.getUsername())
                    .add("target", auditLogDto.getEntityId())
                    .add("class", auditLogDto.getEntityClass())
                    .add("type", auditLogDto.getType().name())
                    .add("message", JsonUtil.nullable(auditLogDto.getMessage()))
                    .add("create_date", auditLogDto.getCreateTimestamp()));
        }

        // Send the response
        JsonObjectBuilder response = Json.createObjectBuilder()
                .add("logs", logs)
                .add("total", paginatedList.getResultCount());
        return Response.ok().entity(response.build()).build();
    }

    /**
     * Returns dashboard data for admin.
     *
     * @return Response
     */
    @GET
    @Path("dashboard")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDashboardData() {
        if (!authenticate()) {
            throw new ForbiddenClientException();
        }


        checkBaseFunction(BaseFunction.ADMIN);

        AuditLogDao auditLogDao = new AuditLogDao();
        JsonObjectBuilder response = Json.createObjectBuilder();

        // Get user activity over time (last 30 days)
        Map<String, Map<String, Long>> userActivity = auditLogDao.getUserActivityOverTime(30);
        JsonObjectBuilder userActivityJson = Json.createObjectBuilder();
        for (Map.Entry<String, Map<String, Long>> entry : userActivity.entrySet()) {
            JsonObjectBuilder dateData = Json.createObjectBuilder();
            for (Map.Entry<String, Long> dateEntry : entry.getValue().entrySet()) {
                dateData.add(dateEntry.getKey(), dateEntry.getValue());
            }
            userActivityJson.add(entry.getKey(), dateData);
        }
        response.add("userActivity", userActivityJson);

        // get activity by type
        Map<String, Long> activityByType = auditLogDao.getActivityCountByType();
        JsonObjectBuilder activityByTypeJson = Json.createObjectBuilder();
        for (Map.Entry<String, Long> entry : activityByType.entrySet()) {
            activityByTypeJson.add(entry.getKey(), entry.getValue());
        }
        response.add("activityByType", activityByTypeJson);

        // get most active documents
        List<Map<String, Object>> activeDocuments = auditLogDao.getMostActiveDocuments(10);
        JsonArrayBuilder activeDocsJson = Json.createArrayBuilder();
        for (Map<String, Object> doc : activeDocuments) {
            JsonObjectBuilder docJson = Json.createObjectBuilder()
                    .add("id", (String) doc.get("id"))
                    .add("title", (String) doc.get("title"))
                    .add("count", (Long) doc.get("count"));
            activeDocsJson.add(docJson);
        }
        response.add("activeDocuments", activeDocsJson);


        // In the getDashboardData method, add:

//recent activity
        List<Map<String, Object>> recentActivity = auditLogDao.getRecentActivity(20);
        JsonArrayBuilder activityArray = Json.createArrayBuilder();
        for (Map<String, Object> activity : recentActivity) {
            JsonObjectBuilder activityJson = Json.createObjectBuilder()
                    .add("id", (String) activity.get("id"))
                    .add("date", (Long) activity.get("date"))
                    .add("username", (String) activity.get("username"))
                    .add("type", (String) activity.get("type"))
                    .add("entityClass", (String) activity.get("entityClass"))
                    .add("entityId", (String) activity.get("entityId"));

            //optional fields
            if (activity.get("message") != null) {
                activityJson.add("message", (String) activity.get("message"));
            } else {
                activityJson.add("message", JsonValue.NULL);
            }

            if (activity.get("documentTitle") != null) {
                activityJson.add("documentTitle", (String) activity.get("documentTitle"));
            } else {
                activityJson.add("documentTitle", JsonValue.NULL);
            }

            activityArray.add(activityJson);
        }
        response.add("recentActivity", activityArray);


        return Response.ok().entity(response.build()).build();
    }



}
