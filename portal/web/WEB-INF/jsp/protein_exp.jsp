<%@ page import="org.mskcc.portal.servlet.ProteinArraySignificanceTestJSON" %>

<link href="css/data_table.css" type="text/css" rel="stylesheet"/>

<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script> 

<script type="text/javascript">
    jQuery.fn.dataTableExt.oSort['num-nan-col-asc']  = function(a,b) {
	var x = parseFloat(a);
	var y = parseFloat(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
	return ((x < y) ? -1 : ((x > y) ?  1 : 0));
    };

    jQuery.fn.dataTableExt.oSort['num-nan-col-desc'] = function(a,b) {
	var x = parseFloat(a);
	var y = parseFloat(b);
        if (isNaN(x)) {
            return isNaN(y) ? 0 : 1;
        }
        if (isNaN(y))
            return -1;
	return ((x < y) ? 1 : ((x > y) ?  -1 : 0));
    };
    
    $(document).ready(function(){
        $('table#protein_expr').hide();
        $.post("ProteinArraySignificanceTest.json", 
            {<%=ProteinArraySignificanceTestJSON.HEAT_MAP%>:$("textarea#heat_map").html()
            },
            function(aDataSet){
                //$("div#protein_exp").html(aDataSet);
                //alert(aDataSet);
                //$('div#protein_exp').html( '<table cellpadding="0" cellspacing="0" border="0" class="display" class="display" id="protein_expr"></table>' );
                $('table#protein_expr').dataTable( {
                        "aaData": aDataSet,
                        "aoColumnDefs":[
                            { //"sTitle": "Gene",
                              //"bVisible": false,
                              "aTargets": [ 0 ]
                            },
                            { //"sTitle": "Alteration type",
                              //"bVisible": false,
                              "aTargets": [ 1 ] 
                            },
                            { //"sTitle": "Type",
                              "aTargets": [ 2 ]
                            },
                            { //"sTitle": "Target Gene",
                              "aTargets": [ 3 ] 
                            },
                            { //"sTitle": "Target Residue",
                              "aTargets": [ 4 ] 
                            },
                            { //"sTitle": "Source organism",
                              "bVisible": false, 
                              "aTargets": [ 5 ] 
                            },
                            { //"sTitle": "Validated?",
                              "bVisible": false,
                              "aTargets": [ 6 ]
                            },
                            { //"sTitle": "Ave. Altered<sup>1</sup>",
                              "sType": "num-nan-col",
                              "bSearchable": false,
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    return value.toFixed(2);
                               },
                               "aTargets": [ 7 ]
                            },
                            { //"sTitle": "Ave. Unaltered<sup>1</sup>",
                              "sType": "num-nan-col",
                              "bSearchable": false,
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    return value.toFixed(2);
                               },
                               "aTargets": [ 8 ]
                            },
                            { //"sTitle": "p-value",
                              "sType": "num-nan-col",
                              "fnRender": function(obj) {
                                    var value = parseFloat(obj.aData[ obj.iDataColumn ]);
                                    if (isNaN(value))
                                        return "NaN";
                                    
                                    var ret = value < 0.001 ? value.toExponential(2) : value.toFixed(3);
                                    
                                    var eps = 10e-5;
                                    var abunUnaltered = parseFloat(obj.aData[7]);
                                    var abunAltered = parseFloat(obj.aData[8]);
                                    
                                    if (Math.abs(abunUnaltered-abunAltered)<eps)
                                        return ret;
                                    if (abunUnaltered < abunAltered)
                                        return ret + "<img src=\"images/up1.png\"/>";
                                    
                                    return ret + "<img src=\"images/down1.png\"/>";                                    
                               },
                               "aTargets": [ 9 ]
                            },
                            { //"sTitle": "data",
                              "bVisible": false,
                              "bSearchable": false,
                              "bSortable": false,
                              "aTargets": [ 10 ]
                            }
                        ],
                        "aaSorting": [[9,'asc']],
                        "iDisplayLength": 20
                } );
                $('table#protein_expr').show();
                $('div#protein_expr_wait').remove();
            }
            ,"json"
        );
    });
</script>

<div class="section" id="protein_exp">
    <div id="protein_expr_wait"><img src="images/ajax-loader.gif"/></div>
    
    <table cellpadding="0" cellspacing="0" border="0" class="display" class="display" id="protein_expr">
        <thead>
            <tr valign="bottom">
                <th rowspan="2">Gene</th>
                <th rowspan="2">Alteration</th>
                <th rowspan="2">Type</th>
                <th colspan="2">Target</th>
                <th rowspan="2">Source organism</th>
                <th rowspan="2">Validated?</th>
                <th colspan="2">Ave. Abundance<a href="#" title="Average of median centered protein abundance scores for unaltered cases and altered cases, respectively."><sup>1</sup></a></th>
                <th rowspan="2">p-value<a href="#" title="Based on two-sided two sample student t-test."><sup>2</sup></a></th>
                <th rowspan="2">Data</th>
            </tr>
            <tr>
                <th>Protein</th>
                <th>Residue</th>
                <th>Unaltered</th>
                <th>Altered</th>
            </tr>
        </thead>
    </table>
</div>