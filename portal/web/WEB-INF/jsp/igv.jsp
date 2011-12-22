<%@ page import="org.mskcc.portal.servlet.QueryBuilder" %>
<%@ page import="org.mskcc.portal.servlet.ServletXssUtil" %>


<div class="section" id="igv_tab">
    <table>
        <tr>
            <td style="padding-right:25px; vertical-align:top;"><img src="images/IGVlogo.png" alt=""/></td>
            <td style="vertical-align:top">

                <p>
                    The <a href="http://www.broadinstitute.org/igv/home">Integrative Genomics
                    Viewer (IGV)</a> is a high-performance visualization tool for interactive exploration
                    of large, integrated datasets. It supports a wide variety of data types including
                    sequence alignments, microarrays, and genomic annotations.
                </p>

                <p>Clicking the launch button below will:</p>

                <p>
                    <ul>
                        <li>start IGV via Java Web Start.</li>
                        <li>load copy number data (segmented) for your selected cancer study; and</li>
                        <li>automatically highlight your query genes.</li>
                    </ul>
                </p>

                <br>
                    <a id="igvLaunch" href="#" onclick="prepIGVLaunch('<%= cancerTypeId %>','<%= geneList %>')"><img src="images/webstart.jpg" alt=""/></a>
                <br>

                <p>
                    Once you click the launch button, you may need to select Open with Java&#8482;
                    Web Start and click OK. If the system displays messages about trusting the application,
                    confirm that you trust the application. Web Start will then download and start IGV.
                    This process can take a few minutes.
                </p>
                <br>
                <p>
                    For information regarding IGV, please see:
                    <ul>
                        <li><a href="http://www.broadinstitute.org/software/igv/QuickStart">IGV Quick Start Tutorial</a></li>
                        <li><a href="http://www.broadinstitute.org/software/igv/UserGuide">IGV User Guide</a></li>
                    </ul>
                </p>
                
                <p>
                    IGV is developed at the <a href="http://www.broadinstitute.org/">Broad Institute of MIT and Harvard</a>.
                </p>
            </td>
        </tr>
    </table>
</div>

<script type="text/javascript" src="js/igv_webstart.js"></script>

<script type="text/javascript">

        function prepIGVLaunch(cancerTypeId, geneList) {

            var genes = geneList;
            genes = genes.replace(/\s+/gi, "%20");

            var segFile = cancerTypeId + ".seg";

            var port = 60151;
            var dataUrl = "http://cbio.mskcc.org/cancergenomics/public-portal/" + segFile;
            var genomeID = "hg18";
            var mergeFlag = false;
            var locusString = genes;
            var trackName = null;

            appRequest(port, dataUrl, genomeID, mergeFlag, locusString, trackName);

        }
    
</script>
