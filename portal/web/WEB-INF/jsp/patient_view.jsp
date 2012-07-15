<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>
<%@ page import="org.mskcc.cgds.model.CancerStudy" %>
<%@ page import="org.mskcc.cgds.model.GeneticProfile" %>
<%@ page import="org.mskcc.portal.util.SkinUtil" %>


<%
request.setAttribute("in-patient-view", true);
%>
<jsp:include page="global/header_above_bar.jsp" flush="true" />

<tr>
    <td>

<%
String patient = (String)request.getAttribute(PatientView.PATIENT_ID);
String patientViewError = (String)request.getAttribute(PatientView.ERROR);
String patientInfo = (String)request.getAttribute(PatientView.PATIENT_INFO);
String diseaseInfo = (String)request.getAttribute(PatientView.DISEASE_INFO);
String patientStatus = (String)request.getAttribute(PatientView.PATIENT_STATUS);
CancerStudy cancerStudy = (CancerStudy)request.getAttribute(PatientView.CANCER_STUDY);

GeneticProfile mutationProfile = (GeneticProfile)request.getAttribute(PatientView.MUTATION_PROFILE);
boolean showMutations = mutationProfile!=null;

GeneticProfile cnaProfile = (GeneticProfile)request.getAttribute(PatientView.CNA_PROFILE);
boolean showCNA = cnaProfile!=null;

String isDemoMode = request.getParameter("demo");
boolean showPlaceHoder;
if (isDemoMode!=null) {
    showPlaceHoder = isDemoMode.equalsIgnoreCase("on");
} else {
    showPlaceHoder = SkinUtil.showPlaceholderInPatientView();
}

boolean showPathways = showMutations | showCNA;
boolean showSimilarPatient = showMutations | showCNA;
boolean showGenomicOverview = showMutations | showCNA;

int numPatientInSameStudy = 0;
int numPatientInSameMutationProfile = 0;
int numPatientInSameCnaProfile = 0;

if (patientViewError!=null) {
    out.print(patient);
    out.print(": ");
    out.println();
    out.print(patientViewError);
} else {
    numPatientInSameStudy = (Integer)request.getAttribute(PatientView.NUM_CASES_IN_SAME_STUDY);
    if (mutationProfile!=null) {
        numPatientInSameMutationProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_MUTATION_PROFILE);
    }
    if (cnaProfile!=null) {
        numPatientInSameCnaProfile = (Integer)request.getAttribute(
                PatientView.NUM_CASES_IN_SAME_CNA_PROFILE);
    }
%>


<table width="100%">
    <tr>
        <td ncol="2"><b><u><%=patientInfo%></u></b></td>
    </tr>
    <tr>
        <td><%=diseaseInfo%></td>
        <td align="right"><%=patientStatus%></td>
    </tr>
</table>


<div id="patient-tabs">
    <ul>
        
    <li><a href='#summary' class='patient-tab' title='Events of Interest'>Summary</a></li>
    
    <%if(showMutations){%>
    <li><a href='#mutations' class='patient-tab' title='Mutations'>Mutations</a></li>
    <%}%>
    
    <%if(showCNA){%>
    <li><a href='#cna' class='patient-tab' title='Copy Number Alterations'>Copy Number Alterations</a></li>
    <%}%>

    <%if(showPathways){%>
    <li><a href='#pathways' class='patient-tab' title='Pathway View'>Network</a></li>
    <%}%>
    
    <%if(showSimilarPatient){%>
    <li><a href='#similar-patients' class='patient-tab' title='Similar Patients'>Similar Patients</a></li>
    <%}%>
    
    </ul>

    <div class="patient-section" id="summary">
        <%@ include file="patient_view/summary.jsp" %>
    </div>

    <%if(showMutations){%>
    <div class="patient-section" id="mutations">
        <%@ include file="patient_view/mutations.jsp" %>
    </div>
    <%}%>

    <%if(showCNA){%>
    <div class="patient-section" id="cna">
        <%@ include file="patient_view/cna.jsp" %>
    </div>
    <%}%>

    <%if(showPathways){%>
    <div class="patient-section" id="pathways">
        <%@ include file="patient_view/pathways.jsp" %>
    </div>
    <%}%>

    <%if(showSimilarPatient){%>
    <div class="patient-section" id="similar-patients">
        <%@ include file="patient_view/similar_patients.jsp" %>
    </div>
    <%}%>

</div>
<%  
}
%>
    </td>
</tr>

<tr>
    <td colspan="3">
	<jsp:include page="global/footer.jsp" flush="true" />
    </td>
</tr>

</table>
</center>
</div>
<jsp:include page="global/xdebug.jsp" flush="true" />    

<script type="text/javascript">
$(document).ready(function(){
    setUpPatientTabs();
    initTabs();
});

function setUpPatientTabs() {
    $('#patient-tabs').tabs();
    $('#patient-tabs').show();
    fixCytoscapeWebRedraw();
}

function initTabs() {
    var tabContainers = $('.patient-section');
    tabContainers.hide().filter(':first').show();

    $('#patient-tabs ul a').click(function () {
            tabContainers.hide();
            tabContainers.filter(this.hash).show();
            $('#patient-tabs ul a').removeClass('selected');
            $(this).addClass('selected');
            return false;
    }).filter(':first').click();
    
}

function fixCytoscapeWebRedraw() {
    // to initially hide the network tab
    $("#pathways").attr('style', 'display: none !important; height: 0px; width: 0px; visibility: hidden;');
    
    // to fix problem of flash repainting
    $("a.patient-tab").click(function(){
        if($(this).attr("href")=="#pathways") {
            $("#pathways").removeAttr('style');
        } else {
            $("#pathways").attr('style', 'display: block !important; height: 0px; width: 0px; visibility: hidden;');
        }
    });
}

function switchToTab(toTab) {
    $('.patient-section').hide();
    $('.patient-section#'+toTab).show();
    $('#patient-tabs').tabs('select',$('#patient-tabs ul a[href="#'+toTab+'"]').parent().index());
}

function getEventString(eventTableData,dataCol,overviewCol) {
    var s = [];
    for (var i=0; i<eventTableData.length; i++) {
        if (overviewCol==null || eventTableData[i][overviewCol])
            s.push(eventTableData[i][dataCol]);
    }
    return s.join(",");
}

var cnaEventIds = null;
var overviewCnaEventIds = null;
var mutEventIds = null;
var overviewMutEventIds = null;
var overviewCnaGenes = null;
var overviewMutGenes = null;

var placeHolder = <%=Boolean.toString(showPlaceHoder)%>;

function getDrugMap(drugs) {
    var map = {};
    for (var gene in drugs) {
        var strs = [];
        var drugs_arr = drugs[gene];
        for (var i=0; i<drugs_arr.length; i++) {
            var drug = drugs_arr[i];
            strs.push("<a href=\"http://www.drugbank.ca/drugs/"+drug+"\">"
                + drug + "</a>");
        }
        
        map[gene] = strs.join("<br/>");
    }
    return map;
}

function trimHtml(html) {
    return html.replace(/<[^>]*>/g,"");
}

var geObs =  new GenomicEventObserver(<%=showMutations%>,<%=showCNA%>);

</script>

</body>
</html>