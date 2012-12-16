package exomizer.exome;

import java.util.ArrayList;

import exomizer.common.Constants;
import exomizer.filter.ITriage;
import exomizer.reference.Annotation;

/* A class that is used to hold information about the individual variants 
 *  as parsed from the VCF file.
 * @author Peter Robinson
 * @version 0.03 12 December 2012
 */
public class Variant implements Comparable<Variant>, Constants {
    
    
    /** chromosome; 23=X, 24=Y */
    private byte chromosome;
    /** position along chromosome; */
    private int position; 
    /** Reference sequence (a single nucleotide for a SNV, more for some other types of variant) */
    private String ref;
    /** Variant sequence (called "ALT", alternate, in VCF files). */
    private String var;
    /**  genotype (See exomizer.common.Constants for the integer constants used to represent the genotypes). */
    private byte genotype=GENOTYPE_NOT_INITIALIZED;
    /** Quality of variant call, taken from QUAL column of VCF file, with QUAL=-10log<SUB>10</SUB> prob(call in ALT is wrong). */
    private float variant_quality;
    /** DP4: Number=4,Type=Integer,Description="# high-quality ref-forward bases, ref-reverse, alt-forward and alt-reverse bases"*/
    private int[] DP4;
    /** Number of reads associated with variant call (since we are using a short, this value has a maximum of 32,767). */
    private short nReads=0;
    /** A list of results of filtering applied to this variant. */
    private ArrayList<ITriage> triage_list=null;
    /** Original VCF line from which this mutation comes. */
    public String vcfLine=null;
    /** Annotation object resulting from Jannovar-type annotation of this variant. */
    private Annotation annot=null;

   

 
   
     /**
     * @param c The chromosome (note: X=23, Y=24)
     * @param p Position of the variant
     * @param r Reference nucleotide
     * @param var variant (alt) nucleotide
    */
    public Variant(String c, int p, String r, String var) {
	this.chromosome = convertChromosomeStringToByteValue(c);
	this.position=p;
	this.ref = r;
	this.var = var;
	this.triage_list = new ArrayList<ITriage> ();
    }

    /**
     * This constructor is intended to be used by the factory method
     * {@link exomizer.io.VCFLine#extractVariant extractVariant} in the
     * class {@link exomizer.io.VCFLine VCFLine}. The constructor merely
     * initializes the ArrayList of ITriage objects and expects that everything
     * else will be initialized by {@link exomizer.io.VCFLine#extractVariant extractVariant}.
     */
    public Variant() {
	this.triage_list = new ArrayList<ITriage> ();
    }
   

    // ###########   SETTERS ######################### //
    /** Initialize the {@link #chromosome} field
     * @param chr A string representation of the chromosome such as chr3 or chrX
     */
    public void setChromosome(String chr) {
	this.chromosome = convertChromosomeStringToByteValue(chr);
    }
     /** Initialize the {@link #position} field
     * @param p Position of the variant on the chromosome
     */
    public void setPosition(int p) {
	this.position = p;
    }
    /**
     * Initialize the {@link #ref} field
     * @param s sequence of reference
     */
    public void setRef(String s) {
	this.ref = s;
    }
     /**
     * Initialize the {@link #var} field
     * @param s sequence of variant
     */
    public void setVar(String s) {
	this.var = s;
    }
    public void set_homozygous_alt() { this.genotype = GENOTYPE_HOMOZYGOUS_ALT; }
    public void set_heterozygous() { this.genotype = GENOTYPE_HETEROZYGOUS; }
    public void set_homozygous_ref() { this.genotype = GENOTYPE_HOMOZYGOUS_REF; }
    public void set_variant_quality(int q) { this.variant_quality = q; }
    public void addFilterTriage(ITriage t){ this.triage_list.add(t); }
    public void setVCFline(String line) { this.vcfLine = line; }

    /**
     * Set the annotation object for this variant. This method is intended to be
     * used by our annovar-style annotation code in order to provide transcript-
     * level annotation for the variants, for example, to annotate the chromosomal
     * variant {@code chr7:34889222:T>C} to {@code NPSR1(uc003teh.1:exon10:c.1171T>C:p.*391R)" (Type:STOPLOSS)}.
     */
    public void setAnnotation(Annotation a) {
	this.annot = a;
    }


   
    // ###########   GETTERS ######################### //
    private boolean is_non_SNV_patho = false;
    public boolean is_non_SNV_pathogenic() { return is_non_SNV_patho; }
    public int get_position() { return position; }
    /**
     * @return The reference base or bases of this variant
     */
    public String get_ref() { return ref; }
    /**
     * The alternate base or bases of this variant.
     */
    public String get_alt() { return var; }
    /**
     * Get the genesymbol of the gene associated with this variant, if possible 
     */
    public String get_genename() { 
	if (this.annot != null)  {
	    return annot.getGeneSymbol();
	} else {
	    return ".";
	}    
    }
    public boolean is_homozygous_alt() { return this.genotype == GENOTYPE_HOMOZYGOUS_ALT; }
    public boolean is_homozygous_ref() { return this.genotype == GENOTYPE_HOMOZYGOUS_REF; }
    public boolean is_heterozygous() { return this.genotype == GENOTYPE_HETEROZYGOUS; }
    public boolean is_unknown_genotype() { return this.genotype ==GENOTYPE_UNKNOWN; }
    public boolean genotype_not_initialized() { return this.genotype == GENOTYPE_NOT_INITIALIZED; }
    /** 
     * @return true if this variant is a nonsynonymous substitution (missense).
     */
    public boolean is_missense_variant() { 
	if (annot == null)
	    return false;
	else return (annot.getVariantType() == MISSENSE);
    }
    public String getVCFline() { return this.vcfLine; }
   
