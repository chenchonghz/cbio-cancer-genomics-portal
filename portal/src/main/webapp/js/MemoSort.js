var MemoSort = function(geneAlterations, sort_by) {

    var query = GeneAlterations.query(geneAlterations);

    var comparator = function(s1, s2) {
        // list of genes with corresponding alteration data
        var sample1 = query.bySampleId(s1),
            sample2 = query.bySampleId(s2);

        // alterations for the gene we want to sort by
        sample1 = sample1[sort_by];
        sample2 = sample2[sort_by];
//        console.log('sample', sample1);

        var cna_order = {AMPLIFIED: 2, DELETED: 1, HOMODELETED: 1, null: 0},
            regulated_order = {UPREGULATED: 2, DOWNREGULATED: 1, null: 0};

        // diffs
        var cna = cna_order[sample1.cna] - cna_order[sample2.cna],
            mutation,
            mrna = regulated_order[sample1.mrna] - regulated_order[sample2.mrna],
            rppa = regulated_order[sample1.rppa] - regulated_order[sample2.rppa];

        // figure out the mutation diff
        if ((sample1.mutation === null) === (sample2.mutation === null)) {
            mutation = 0;
        } else if (sample1.mutation !== null) {
            mutation = 1;
        } else {
            mutation = -1;
        }

        // sanity check
        if (cna === undefined
            || mutation === undefined
            || mrna === undefined
            || rppa === undefined) {
            console.log("cna: " + cna
                + " mutation: " + mutation
                + " mrna: " + mrna
                + " rppa: " + rppa);
            return;
        }

        // do some logic
        // cna > mutation > mrna > rppa

        if (cna !== 0) {
            return cna;
        }

        if (mutation !== 0) {
            return mutation;
        }

        if (mrna !== 0) {
            return mrna;
        }

        if (rppa !== 0) {
            return rppa;
        }

        return 0;
    };

    var sort = function() {
        // geneAlterations:  data structure from GeneDataJSON

        // the hugo gene symbol to sort by

        // sorting order : amplification, deletion, mutation, mrna, rppa
        // mutation > 0
        // amp > del > 0
        //

        var samples = geneAlterations.samples;

        var query = GeneAlterations.query(geneAlterations);

        // get the array of samples in the defined order
        var samples_l = query.getSampleList();

        samples_l.sort(that.comparator);
        // samples_l is now sorted, is this bad functional programming?

        // copy the mapping
        var sorted_samples =  $.extend({}, samples);

        // reindex the mapping according to the new sorting
        samples_l.forEach(function(val, i) {
            sorted_samples[val] = i;
        });

        return sorted_samples;
    };

    var that = {
        comparator: comparator,
        sort: sort
    };

    return that;
};
