<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.model.GeneSet" %>
<div class="query_step_section">
    <span class="step_header">Enter Gene Set:</span>

    <script language="javascript" type="text/javascript">

    function popitup(url) {
        newwindow=window.open(url,'OncoSpecLangInstructions','height=1000,width=1000,left=400,top=0,scrollbars=yes');
        if (window.focus) {newwindow.focus()}
        return false;
    }
    </script>
    <span style="float:right">
        <a href="onco_query_lang_desc.jsp" onclick="return popitup('onco_query_lang_desc.jsp')">Advanced:  Onco Query Language (OQL)</a>
    </span>
    <P/>
<textarea rows='5' cols='80' id='gene_list' name='<%= QueryBuilder.GENE_LIST %>'>
</textarea>

    <p>Or Select from Example Gene Sets:</p>
    <p><select id="select_gene_set" name="<%= QueryBuilder.GENE_SET_CHOICE %>"></select>
</div>