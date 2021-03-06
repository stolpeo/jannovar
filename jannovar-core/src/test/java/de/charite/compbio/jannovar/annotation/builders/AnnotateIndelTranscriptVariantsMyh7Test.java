package de.charite.compbio.jannovar.annotation.builders;

import static org.junit.Assert.assertEquals;

import com.google.common.io.Files;
import de.charite.compbio.jannovar.annotation.Annotation;
import de.charite.compbio.jannovar.annotation.InvalidGenomeVariant;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.hgvs.AminoAcidCode;
import de.charite.compbio.jannovar.reference.GenomePosition;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.reference.PositionType;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import de.charite.compbio.jannovar.testutils.ResourceUtils;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests for annotating variants that lie on transcripts which align to the reference with indels.
 *
 * gene: MYH7, transcript: NM_000257.2
 *
 * @author <a href="mailto:manuel.holtgrewe@charite.de">Manuel Holtgrewe</a>
 */
@RunWith(Parameterized.class) public class AnnotateIndelTranscriptVariantsMyh7Test {

	@Parameters(name = "{index}: {0} => {1}/{2}/{3}") public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			// Cover LTBP4 more or less systematically, VV uses NP_000248;       //__VV prediction__
			{ "14-23902942-G-T", "g.23902942G>T", "c.-1C>A", "(=)" },            // p.?
			{ "14-23902941-T-C", "g.23902941T>C", "c.1A>G", "0?" },              // p.(Met1?)
			{ "14-23896042-C-T", "g.23896042C>T", "c.1988G>A", "(Arg663His)" },  // p.(Arg663His)
			{ "14-23893328-G-A", "g.23893328G>A", "c.2710C>T", "(Arg904Cys)" },  // p.(Arg904Cys)
			{ "14-23897862-C-A", "g.23897862C>A", "c.1425G>T", "(Gln475His)" },  // p.(Gln475His)
			{ "14-23901875-C-T", "g.23901875C>T", "c.475G>A", "(Asp159Asn)" },   // p.(Asp159Asn)
			{ "14-23893148-C-G", "g.23893148C>G", "c.2890G>C", "(Val964Leu)" },  // p.(Val964Leu)
			{ "14-23898214-G-A", "g.23898214G>A", "c.1357C>T", "(Arg453Cys)" },  // p.(Arg453Cys)
			{ "14-23882991-T-C", "g.23882991T>C", "c.5767A>G", "(Lys1923Glu)" }, // p.(Lys1923Glu)
			{ "14-23899059-C-T", "g.23899059C>T", "c.1063G>A", "(Ala355Thr)" },  // p.(Ala355Thr)
			{ "14-23891465-C-T", "g.23891465C>T", "c.3169G>A", "(Gly1057Ser)" }, // p.(Gly1057Ser)
			{ "14-23898288-G-T", "g.23898288G>T", "c.1283C>A", "(Ala428Asp)" },  // p.(Ala428Asp)
			{ "14-23896043-G-A", "g.23896043G>A", "c.1987C>T", "(Arg663Cys)" },  // p.(Arg663Cys)
			{ "14-23900849-G-A", "g.23900849G>A", "c.677C>T", "(Ala226Val)" },   // p.(Ala226Val)
			// NB: the following was not predicted as "stop gained" prior to Jannovar v0.31
			{ "14-23886380-C-A", "g.23886380C>A", "c.4501G>T", "(Glu1501*)" },   // p.(Glu1501Ter)
		});
	}

	@Parameter(/*0*/) public String fInput;

	@Parameter(1) public String fExpectedGenomic;
	@Parameter(2) public String fExpectedNucleotides;
	@Parameter(3) public String fExpectedProtein;

	/**
	 * Path to .ser file.
	 */
	static String dbPath;

	/**
	 * The {@link JannovarData} to load the test data into.
	 */
	static JannovarData jvData;

	/**
	 * The transcript to annotate with.
	 */
	static TranscriptModel tx;

	/**
	 * Copy out .ser file to temporary directory for tests and load.
	 */
	@BeforeClass public static void setUpClass() throws Exception {
		File tmpDir = Files.createTempDir();
		dbPath = tmpDir + "/hg19_refseq_indels.ser";
		ResourceUtils.copyResourceToFile("/hg19_refseq_indels.ser", new File(dbPath));
		jvData = new JannovarDataSerializer(dbPath).load();

		tx = jvData.getTmByAccession().get("NM_000257.2");
	}

	@Test public void test() throws InvalidGenomeVariant {
		// Build genome change
		final String[] tokens = fInput.split("-");
		final GenomePosition gPos = new GenomePosition(jvData.getRefDict(), Strand.FWD,
			jvData.getRefDict().getContigNameToID().get(tokens[0]), Integer.parseInt(tokens[1]),
			PositionType.ONE_BASED);
		final GenomeVariant change = new GenomeVariant(gPos, tokens[2], tokens[3]);

		// Run code under test.
		final AnnotationBuilderDispatcher dispatcher = new AnnotationBuilderDispatcher(tx, change,
			new AnnotationBuilderOptions());
		final Annotation result = dispatcher.build();

		// Check expectations.
		assertEquals(fExpectedGenomic, result.getGenomicNTChangeStr());
		assertEquals(fExpectedNucleotides, result.getCDSNTChangeStr());
		assertEquals(fExpectedProtein,
			result.getProteinChange().toHGVSString(AminoAcidCode.THREE_LETTER));
	}

}
