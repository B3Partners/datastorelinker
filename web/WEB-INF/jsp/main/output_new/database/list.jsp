<%@include file="/WEB-INF/jsp/commons/taglibs.jsp" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<script type="text/javascript" class="ui-layout-ignore">
    $(document).ready(function() {
        initGuiInput();
    });
</script>

<div id="inputList">
    <stripes:form partial="true" action="/">
        <c:forEach var="input" items="${actionBean.inputs}" varStatus="status">
            <c:choose>
                <c:when test="${not empty actionBean.selectedOutputId and input.id == actionBean.selectedOutputId}">
                    <input type="radio" id="input${status.index}" name="selectedOutputId" value="${input.id}" class="required" checked="checked"/>
                    <script type="text/javascript" class="ui-layout-ignore">
                        $(document).ready(function() {
                            $("#inputList").parent().scrollTo(
                                $("#input${status.index}"),
                                defaultScrollToDuration,
                                defaultScrollToOptions
                            );
                        });
                    </script>
                </c:when>
                <c:otherwise>
                    <input type="radio" id="input${status.index}" name="selectedOutputId" value="${input.id}" class="required"/>
                </c:otherwise>
            </c:choose>
            <stripes:label for="input${status.index}">
                <c:out value="${input.name}"/>
            </stripes:label>
        </c:forEach>
    </stripes:form>
</div>