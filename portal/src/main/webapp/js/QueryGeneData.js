// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

// check out the wiki for some documentation on the Gene Data datastructure


QueryGeneData = function(data) {
    // data query tools
    // for gene alterations data

    var that = {};

    that.byHugo = function(hugo) {
        // returns all the data associated with a particular gene
        // (a row in the oncoprint matrix)
        var index = data.hugo_to_gene_index[hugo];

        return data.gene_data[index];
    };

    that.bySampleId = function(sample_id) {
        // returns all the data associated with a particular sample
        // (a column in the oncoprint matrix)
        var index = data.samples[sample_id];

        var toReturn = {};

        data.gene_data.forEach(function(gene) {
            toReturn[gene.hugo] = {
                mutation: gene.mutations[index],
                cna: gene.cna[index],
                mrna: gene.mrna[index],
                rppa: gene.rppa[index]
            }
        });

        return toReturn;
    };

    that.getSampleList = function() {
        var samples = data.samples;

        var samples_l = [];

        for (var sample in samples) {
            samples_l.push(sample);
//                console.log(sample);
        }

        samples_l.sort(function(a,b) {
            return samples[a] - samples[b];
        });

        return samples_l;
    };

    that.data = function(sample_id, gene, data_type) {
        // _sample_id, _gene, _data_type -> data
        // e.g. "TCGA-04-1331", "BRCA2", "mutations" -> "C711*"
        // (an entry in the oncoprint matrix)

        return that.bySampleId(sample_id)[gene][data_type];
    };

    return that;
};
