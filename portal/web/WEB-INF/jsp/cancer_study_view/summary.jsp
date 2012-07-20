
<%@ page import="org.mskcc.portal.servlet.MutationsJSON" %>
<%@ page import="org.mskcc.portal.servlet.CnaJSON" %>
<%@ page import="org.mskcc.portal.servlet.PatientView" %>

<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">   
    google.load('visualization', '1', {packages:['table','corechart']}); 
    $(document).ready(function(){
        setupCaseSelect(caseIds);
        loadClinicalData(caseSetId);
        loadMutationCount(mutationProfileId,caseIds);
        loadCnaFraction(caseIds);
    });
    
    function CaseSelectObserver() {
        this.funcs = {};
        this.caseId = null;
    }
    CaseSelectObserver.prototype = {
        subscribe: function(listener,func) {
            this.funcs[listener] = func;
            func.call(window,this.caseId);
        },
        fireSelection: function(caseId,source) {
            if (caseId == this.caseId) return;
            this.caseId = caseId;
            for (var listener in this.funcs) {
                if (listener==source) continue;
                var func = this.funcs[listener];
                func.call(window,caseId);
            }
        }
    };
    var csObs = new CaseSelectObserver();
    
    function setupCaseSelect(caseIds) {
        var caseSelect = $('#case-select');
        for (var i=0; i<caseIds.length; i++) {
            caseSelect
                .append($("<option></option>")
                .attr("value",caseIds[i])
                .attr("id",caseIds[i]+"_select")
                .text(caseIds[i]));
        }
        csObs.subscribe('case-select',function(caseId){
            var op = caseId ? $("#"+caseId+"_select") : $("#null_case_select");
            op.attr("selected","selected");
        });
        caseSelect.change(function(e) {
            var caseId = $('#case-select  option:selected').attr('value');
            csObs.fireSelection(caseId,'case-select')
        });
    }
    
    var clincialDataTableWrapper = null;
    function loadClinicalData(caseSetId) {
        var params = {cmd:'getClinicalData',
                    case_set_id:caseSetId,
                    include_free_form:1};
        $.get("webservice.do",
            params,
            function(data){
                //$('#summary').html("<table><tr><td>"+data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/[\r\n]+/g,"</td></tr><tr><td>").replace(/\t/g,"</td><td>")+"</td></tr></table>");
                var rows = data.replace(/^#[^\r\n]+[\r\n]+/g,"").replace(/\tNA([\t\r\n])/g,"\t$1").replace(/\tNA([\t\r\n])/g,"\t$1").match(/[^\r\n]+/g);
                var matrix = [];
                for (var i=0; i<rows.length; i++) {
                    matrix.push(rows[i].split('\t'));
                }

                clincialDataTableWrapper = new DataTableWrapper();
                clincialDataTableWrapper.setDataMatrixAndFixTypes(matrix);
                mergeTablesAndVisualize();
            })
    }

    var mutDataTableWrapper = null;
    function loadMutationCount(mutationProfileId,caseIds) {
        if (mutationProfileId==null) return;
        var params = {
            <%=MutationsJSON.CMD%>: '<%=MutationsJSON.COUNT_MUTATIONS_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' '),
            <%=PatientView.MUTATION_PROFILE%>: mutationProfileId
        };

        $.post("mutations.json", 
            params,
            function(mutationCounts){
                mutDataTableWrapper = new DataTableWrapper();
                mutDataTableWrapper.setDataMap(mutationCounts,['case_id','mutation_count']);
                mergeTablesAndVisualize();
            }
            ,"json"
        );
    }

    var cnaDataTableWrapper = null;
    function loadCnaFraction(caseIds) {
        if (cnaProfileId==null) return;
        var params = {
            <%=CnaJSON.CMD%>: '<%=CnaJSON.GET_CNA_FRACTION_CMD%>',
            <%=QueryBuilder.CASE_IDS%>: caseIds.join(' ')
        };

        $.post("cna.json", 
            params,
            function(cnaFracs){
                cnaDataTableWrapper = new DataTableWrapper();
                // TODO: what if no segment available
                cnaDataTableWrapper.setDataMap(cnaFracs,['case_id','copy_number_altered_fraction']);
                mergeTablesAndVisualize();
            }
            ,"json"
        );
    }
    
    function mergeTablesAndVisualize() {
        var dt = mergeDataTables();
        if (dt) {
            var headerMap = getHeaderMap(dt);
            var caseMap = getCaseMap(dt);
            
            plotAgeHistogram(dt,headerMap['age_at_diagnosis'],'age-hist');
            plotPieChart(dt,headerMap['histology'],'hist-pie');
            plotPieChart(dt,headerMap['2009stagegroup'],'stage-pie');
            plotPieChart(dt,headerMap['tumor_grade'],'grade-pie');
            
            var formatter = new google.visualization.PatternFormat(formatPatientLink('{0}'));
            formatter.format(dt, [0]);
            drawDataTable(dt,'clinical-data-table');
            
            var colCna = headerMap['copy_number_altered_fraction'];
            var colMut = headerMap['mutation_count'];
            plotMutVsCna(dt,colCna,colMut,caseMap,true,'scatter-plot');
        }
    }
    
    function drawDataTable(dt,divId) {
            var tableDataView = new google.visualization.DataView(dt);
            var table = new google.visualization.Table(document.getElementById(divId));
            table.draw(tableDataView,{allowHtml: true, showRowNumber: true});
    }
    
    function plotMutVsCna(dt,colCna,colMut,caseMap,vLog,divId) {
            var scatterDataView = new google.visualization.DataView(dt);
            scatterDataView.setColumns(
                [{calc:function(dt,row){return 100*dt.getValue(row,colCna);},type:'number'},
                 colMut,
                 {calc:function(dt,row){return dt.getValue(row,0);},type:'string',role:'tooltip'}]);
            var scatter = new google.visualization.ScatterChart(document.getElementById(divId));
            google.visualization.events.addListener(scatter, 'select', function(e){
                var s = scatter.getSelection();
                var caseId = s.length==0 ? null : dt.getValue(s[0].row,0);
                csObs.fireSelection(caseId, 'scatter-plot');
            });
            
            google.visualization.events.addListener(scatter, 'ready', function(e){
                csObs.subscribe('scatter-plot',function(caseId) {
                    var ix = caseMap[caseId];
                    // this is not working due to a google bug
                    // http://goo.gl/dXvDN
                    scatter.setSelection(ix==null?null:[ix]);
                });
            });
            var options = {
                hAxis: {title: "Copy number alteration percentage (%)"},
                vAxis: {title: "# of mutations", logScale:vLog},
                legend: 'none'
            };
            scatter.draw(scatterDataView,options);
    }
    
    function formatPatientLink(caseId) {
        return '<a href="patient.do?<%=PatientView.PATIENT_ID%>='+caseId+'">'+caseId+'</a>'
    }
    
    function mergeDataTables() {
        if (clincialDataTableWrapper==null ||
            (mutationProfileId!=null && mutDataTableWrapper==null) ||
            (cnaProfileId!=null && cnaDataTableWrapper==null)) {
            return null;
        }
        
        var dt = clincialDataTableWrapper.dataTable;
        
        if (mutDataTableWrapper!=null) {
            dt = google.visualization.data.join(dt, mutDataTableWrapper.dataTable,
                    'full', [[0,0]], makeContInxArray(1,dt.getNumberOfColumns()-1),[1]);
        }
        
        if (cnaDataTableWrapper!=null) {
            dt = google.visualization.data.join(dt, cnaDataTableWrapper.dataTable,
                    'full', [[0,0]], makeContInxArray(1,dt.getNumberOfColumns()-1),[1]);
        }
        
        clincialDataTableWrapper = null;
        mutDataTableWrapper = null;
        
        return dt;
    }
    
    function getHeaderMap(dataTable) {
        var map = {};
        var cols = dataTable.getNumberOfColumns();
        for (var i=0; i<cols; i++) {
            map[dataTable.getColumnLabel(i)] = i;
        }
        return map;
    }
    
    function getCaseMap(dataTable) {
        var map = {};
        var rows = dataTable.getNumberOfRows();
        for (var i=0; i<rows; i++) {
            map[dataTable.getValue(i,0)] = i;
        }
        return map;
    }
    
    function makeContInxArray(start,end) {
        var ix = [];
        for (var i=start; i<=end; i++) {
            ix.push(i);
        }
        return ix;
    }
    
    function plotAgeHistogram(dt,col,divId) {
        var bins = [20,30,40,50,60,70,80];
        var hist = calcHistogram(dt,col,bins);
        var ageHistDTW = new DataTableWrapper();
        ageHistDTW.setDataMap(hist,['Age','# patients']);
        var column = new google.visualization.ColumnChart(document.getElementById(divId));
        var options = {
            hAxis: {title: 'Age at diagnosis'},
            vAxis: {title: '# of Patients'},
            legend: {position: 'none'}
        }
        column.draw(ageHistDTW.dataTable,options);
    }
    
    function plotPieChart(dt,col,divId) {
        var hist = calcHistogram(dt,col);
        var histHistDTW = new DataTableWrapper();
        histHistDTW.setDataMap(hist,[dt.getColumnLabel(col),'# patients']);
        var column = new google.visualization.PieChart(document.getElementById(divId));
        column.draw(histHistDTW.dataTable);
    }
    
    function calcHistogram(dt,col,bins) {
        var hist = {};
        var rows = dt.getNumberOfRows();
        var t = dt.getColumnType(col);
        if (t=="number") {
            if (bins==null) bins = 10;
            if ((typeof bins)==(typeof 1)) {
                var r = getColumnRange(col);
                var step = (r.max-r.min)/(bins-1);
                bins = [];
                for (var i=0; i<bins-1; i++) {
                    bins.push(r.min+i*step);
                }
            }
            
            var count = [];
            for (var i=0; i<=bins.length; i++) {
                count.push(0);
            }
            
            for (var r=0; r<rows; r++) {
                var v = dt.getValue(r,col);
                var low = 0, high = bins.length, i;
                while (low <= high) {
                    i = Math.floor((low + high) / 2); 
                    if (bins[i] >= v)  {high = i - 1;}
                    else {low = i + 1;}
                }
                count[low] = count[low]+1;
            }
            
            hist['<='+bins[0]] = count[0];
            var i=1;
            for (; i<bins.length; i++) {
                hist[bins[i-1]+'~'+bins[i]] = count[i];
            }
            hist['>'+bins[bins.length-1]] = count[i];
        } else {
            for (var r=0; r<rows; r++) {
                var v = dt.getValue(r,col);
                if(v!=null)
                    hist[v] = hist[v] ? hist[v]+1 : 1;
            }
        }
        return hist;
    }
</script>

<table>
    <tr>
        <td>
            <fieldset>
                <legend style="color:blue;font-weight:bold;">Age Distributions</legend>
                <div id="age-hist" style="width:300px;height:200px;display:block;">
                    <img src="images/ajax-loader.gif"/>
                </div>
            </fieldset>
        </td>
        <td>
            <fieldset>
                <legend style="color:blue;font-weight:bold;">Histology</legend>
                <div id="hist-pie" style="width:300px;height:200px;display:block;">
                    <img src="images/ajax-loader.gif"/>
                </div>
            </fieldset>
        </td>
        <td rowspan="2">
            <fieldset>
                <div>
                    <form name="input" action="patient.do" method="get">
                        <select id="case-select" name="<%=PatientView.PATIENT_ID%>"><option id="null_case_select">select one case</option></select>
                        <input type="submit" value="More About This Case" />
                    </form>
                </div>
                <legend style="color:blue;font-weight:bold;">Mutation Count VS. Copy Number Alteration Fraction</legend>
                <div id="scatter-plot" style="width:500px;height:410px;display:block;">
                    <img src="images/ajax-loader.gif"/>
                </div>
            </fieldset>
        </td>
    </tr>
    <tr>
        <td>
            <fieldset>
                <legend style="color:blue;font-weight:bold;">Stage</legend>
                <div id="stage-pie" style="width:300px;height:200px;display:block;">
                    <img src="images/ajax-loader.gif"/>
                </div>
            </fieldset>
        </td>
        <td>
            <fieldset>
                <legend style="color:blue;font-weight:bold;">Grade</legend>
                <div id="grade-pie" style="width:300px;height:200px;display:block;">
                    <img src="images/ajax-loader.gif"/>
                </div>
            </fieldset>
        </td>
    </tr>
    
</table>

<div id="clinicalTable"></div>