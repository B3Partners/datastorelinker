<%-- 
    Document   : create
    Created on : 23-apr-2010, 19:25:55
    Author     : Erik van de Pol
--%>
<%@include file="/pages/commons/taglibs.jsp" %>

<script type="text/javascript">
    $(function() {
        $("#inputList").buttonset();
        $("#outputList").buttonset();

        $("#createProcessBackButton").button();
        $("#createProcessNextButton").button();

        $("#createInputDB").button();
        $("#createInputFile").button();
        $("#updateInput").button();
        $("#deleteInput").button();

        $("#createOutput").button();
        $("#updateOutput").button();
        $("#deleteOutput").button();

        $("#createProcessForm").formwizard( {
            //form wizard settings
            historyEnabled : false,
            formPluginEnabled : true,
            validationEnabled : false,
            focusFirstInput : true,
            textNext : "Volgende",
            textBack : "Vorige",
            textSubmit : "Voltooien",
            inAnimation : "slideDown", //"show",
            outAnimation : "slideUp" //"hide"
        }, {
            //validation settings
        }, {
            // form plugin settings
            /*target: "#ui-tabs-1",
            success: function() {
                log("success!");
                createProcessDialog.dialog("close");
            },*/
            beforeSend: function() {
                // beetje een lelijke hack, maar werkt wel mooi:
                ajaxFormEventInto("#createProcessForm", "createComplete", "#processesList", function() {
                    log("success!");
                    createProcessDialog.dialog("close");
                    $("#processesList").buttonset();
                });
                return false;
            }
        });

        $("#createInputDB").click(function() {
            $("<div id='createInputDBContainer'/>").appendTo(document.body);

            createInputDBDialog = $("#createInputDBContainer").dialog({
                title: "Nieuwe Database-invoer...", // TODO: localization
                width: 800,
                height: 500,
                modal: true,
                close: function() {
                    log("createInputDBDialog closing");
                    if ($("#createInputForm")) {
                        $("#createInputForm").formwizard("destroy");
                    }
                    createInputDBDialog.dialog("destroy");
                    // volgende regel heel belangrijk!!
                    createInputDBDialog.remove();
                }
            });

            $.get("<stripes:url beanclass="nl.b3p.datastorelinker.gui.stripes.InputAction"/>", "createDatabaseInput", function(data) {
                $("#createInputDBContainer").html(data);
            });

            return false;
        });

        $("#createInputFile").click(function() {
            $("<div id='createInputFileContainer'/>").appendTo(document.body);

            createInputFileDialog = $("#createInputFileContainer").dialog({
                title: "Nieuwe Bestandsinvoer...", // TODO: localization
                width: 800,
                height: 500,
                modal: true,
                close: function() {
                    log("createInputDBDialog closing");
                    if ($("#createInputForm")) {
                        $("#createInputForm").formwizard("destroy");
                    }
                    createInputFileDialog.dialog("destroy");
                    // volgende regel heel belangrijk!!
                    createInputFileDialog.remove();
                }
            });

            $.get("<stripes:url beanclass="nl.b3p.datastorelinker.gui.stripes.InputAction"/>", "createFileInput", function(data) {
                $("#createInputFileContainer").html(data);
            });

            return false;
        });

        $("#createOutput").click(function() {
            $("<div id='createOutputContainer'/>").appendTo(document.body);

            createOutputDialog = $("#createOutputContainer").dialog({
                title: "Nieuwe Uitvoer Database...", // TODO: localization
                width: 700,
                height: 600,
                modal: true,
                buttons: { // TODO: localize button name:
                    "Voltooien" : function() {
                        // is deze button wel disabled totdat dialog alles ready is
                        ajaxFormEventInto("#postgisForm", "createComplete", "#outputList", function() {
                            createOutputDialog.dialog("close");
                            $("#outputList").buttonset();
                        }, "<stripes:url beanclass="nl.b3p.datastorelinker.gui.stripes.OutputAction"/>");
                    }
                },
                close: function() {
                    log("createOutputContainer closing");
                    createOutputDialog.dialog("destroy");
                    // volgende regel heel belangrijk!!
                    createOutputDialog.remove();
                },
                beforeclose: function(event, ui) {
                    // TODO: check connection. if bad return false
                    return true;
                }
            });

            $.get("<stripes:url beanclass="nl.b3p.datastorelinker.gui.stripes.OutputAction"/>", "create", function(data) {
                $("#createOutputContainer").html(data);
            });
        })

    });

</script>

<stripes:form id="createProcessForm" beanclass="nl.b3p.datastorelinker.gui.stripes.ProcessAction">
    <div id="SelecteerInvoer" class="step">
        <h1>Selecteer bestand- of database-invoer:</h1>
        <div id="inputList" class="radioList">
            <%@include file="/pages/main/input/list.jsp" %>
        </div>
        <div>
            <stripes:button id="createInputDB" name="createInputDB"/>
            <stripes:button id="createInputFile" name="createInputFile"/>
            <stripes:button id="updateInput" name="update"/>
            <stripes:button id="deleteInput" name="delete"/>
        </div>
    </div>
    <div id="SelecteerUitvoer" class="step">
        <h1>Selecteer database om naar uit te voeren:</h1>
        <div id="outputList" class="radioList">
            <%@include file="/pages/main/output/list.jsp" %>
        </div>
        <div>
            <stripes:button id="createOutput" name="create"/>
            <stripes:button id="updateOutput" name="update"/>
            <stripes:button id="deleteOutput" name="delete"/>
        </div>
    </div>
    <!--div id="secondStep" class="step">
        <h1>step 2 - branch step</h1>
        <input  type="text" value="" /><br />
        <input  type="text" value="" /><br />
        <input  type="text" value="" /><br />
        <select  class="link" >
            <option value="" >Choose the step to go to...</option>
            <option value="thirdStep" >Go to Step3</option>
            <option value="fourthStep" >Go to Step4</option>
        </select><br />
    </div>
    <div id="thirdStep" class="step submit_step">
        <h1>step 3 - submit step</h1>
        <input  type="text" value="" /><br />
        <input  type="text" value="" class="required"/><br />
    </div>
    <div id="fourthStep" class="step">
        <h1>step 4</h1>
        <input  type="text" value="" /><br />
        <input  type="text" name="email" class="required email" /><br />
    </div>
    <div id="lastStep" class="step">
        <h1>step 5 - last step</h1>
        <input  type="text" value="" /><br />
        <input  type="text" value="" /><br />
    </div-->
    <div class="wizardButtonsArea">
        <stripes:reset id="createProcessBackButton" name="resetDummyName"/>
        <stripes:submit id="createProcessNextButton" name="createComplete"/>
    </div>
</stripes:form>