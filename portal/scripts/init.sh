# First, verify if all symbols in the sample genesets are latest
./verifyGeneSets.pl $CGDS_DATA_HOME/human_genes.txt

# Clear the Database
./resetDb.pl

# Load up Entrez Genes
./importGenes.pl $CGDS_DATA_HOME/human_genes.txt

# Load up all microRNA IDs
#./importMicroRna.pl $CGDS_DATA_HOME/microRNA/microRNAs.txt

# Load up Cancer Types
./importTypesOfCancer.pl $CGDS_DATA_HOME/cancers.txt

# Load up Sanger Cancer Gene Census
./importSangerCensus.pl ../sample_data/genes/sanger_gene_census.txt