<%@ page import="org.mskcc.cbio.portal.servlet.QueryBuilder" %>
<%
    String step3ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP3_ERROR_MSG);
%>

<div class="query_step_section" id="step3">
	<table>
		<tr>
			<td>
    		<span class="step_header">Select Patient/Case Set:</span>
			</td>
			<td>
				<select id="select_case_set" name="<%= QueryBuilder.CASE_SET_ID %>"></select>
	 		</td>
	 		<td>
	 			<a id="build_custom_case_set" onclick="promptCustomCaseSetBuilder()" title="Build a Custom Case Set Based on Clinical Attributes">
	 				Build Case Set
	 			</a>
	 		</td>
		</tr>
		<tr>
			<td></td>
			<td><span style="font-size:95%; color:black">(Tip:  Hover your mouse over a case set to view a description.)</span>
			</td>
		</tr>
	</table>
	
	<div id="custom_case_set_dialog" title="Build a Case Set">
		<table id="case_set_dialog_header">
			<tr>
				<td id="selected_cancer_study_title" align="left">Build a Custom Case Set for: </td>
				<td id="number_of_cases_title" align="right">Number of Matching Cases: </td>
			</tr>
			<tr>
				<td id="selected_cancer_study" align="left"></td>
				<td id="number_of_cases" align="right"></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td class="custom_case_set_warning" align="right"></td>
			</tr>
		</table>
		<table id="case_set_dialog_content"></table>
		<table id="case_set_dialog_footer">			
   			<tr>
   				<td>
					<button id="cancel_custom_case_set" title="Cancel">Cancel</button>
				</td>
				<td>
					<button id="submit_custom_case_set" class="tabs-button" title="Use this case set">Build</button>
				</td>
			</tr>
		</table>
	</div>
    <div id="mutsig_dialog" title="Recurrently Mutated Genes" style="font-size: 11px; .ui-dialog {padding: 0em;};">
        <img id='loader-img' src="images/ajax-loader.gif"/>
        <table class="MutSig">
            <thead>
                <tr>
                    <th>Gene Symbol</th>
                    <th>Num Mutations</th>
                    <th>Q-Value</th>
                    <th><input class="checkall" type="checkbox"></td>
                </tr>
            </thead>
            <tbody>
            </tbody>
        </table>
		<div id="mutsig_dialog_footer" style="float: right;">
					<button id="cancel_mutsig" title="Cancel">Cancel</button>
					<button id="select_mutsig" class="tabs-button" title="Use these mutsig genes">Select</button>
		</div>
    </div>
    <div id="gistic_dialog" title="Regions of Interest" style="font-size:11px; text-align:left; .ui-dialog {padding:0em;};">
        <span id='gistic_message_box' style='display:none; color:red; font-weight:bold; font-size:1.2em;'></span>
        <div id='gistic_loading'>loading...</div>
        <table id="gistic_table"></table>
        <div id="gistic_dialog_footer">
            <button style="float: right;" id="gistic_close" onclick="Gistic.UI.updateGenes(); Gistic.dialog_el.dialog('close');" class="tabs-button" title="Use these ROI genes">Sync Selected Genes With Query</button>
        </div>
    </div>
    <style type='text/css'>
        .gistic_gene {
            padding: 3px;
            border-radius:5px;
            cursor:pointer;
        }
        .gistic_gene:hover {
            padding: 1px;
            border: 2px solid #1974b8;
        }
        .gistic_selected_gene {
            #background:#DDD;
            font-weight:bold;
        }
    </style>
    <script type='text/javascript'>
    // set up modal dialog box for gistic table
    $('#gistic_dialog').dialog( {autoOpen: false,
            modal: true,
            overflow: 'hidden',
            minWidth: 700,
            //resizable: false,
            height: 500,
            // width: 'auto',
            open: function() { 
                // sets the scrollbar to the top of the table
                $(this).scrollTop(0);
                return;

                // workaround to prevent auto focus
                //$(this).add('input').blur();
            },
    });
    </script>
<%
String customCaseListStyle = "none";
// Output step 3 form validation error
if (step3ErrorMsg != null) {
    out.println("<div class='ui-state-error ui-corner-all' style='margin-top:4px; padding:5px;'>"
            + "<span class='ui-icon ui-icon-alert' style='float: left; margin-right: .3em;'></span>"
            + "<strong>" + step3ErrorMsg + "</strong>");
    customCaseListStyle = "block";
}
%>
    <div id='custom_case_list_section' style="display:<%= customCaseListStyle %>;">
        <p><span style="font-size:80%">Enter case IDs below:</span></p>
<textarea id='custom_case_set_ids' name='<%= QueryBuilder.CASE_IDS %>' rows=6 cols=80><%
    if (localCaseIds != null) {
            out.print (localCaseIds.trim());
        }
%></textarea>
    </div>

<%
if (step3ErrorMsg != null) {
    out.println("</div>");
}
%>
</div>
