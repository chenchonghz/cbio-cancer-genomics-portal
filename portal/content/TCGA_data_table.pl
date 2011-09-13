#!/usr/bin/perl

my ($y, $m, $d) = (localtime)[5,4,3];

$y += 1900;
$m += 1;

open (IN1,"cancers.txt");

while ($line = <IN1>) {

	chomp $line;
	@data = split (/ \: /,$line);
	$data[0] =~ tr/a-z/A-Z/;
	$cancers{$data[0]} = $data[1];
#	print "$data[0]\t$data[1]\n";
}


open (IN2,"LATEST_RUN.txt");
<IN2>;
$line = <IN2>;
chomp $line;
@data=split(/00/,$line);
$date=$data[0];

open (IN3,"convertFirehoseData.out");

open (OUT1,">data_sets_tcga.html");
open (OUT2,">data_sets_tcga_right_column.markdown");

print OUT1 "<P><p>The portal currently contains data from the following TCGA cancer genomics studies. The table below lists the number of available samples per data type and tumor.<br><br>\n";
#print OUT1 "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" bordercolor=\"#808080\">\n";
print OUT1 "<table>\n";
print OUT1 "\t<tr>\n";
print OUT1 "\t\t<th align=\"left\">Cancer Type/Study</th>\n";
print OUT1 "\t\t<th>Full cancer name</th>\n";
print OUT1 "\t\t<th>aCGH</th>\n";
print OUT1 "\t\t<th>Sequenced</th>\n";
print OUT1 "\t\t<th>Normal mRNA</th>\n";
print OUT1 "\t\t<th>Tumor mRNA</th>\n";
print OUT1 "\t\t<th>Complete</th>\n";
print OUT1 "\t\t<th>All</th>\n";
print OUT1 "\t</tr>\n\n";

print OUT2 "<table width=150>\n";
print OUT2 "<tr>\n";
print OUT2 "<td><b>Cancer</b></th>\n";
print OUT2 "<td align=right><b>Cases</b></th>\n";
print OUT2 "</tr>\n\n";


<IN3>;
$line_ct = 0;
while ($line = <IN3>) {

	chomp $line;
	@data = split (/\t/,$line);
	$data[0] =~ tr/a-z/A-Z/;

	unless ($data[6]==0) {
      if ($line_ct++ % 2) {
        print OUT1 "\t<tr>\n";
      }
      else {
		print OUT1 "\t<tr class=\"rowcolor\">\n";
      }
		print OUT1 "\t\t<td><b>$data[0]</b></td>\n";
		print OUT1 "\t\t<td style=\"text-align: left;\"><b>$cancers{$data[0]}</b></td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[2]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[1]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[4]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[5]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\">$data[3]</td>\n";
		print OUT1 "\t\t<td style=\"text-align: center;\"><b>$data[6]</b></td>\n";
		print OUT1 "\t</tr>\n\n";
	}
	
	unless ($data[6]==0) {
		print OUT2 "<tr>\n";
		print OUT2 "<td class=\"Tips1\" title=\"$cancers{$data[0]}\">$data[0]</td>\n";
		print OUT2 "<td style=\"text-align: right;\">$data[6]</td>\n";
		print OUT2 "</tr>\n";
	}
}

print OUT1 "</table>";
#print OUT1 "\n<br>Last update: $m/$d/$y<br>";

$date =~ /(\d\d\d\d)(\d\d)(\d\d)/;
$dateOut = "$2/$3/$1";

print OUT1 "Based on the Firehose run from $dateOut.</p>";

print OUT2 "</table>\n";
#print OUT2 "\n<p>Last update: $m/$d/$y.<br><a href=\"data_sets.jsp\">More...</a></p>";
print OUT2 "Based on the Firehose run from $dateOut.</p>";

close (IN1);
close (IN2);
close (IN3);
close (OUT1);
close (OUT2);
