<%@include file="/WEB-INF/jsp/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/commons/urls.jsp" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<script type="text/javascript" class="ui-layout-ignore">
    $(document).ready(function() {
        initUsers();

        createDefaultVerticalLayout($("#authUsersAdmin"), $.extend({}, defaultLayoutOptions, {
            south__size: 50,
            south__minSize: 50
        }));
    });
</script>

<div id="authUsersAdmin" style="height: 100%">
    <stripes:form id="authUsersForm" beanclass="nl.b3p.datastorelinker.gui.stripes.AuthorizationAction">
        <%@include file="/WEB-INF/jsp/main/auth/users/main.jsp" %>
    </stripes:form>
</div>