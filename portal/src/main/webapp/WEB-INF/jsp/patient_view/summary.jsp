<%@ page import="org.mskcc.cbio.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.cbio.cgds.dao.DaoCase" %>
<%@ page import="org.mskcc.cbio.cgds.model.Case" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="org.json.simple.JSONValue" %>
<%@ page import="org.mskcc.cbio.portal.util.SkinUtil" %>

<style type="text/css" title="currentStyle">
#genomic-overview-tip {
    position : absolute;
    border : 1px solid gray;
    background-color : #efefef;
    padding : 3px;
    z-index : 1000;
    max-width : 300px;
}
.ui-tooltip, .qtip{
	position: absolute;
	left: -10000em;
	top: -10000em;
 
	max-width: 600px; /* Change this? */
	min-width: 50px; /* ...and this! */
}
</style>

<%
String jsonCaseIds = "[]";
if (mutationProfile!=null && cnaProfile!=null) {
    List<Case> cases = DaoCase.getAllCaseIdsInCancer(cancerStudy.getInternalId());
    List<String> caseIds = new ArrayList<String>(cases.size());
    for (Case c : cases) {
        caseIds.add(c.getCaseId());
    }
    jsonCaseIds = JSONValue.toJSONString(caseIds);
}
String linkToCancerStudy = SkinUtil.getLinkToCancerStudyView(cancerStudy.getCancerStudyStableId());
%>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript" src="js/patient-view/genomic-overview.js"></script>
<script type="text/javascript" src="js/cancer-study-view/scatter-plot-mut-cna.js"></script>
<script type="text/javascript" src="js/cancer-study-view/load-clinical-data.js"></script>
<script type="text/javascript">
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        $('#mutation_summary_wrapper_table').hide();
        $('#cna_summary_wrapper_table').hide();
        if (!geObs.hasMut||!geObs.hasCna) $('#mut-cna-scatter').hide();
        initGenomicsOverview();
        //initMutCnaScatterDialog();
        if (geObs.hasMut&&geObs.hasCna) {
            loadMutCnaAndPlot("mut-cna-scatter");
            addMutCnaPlotTooltip("mut-cna-scatter");
        }
            
    });

    function initGenomicsOverview() {
        var chmInfo = new ChmInfo();
        var config = new GenomicOverviewConfig((geObs.hasMut?1:0)+(geObs.hasCna?1:0),$("#td-content").width()-(geObs.hasMut&&geObs.hasCna?150:0));
        config.cnTh = [<%=genomicOverviewCopyNumberCnaCutoff[0]%>,<%=genomicOverviewCopyNumberCnaCutoff[1]%>];
        var paper = createRaphaelCanvas("genomics-overview", config);
        plotChromosomes(paper,config,chmInfo);
        if (geObs.hasMut) {
            geObs.subscribeMut(function(){
                var muts = $('#mutation_table').dataTable().fnGetData();
                plotMuts(paper,config,chmInfo,geObs.hasCna?1:0,muts,mutTableIndices['chr'],mutTableIndices['start'],mutTableIndices['end'],mutTableIndices['id'],geObs.hasCna);
            });
        }
        
        if (geObs.hasCna) {
            plotCopyNumberOverview(paper,config,chmInfo,geObs.hasMut);
        }
    }
    
    function plotCopyNumberOverview(paper,config,chmInfo,hasMut) {
        var params = {
            <%=CnaJSON.CMD%>:'<%=CnaJSON.GET_SEGMENT_CMD%>',
            <%=PatientView.PATIENT_ID%>:'<%=patient%>'
        };

        $.post("cna.json", 
            params,
            function(segs){
                plotCnSegs(paper,config,chmInfo,0,segs,1,2,3,5,hasMut);
            }
            ,"json"
        );
    }
    
    function initMutCnaScatterDialog() {
        $('#mut_cna_scatter_dialog').dialog({autoOpen: false,
            modal: true,
            minHeight: 200,
            maxHeight: 600,
            height: 550,
            minWidth: 300,
            width: 600
            });
    }

    var mutCnaScatterDialogLoaded = false;
    function openMutCnaScatterDialog() {
        if (!mutCnaScatterDialogLoaded) {
            if (mutationProfileId==null) {
                alert('no mutation data');
                return;
            }
                
            if (cnaProfileId==null) {
                alert('no cna data');
                return;
            }
            
            $('#mut_cna_more_plot_msg').hide();
            
            loadMutCnaAndPlot('mut-cna-scatter-plot','case-id-div');
            
            mutCnaScatterDialogLoaded = true;
        }
        
        //$('#mut_cna_scatter_dialog').dialog('open');
    }
    
    function loadMutCnaAndPlot(scatterPlotDiv,caseIdDiv) {
        loadMutCountCnaFrac(<%=jsonCaseIds%>,
            <%=mutationProfileStableId==null%>?null:'<%=mutationProfileStableId%>',
            <%=cnaProfileStableId==null%>?null:'<%=cnaProfileStableId%>',
            function(dt){
                var maxMut = dt.getColumnRange(1).max;
                var vLog = maxMut>1000;
                $('#mut-cna-haxis-log').attr('checked',true);
                scatterPlotMutVsCna(dt,false,vLog,scatterPlotDiv,caseIdDiv);

                $('#mut-cna-config').show();

                $(".mut-cna-axis-log").change(function() {
                    var hLog = $('#mut-cna-haxis-log').is(":checked");
                    var vLog = $('#mut-cna-vaxis-log').is(":checked");
                    scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv);
                });
                $('#mut_cna_more_plot_msg').show();
            }
        );
    }
    
    function addMutCnaPlotTooltip(scatterPlotDiv) {
        var params = {
            content: $('#mut_cna_scatter_dialog').remove(),
            hide: { fixed: true, delay: 100 },
            style: { classes: 'ui-tooltip-light ui-tooltip-rounded' },
            position: {my:'top right',at:'top right'},
            events: {
                render: function(event, api) {
                    $('.ui-tooltip').css('max-width',800);
                    openMutCnaScatterDialog();
                }
            }
        }
        $('#'+scatterPlotDiv).qtip(params);
    }
    
    function scatterPlotMutVsCna(dt,hLog,vLog,scatterPlotDiv,caseIdDiv) {
        var scatter = plotMutVsCna(null,scatterPlotDiv,caseIdDiv,dt,caseId,2,1,null,hLog,vLog);
        google.visualization.events.addListener(scatter, 'select', function(e){
            var s = scatter.getSelection();
            if (s.length>1) return;
            if (caseIdDiv) {
                var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
                $('#case-id-div').html(formatPatientLink(caseId));
            }
        });
        for (var i=0, rows = dt.getNumberOfRows(); i<rows; i++) {
            if (dt.getValue(i,0)===caseId) {
                scatter.setSelection([{'row': i}]);
                if (caseIdDiv) {
                    $('#case-id-div').html(formatPatientLink(caseId));
                }
                break;
            }
        }
    }
