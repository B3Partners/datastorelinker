<%-- 
    Document   : main
    Created on : 7-okt-2010, 20:00:35
    Author     : Erik van de Pol
--%>

<%@include file="/WEB-INF/jsp/commons/taglibs.jsp" %>
<%@include file="/WEB-INF/jsp/commons/urls.jsp" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>


<script type="text/javascript" class="ui-layout-ignore">
    $(document).ready(function() {
        var newUpdateInputCommonDialogOptions = $.extend({}, defaultDialogOptions, {
            width: Math.floor($('body').width() * .65),
            height: Math.floor($('body').height() * .60),
            resize: function(event, ui) {
                $("#inputContainer").layout().resizeAll();
                if ($("#inputSteps").length != 0) // it exists
                    $("#inputSteps").layout().resizeAll();
            },
            close: function(event, ui) {
                $("#uploader").uiloadDestroy();
                defaultDialogClose(event, ui);
            }
        });

        $("#createInputDB").click(function() {
            ajaxOpen({
                url: "${inputUrl}",
                event: "createDatabaseInput",
                containerId: "inputContainer",
                openInDialog: true,
                dialogOptions: $.extend({}, newUpdateInputCommonDialogOptions, {
                    title: I18N.newDatabaseInput
                })
            });

            return defaultButtonClick(this);
        });

        $("#updateInput").click(function() {
            ajaxOpen({
                url: "${inputUrl}",
                formSelector: "#createUpdateProcessForm",
                event: "update",
                containerId: "inputContainer",
                openInDialog: true,
                dialogOptions: $.extend({}, newUpdateInputCommonDialogOptions, {
                    title: I18N.editInput
                })
            });

            return defaultButtonClick(this);
        });

        $("#deleteInput").click(function() {
            if (!isFormValidAndContainsInput("#createUpdateProcessForm"))
                return defaultButtonClick(this);

            $("<div></div>").html(I18N.deleteInputAreYouSure)
                .attr("id", "inputContainer").appendTo(document.body);

            $("#inputContainer").dialog($.extend({}, defaultDialogOptions, {
                title: I18N.deleteInput,
                buttons: {
                    "<fmt:message key="no"/>": function() {
                        $(this).dialog("close");
                    },
                    "<fmt:message key="yes"/>": function() {
                        $.blockUI(blockUIOptions);
                        ajaxOpen({
                            url: "${inputUrl}",
                            formSelector: "#createUpdateProcessForm",
                            event: "delete",
                            containerSelector: "#inputListContainer",
                            ajaxOptions: {global: false}, // prevent blockUI being called 2 times. Called manually.
                            successAfterContainerFill: function() {
                                ajaxOpen({
                                    url: "${processUrl}",
                                    event: "list",
                                    containerSelector: "#processesListContainer",
                                    ajaxOptions: {global: false},
                                    successAfterContainerFill: function() {
                                        $("#inputContainer").dialog("close");
                                        $.unblockUI(unblockUIOptions);
                                    }
                                });
                            }
                        });
                    }
                }
            }));

            return defaultButtonClick(this);
        });
    });
</script>

<stripes:form partial="true" action="#">
    <div id="databaseInputHeader" class="ui-layout-north">
    </div>
    <div id="inputListContainer" class="mandatory-form-input ui-layout-center radioList ui-widget-content ui-corner-all">
        <%@include file="/WEB-INF/jsp/main/input/database/list.jsp" %>
    </div>
    <div class="ui-layout-south crudButtonsArea">
        <stripes:button id="createInputDB" name="create"/>
        <stripes:button id="updateInput" name="update"/>
        <stripes:button id="deleteInput" name="delete"/>
    </div>
</stripes:form>