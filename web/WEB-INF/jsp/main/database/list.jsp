<%-- 
    Document   : databaseInList
    Created on : 6-mei-2010, 14:30:21
    Author     : Erik van de Pol
--%>
<%@include file="/WEB-INF/jsp/commons/taglibs.jsp" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>


<script type="text/javascript" class="ui-layout-ignore">
    $(document).ready(function() {
        selectFirstRadioInputIfPresentAndNoneSelected($("#databasesList input:radio"));
        $("#databasesList").buttonset();
    });
</script>

<div id="databasesList">
    <stripes:form partial="true" action="/">
        <c:forEach var="database" items="${actionBean.databases}" varStatus="status">
            <c:choose>
                <c:when test="${not empty actionBean.selectedDatabaseId and database.id == actionBean.selectedDatabaseId}">
                    <input type="radio" id="database${status.index}" name="selectedDatabaseId" value="${database.id}" class="required" checked="checked" />
                    <script type="text/javascript" class="ui-layout-ignore">
                        $(document).ready(function() {
                            $("#databasesList").parent().scrollTo(
                                $("#database${status.index}"),
                                defaultScrollToDuration,
                                defaultScrollToOptions
                            );
                        });
                    </script>
                </c:when>
                <c:otherwise>
                    <input type="radio" id="database${status.index}" name="selectedDatabaseId" value="${database.id}" class="required"/>
                </c:otherwise>
            </c:choose>
            <stripes:label for="database${status.index}">
                <c:out value="${database.name}"/>
            </stripes:label>
        </c:forEach>
    </stripes:form>
</div>