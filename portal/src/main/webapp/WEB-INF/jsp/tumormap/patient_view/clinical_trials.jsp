<%
    String cancerStudyName = "glioblastoma";
%>
<script type="text/javascript">
    var populateDrugTable = function() {
        var drugIds = [];
        drugIds = drugIds.concat(genomicEventObs.cnas.getDrugIDs());
        drugIds = drugIds.concat(genomicEventObs.mutations.getDrugIDs());

        var keywords = [];
        $.post("drugs.json",
                { drug_ids: drugIds.join(",") },
                function(data) {
                    $("#drugs_wait").hide();
                    for(var i=0; i < data.length; i++) {
                        var drug = data[i];

                        var xref = ["N/A"];
                        if (drug[7]) { // xref
                            xref = [];
                            var nci = drug[7]['NCI_Drug'];
                            if(nci)
                                xref.push("<a href='http://www.cancer.gov/drugdictionary?CdrID="+nci+"'>NCI</a>");
                            var pharmgkb = drug[7]['PharmGKB'];
                            if(pharmgkb)
                                xref.push("<a href='http://www.pharmgkb.org/views/index.jsp?objId="+pharmgkb+"'>PharmGKB</a>");
                            var drugbank = drug[7]['DrugBank'];
                            if(drugbank)
                                xref.push("<a href='http://www.drugbank.ca/drugs/"+drugbank+"'>DrugBank</a>");
                            var keggdrug = drug[7]['KEGG Drug'];
                            if(keggdrug)
                                xref.push("<a href='http://www.genome.jp/dbget-bin/www_bget?dr:"+keggdrug+"'>KEGG Drug</a>");
                        }

                        $("#pv-drugs-table").append(
                            '<tr>'
                                + '<td>' + drug[2] + '</td>'
                                + '<td>' + drug[3] + '</td>'
                                + '<td>' + drug[1] + '</td>'
                                + '<td>' + drug[5] + '</td>'
                                + '<td>' + (drug[4] ? "Yes" : "No") + '</td>'
                                + '<td>' + xref.join(",") + '</td>'
                            + '</tr>'
                        );

                        keywords.push(drug[2]);
                    }

                    $("#pv-drugs-table").dataTable({
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaSorting": [[0, 'asc']]
                    });
                    populateClinicalTrialsTable(keywords);
                }
        );
    };

    var populateClinicalTrialsTable = function(keywords) {
        keywords.push("<%=cancerStudyName%>");
        $.post("clinicaltrials.json",
                { keywords: keywords.join(",") },
                function(data) {
                    $("#trials_wait").hide();

                    for(var i=0; i < data.length; i++) {
                        var trial = data[i];
                        $("#pv-trials-table").append(
                            '<tr>'
                                + '<td>'
                                    + '<a href="http://clinicaltrials.gov/show/' + trial[0] + '" target="_blank">'
                                        + trial[0]
                                    + '</a>'
                                + '</td>'
                                + '<td>' + trial[1] + '</td>'
                                + '<td>' + trial[2] + '</td>'
                                + '<td>' + trial[3] + '</td>'
                                + '<td>' + trial[4] + '</td>'
                            + '</tr>'
                        );
                    }

                    $("#pv-trials-table").dataTable({
                        "bJQueryUI": true,
                        "bDestroy": true,
                        "aaSorting": [[2, 'asc']]
                    });
                }
        );
    };

    $(document).ready(function() {
        genomicEventObs.subscribeMutCna(populateDrugTable);
    });
</script>


<h2 class="pv-drugs-header">Drugs of interest</h2>
<table id="pv-drugs-table">
   <thead>
    <tr>
        <th>Drug Name</th>
        <th>Synonyms</th>
        <th>Drug Target(s)</th>
        <th>Description</th>
        <th>FDA approved?</th>
        <th>Data Sources</th>
    </tr>
   </thead>

</table>
<div id="drugs_wait"><img src="images/ajax-loader.gif"/></div>



<h2 class="pv-drugs-header">Clinical trials of interest</h2>
<table id="pv-trials-table">
   <thead>
    <tr>
        <th>Trial ID</th>
        <th>Title</th>
        <th>Status</th>
        <th>Phase</th>
        <th>Location</th>
    </tr>
   </thead>
</table>
<div id="trials_wait"><img src="images/ajax-loader.gif"/></div>
