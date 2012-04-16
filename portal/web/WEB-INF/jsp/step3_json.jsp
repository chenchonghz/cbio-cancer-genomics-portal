<%
    String step3ErrorMsg = (String) request.getAttribute(QueryBuilder.STEP3_ERROR_MSG);
%>

<!-- Include Custom Case Set Builder Javascript -->
<script type="text/javascript" src="js/customCaseSet.js"></script>

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
	 			<a id="build_custom_case_set" onclick="promptCustomCaseSetBuilder()">
	 				Build a Custom Case Set Based on Clinical Attributes
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
				<td id="selected_cancer_study"></td>
				<td id="number_of_cases"></td>
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
            out.print (localCaseIds);
        }
%>
</textarea>
    </div>

<%
if (step3ErrorMsg != null) {
    out.println("</div>");
}
%>
</div>