</script>


<%if(showPlaceHoder){%>
<br/>Clinical timeline goes here...
<br/><br/>
<%}%>

<%if(showGenomicOverview){%>
<table>
    <tr>
        <td><div id="genomics-overview"></div></td>
        <td><div id="mut-cna-scatter"><img src="images/ajax-loader.gif"/></div></td>
    </tr>
</table>

<div id="mut_cna_scatter_dialog" title="Drugs" style="width:600; height:600;font-size: 11px; text-align: left;.ui-dialog {padding: 0em;};">
    <%@ include file="../cancer_study_view/mut_cna_scatter_plot.jsp" %>
    <p id='mut_cna_more_plot_msg'><sup>*</sup>One dot in this plot represents a case/patient in <a href='<%=linkToCancerStudy%>'><%=cancerStudy.getName()%></a>.<p>
</div>
<%}%>
        
<%if(showMutations){%>
<div id="mutation_summary_wait"><img src="images/ajax-loader.gif"/> Loading mutations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="mutation_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="mutation_summary_table">
                <%@ include file="mutations_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>

<%if(showCNA){%>
<div id="cna_summary_wait"><img src="images/ajax-loader.gif"/> Loading copy number alterations ...</div>
<table cellpadding="0" cellspacing="0" border="0" id="cna_summary_wrapper_table" width="100%">
    <tr>
        <td>
            <table cellpadding="0" cellspacing="0" border="0" class="display" id="cna_summary_table">
                <%@ include file="cna_table_template.jsp"%>
            </table>
        </td>
    </tr>
</table>
<br/>
<%}%>