    public char ref_as_char() { return ref.charAt(0); }
    public char var_as_char() { return var.charAt(0); }

    public boolean is_single_nucleotide_variant () { return (this.ref.length()==1 && this.var.length()==1); }
    /** Return the list of "ITriage objects that represent the result of filtering */
    public ArrayList<ITriage> get_triage_list() { return this.triage_list; }
     /**
     * @return an integer representation of the chromosome  (note: X=23, Y=24).
     */
    public int get_chromosome() { return chromosome; }
    /**
     * @return an byte representation of the chromosome, e.g., 1,2,...,22 (note: X=23, Y=24, MT=25).
     */
    public byte getChromosomeAsByte() { return chromosome; }
   

    public boolean is_X_chromosomal() { return this.chromosome == X_CHROMOSOME;  }
    
   
    public boolean passes_variant_quality_threshold(int threshold) { return this.variant_quality >= threshold; }
    public float get_variant_quality() { return this.variant_quality; }
    public String get_genotype_as_string() {
	switch(this.genotype) {
	case GENOTYPE_NOT_INITIALIZED: return "not initialized";
	case GENOTYPE_HOMOZYGOUS_REF: return "hom. ref";
	case GENOTYPE_HOMOZYGOUS_ALT: return "hom. alt";
	case GENOTYPE_HETEROZYGOUS: return "het.";
	case GENOTYPE_UNKNOWN: return  "unknown";
	}
	return "?";
    }

    public void setDP4(int[] dp4) {
	if (dp4.length != 4) { 
	    System.err.println("Error, length of DP4 array for initializing Variant was not 4 but " + dp4.length);
	    System.exit(1);
	}
	this.DP4 = new int[4];
	for (int i=0;i<4;++i) DP4[i] = dp4[i];
    }
    public int[] getDP4() { return this.DP4; }

    public String get_position_as_string() { return Integer.toString(this.position); }
   

    public String get_chromosomal_mutation() {
	StringBuilder sb = new StringBuilder();
	sb.append( get_chrom_string() );
	sb.append(":g.");
	sb.append(this.position + ref + ">" + var);
	return sb.toString();
    }

    /**
     * @return an String representation of the chromosome (e.g., chr3, chrX).
     */
    public String get_chrom_string() {
	StringBuilder sb = new StringBuilder();
	sb.append("chr");
	if (chromosome ==  X_CHROMOSOME ) sb.append("X");
	else if (chromosome ==  Y_CHROMOSOME ) sb.append("Y");
	else if (chromosome ==  M_CHROMOSOME ) sb.append("M");
	else sb.append(chromosome);
	return sb.toString();
    }


    /**
     * Get representation of current variant as a string. This method
     * retrieves the annotation of the variant stored in the
     * {@link exomizer.reference.Annotation Annotation} object. If this
     * is not initialized (which should never happen), it returns ".".
     *<p>
     * @return The annotation of the current variant.
     */
    public String getAnnotation()
    {
	if (this.annot != null)
	    return this.annot.getVariantAnnotation();
	else return ".";
    }



    // ##########   UTILITY FUNCTIONS ##################### //

    /**
     * @param c a String representation of a chromosome (e.g., chr3, chrX).
     * @return corresponding integer (e.g., 3, 23).
     */
    public byte convertChromosomeStringToByteValue(String c) {
	if (c.startsWith("chr")) c = c.substring(3);
	if (c.equals("X") ) return 23;
	if (c.equals("23")) return 23;
	if (c.equals("Y") ) return 24;
	if (c.equals("24")) return 24;
	if (c.equals("M") ) return 25;
	if (c.equals("MT") ) return 25;
	if (c.equals("25") ) return 25;
	Byte i = null;
	try {
	    i = Byte.parseByte(c);
	} catch (NumberFormatException e) {
	    System.err.println("[SNV.java] Could not parse Chromosome string \"" + c + "\"");
	    throw e;
	}
	return i;
    }
    
    /**
     * @return A String representing the variant in the chromosomal sequence, e.g., chr17:c.73221527G>A
     */
    public String getChromosomalVariant() {
	return String.format("%s:c.%d%s>%s",get_chrom_string(), position, ref, var);
    }



    public String toString() {
	StringBuilder sb = new StringBuilder();
	String chr = get_chrom_string();
	sb.append("\t"+ chr + ":c." + position + ref +">" + var);
	if (annot != null)
	    sb.append("\t: "+ getAnnotation() + "\n");
	else
	    sb.append("\tcds mutation not initialized\n");
	if (genotype == GENOTYPE_HOMOZYGOUS_REF)
	    sb.append("\tGenotype: homzygous ref\n");
	else if (genotype == GENOTYPE_HOMOZYGOUS_ALT)
	    sb.append("\tGenotype: homzygous var\n");
	else if (genotype == GENOTYPE_HETEROZYGOUS)
	    sb.append("\tGenotype: heterozygous\n");
	else 
	    sb.append("\tGenotype not known or not initialized");
	sb.append("\tType: " + get_variant_type_as_string() + "\n");
	
	return sb.toString();

    }
    /**
     * The variant types (e.e., MISSENSE, NONSENSE) are stored internally as byte values.
     * This function converts these byte values into strings.
     * @return A string representing the type of the current variant.
     */
     public String get_variant_type_as_string()
    {
	if (this.annot == null)
	    return "uninitialized";
	else
	    return this.annot.getVariantTypeAsString();
    }



    
    /**
     * Sort based on chromosome and position.
     */
    @Override
    public int compareTo(Variant other) {
	if (other.chromosome > this.chromosome) return -1;
	else if (other.chromosome < this.chromosome) return 1;
	else if (other.position > this.position) return -1;
	else if (other.position < this.position) return 1;
	else return 0;
    }
}