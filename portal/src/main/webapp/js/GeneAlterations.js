// Gideon Dresdner
// dresdnerg@cbio.mskcc.org
// November 2012

var GeneAlterations = function(sendData) {
//     manager to handle ajax requests for gene alteration data
//     (e.g. for tables, oncoprints, ...)
//     the idea is that you can call fire and not have to worry about whether you need to make a new AJAX call or not.
//
//     methods:
//     addListener:    function                        ->  return undefined
//     addListeners:   [list of functions]             ->  return undefined
//     getAlterations: (optional: callback function)   ->  return undefined, or alterations if they have already been loaded
//     redo:           new_sendData    redoes the request on the new data
//
//     sendData is an object literal that looks something like this :
//     {   cancer_study_id: \"tcga_gbm\",
//         genes:\"EGFR MDM2\",
//         samples: \"TCGA-02-0001 TCGA-02-0003 TCGA-02-0004 TCGA-02-0006 TCGA-02-0007 TCGA-02-0009\",
//         geneticProfileIds: \"gbm_mutations gbm_cna_consensus\" }
//
//     doing a request returns an object like this:
//     {
//      gene_data : ~,
//      hugo_to_gene_index: ~,
//      samples: ~
//     }
//
//     gene_data:           *array* of objects like this
//     {
//         cna:                     array,
//         hugo:                    string,
//         mrna:                    array,
//         mutations:               array,
//         rppa:                    array,
//         percent_altered:         string
//     }

//     hugo_to_gene_index:  map into the gene_data array indices
//     samples:             map into the array indices of the data_types (cna, mutations, etc)


    var json = 'GeneAlterations.json',      // json url
        data = {},                          // alterations object, the return of the json 'to be'
        listeners = [],                     // queue of callback functions
        that = {},                          // GeneAlterations object
        MAKE_NEW_REQUEST = true;            // indicates whether you should make a new request or not

    var addListener = function(fun) {
        listeners.unshift(fun);

        return true;
    };

    var fire_listeners = function(data) {
        // kaboom!

        for (var fun = listeners.pop(); fun !== undefined; fun = listeners.pop()) {
            fun(data);
        }

        return listeners;
    };

    that.addListener = addListener;

    that.addListeners = function(fun_l) {
        // adds a list of listeners
        listeners = fun_l.concat(listeners);
    };

    that.fire = function(optional_callback) {
        // fire, fire on the mountain

        if (optional_callback !== undefined) {
            addListener(optional_callback);
        }

        if (MAKE_NEW_REQUEST) {
            $.post(json, sendData, function(returnData) {
                // set alterations first, then fire callbacks
                data = returnData;
                fire_listeners(data);

                MAKE_NEW_REQUEST = false;
            });
        } else {
            fire_listeners(data);
        }
    };

    that.getSendData = function() {
        // gets the original data sent to the server
        return sendData;
    };

    that.compareSendDatas = function(obj1, obj2) {
        // basic object comparison, nothing fancy
        for (i in obj1) {
            if (obj1[i] !== obj2[i]) {
                return false;
            }
        }

        return true;
    };

    that.setSendData = function(new_SendData) {
        // set the data to the new data
        // N.B. you still need to add listeners and fire!

        sendData = new_SendData;

        MAKE_NEW_REQUEST = !that.compareSendDatas(sendData, new_SendData);
        // flip on the switch since you just updated the sendData
    };

    that.getQuery = function() {
        // N.B. you should only use this inside a callback
        return GeneAlterations.query(data);
    };

    return that;
};

GeneAlterations.query = function(data) {
    // data query tools
    // for gene alterations data

    that = {};

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
