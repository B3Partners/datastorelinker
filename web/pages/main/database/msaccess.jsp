<%-- 
    Document   : msaccess
    Created on : 12-mei-2010, 14:52:28
    Author     : Erik van de Pol
--%>
<%@include file="/pages/commons/taglibs.jsp" %>

<c:set var="dbType" value="MSACCESS"/>

<script type="text/javascript">
    $(document).ready(function() {
        <c:choose>
            <c:when test="${not empty actionBean.selectedDatabase and actionBean.selectedDatabase.type == dbType}">
                $("#msaccessurl").val("<c:out value="${actionBean.selectedDatabase.url}"/>");
                $("#msaccesssrs").val("<c:out value="${actionBean.selectedDatabase.srs}"/>");
                $("#msaccesscolX").val("<c:out value="${actionBean.selectedDatabase.colX}"/>");
                $("#msaccesscolY").val("<c:out value="${actionBean.selectedDatabase.colY}"/>");
            </c:when>
            <c:otherwise>
                $("#msaccessurl").val("*.mdb");
                $("#msaccesssrs").val("EPSG:28992");
                $("#msaccesscolX").val("POINT_X");
                $("#msaccesscolY").val("POINT_Y");
            </c:otherwise>
        </c:choose>

        $("#msaccessForm").validate(defaultValidateOptions);
    });
</script>

<stripes:form id="msaccessForm" beanclass="nl.b3p.datastorelinker.gui.stripes.DatabaseAction">
    <stripes:hidden name="dbType" value="${dbType}" />
    <stripes:wizard-fields/>
    <table>
        <tbody>
            <tr>
                <td><stripes:label name="url" for="msaccessurl"/></td>
                <td><stripes:text id="msaccessurl" name="url" class="required"/></td>
            </tr>
            <tr>
                <td><stripes:label name="srs" for="msaccesssrs"/></td>
                <td><stripes:text id="msaccesssrs" name="srs" class="required"/></td>
            </tr>
            <tr>
                <td><stripes:label name="colX" for="msaccesscolX"/></td>
                <td><stripes:text id="msaccesscolX" name="colX" class="required"/></td>
            </tr>
            <tr>
                <td><stripes:label name="colY" for="msaccesscolY"/></td>
                <td><stripes:text id="msaccesscolY" name="colY" class="required"/></td>
            </tr>
        </tbody>
    </table>
</stripes:form>
