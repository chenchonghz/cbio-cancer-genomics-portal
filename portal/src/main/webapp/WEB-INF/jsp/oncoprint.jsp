<%@ page import="org.apache.commons.lang.StringEscapeUtils" %>
<div id="oncoprint" style="padding-top:10px; padding-bottom:10px; padding-left:10px; border: 1px solid #CCC;">
    <img id="loader_img" src="images/ajax-loader.gif"/>
    <div style="display:none;" id="everything">
        <h4>OncoPrint
            <small>(<a href="faq.jsp#what-are-oncoprints">What are OncoPrints?</a>)</small>
        </h4>

        <form id="oncoprintForm" action="oncoprint_converter.svg" enctype="multipart/form-data" method="POST"
              onsubmit="this.elements['xml'].value=oncoprint.getOncoPrintBodyXML(); return true;" target="_blank">
            <input type="hidden" name="xml">
            <input type="hidden" name="longest_label_length">
            <input type="hidden" name="format" value="svg">
            <p>Download OncoPrint:&nbsp;&nbsp;&nbsp;<input type="submit" value="SVG"></p>
        </form>

        <div id="oncoprint_controls">
            <style>
                .onco-customize {
                    color:#2153AA; font-weight: bold; cursor: pointer;
                }
                .onco-customize:hover { text-decoration: underline; }
            </style>
            <p onclick="$('#oncoprint_controls table').toggle(); $('#oncoprint_controls .triangle').toggle();"
               style="margin-bottom: 0px;">
                <span class='triangle ui-icon ui-icon-triangle-1-e' style='float:left;'></span>
                <span class='triangle ui-icon ui-icon-triangle-1-s' style='float:left; display:none;'></span>
                <span class='onco-customize' style="">Customize</span>
            </p>
            <table style="padding-left:13px; padding-top:5px; display:none;">
                <tr>
                    <td><input type='checkbox' onclick='oncoprint.toggleUnaltered();'>Only Show Altered Cases</td>
                    <td><input type='checkbox' onclick='if ($(this).is(":checked")) {oncoprint.defaultSort();} else {oncoprint.memoSort();}'>Unsort Cases</td>
                </tr>

                <tr>
                    <td style="padding-right: 15px;"><span>Zoom</span><div id="zoom" style="display: inline-table;"></div></td>
                    <td><input type='checkbox' onclick='oncoprint.toggleWhiteSpace();'>Remove Whitespace</td>
                </tr>
            </table>
        </div>
        <div id="oncoprint_body">
            <script type="text/javascript" src="js/oncoprint.js?b38533a455c5"></script>
            <script type="text/javascript" src="js/d3.v2.min.js"></script>
            <%--todo: we may want to import d3 globally but for now, it's just here--%>

            <script type="text/javascript">
                var oncoPrintParams = {
                    cancer_study_id: "<%=cancerTypeId%>",
                    case_set_str: "<%=StringEscapeUtils.escapeHtml(OncoPrintUtil.getCaseSetDescription(caseSetId, caseSets))%>",
                    num_cases_affected: "<%=dataSummary.getNumCasesAffected()%>",
                    percent_cases_affected: "<%=OncoPrintUtil.alterationValueToString(dataSummary.getPercentCasesAffected())%>",
                    vis_key: true,
                    customize: true
                };

                var geneDataQuery = {
                    genes: genes,
                    samples: samples,
                    geneticProfileIds: geneticProfiles,
                    z_score_threshold: <%=zScoreThreshold%>,
                    rppa_score_threshold: <%=rppaScoreThreshold%>
                };

                var oncoprint;      // global
                $.post(DataManagerFactory.getGeneDataJsonUrl(), geneDataQuery, function(data) {

                    oncoPrintParams['data'] = data;

                    oncoprint = Oncoprint($('#oncoprint_body')[0], oncoPrintParams);

                    oncoprint.draw();
                    var geneDataManager = DataManagerFactory.getGeneDataManager();
                    geneDataManager.fire(data);

                    $('#oncoprint #loader_img').hide();
                    $('#oncoprint #everything').show();
                });
            </script>
        </div>

        <div id="oncoprint_legend"></div>
    </div>
</div>
