package de.charite.compbio.jannovar.hgvs.bridge;

import com.google.common.io.Files;
import de.charite.compbio.jannovar.annotation.InvalidGenomeVariant;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.hgvs.HGVSVariant;
import de.charite.compbio.jannovar.hgvs.nts.variant.SingleAlleleNucleotideVariant;
import de.charite.compbio.jannovar.hgvs.parser.HGVSParser;
import de.charite.compbio.jannovar.reference.GenomeVariant;
import de.charite.compbio.jannovar.utils.ResourceUtils;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Tests using FBN1 transcript that is on the reverse strand.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 */
public class NucleotideInsertionToGenomeVariantForwardStrandTest {

	/**
	 * path to Jannovar database file
	 */
	static String dbPath;
	/**
	 * path to Jannovar database file
	 */
	static String fastaPath;
	/**
	 * Jannovar database
	 */
	JannovarData jannovarData;
	/**
	 * Translation of NucleotideChange to GenomeVariant
	 */
	NucleotideChangeToGenomeVariantTranslator translator;

	@BeforeClass
	public static void setUpClass() throws Exception {
		// copy out files to temporary directory
		File tmpDir = Files.createTempDir();
		dbPath = tmpDir + "/mini_ctns.ser";
		ResourceUtils.copyResourceToFile("/ex_ctns/mini_ctns.ser", new File(dbPath));
		fastaPath = tmpDir + "/ref.fa";
		ResourceUtils.copyResourceToFile("/ex_ctns/ref.fa", new File(fastaPath));
		String faiPath = tmpDir + "/ref.fa.fai";
		ResourceUtils.copyResourceToFile("/ex_ctns/ref.fa.fai", new File(faiPath));
		String dictPath = tmpDir + "/ref.dict";
		ResourceUtils.copyResourceToFile("/ex_ctns/ref.dict", new File(dictPath));
	}

	@Before
	public void setUp() throws FileNotFoundException, SerializationException {
		jannovarData = new JannovarDataSerializer(dbPath).load();

		IndexedFastaSequenceFile fastaFile = new IndexedFastaSequenceFile(new File(fastaPath));
		translator = new NucleotideChangeToGenomeVariantTranslator(jannovarData, fastaFile);
	}

	@Test
	public void testRangeInCDS() throws CannotTranslateHGVSVariant, InvalidGenomeVariant {
		String hgvsStr = "NM_004937.2(CTNS):c.400_401insATCT";
		HGVSVariant hgvsVar = new HGVSParser().parseHGVSString(hgvsStr);
		Assert.assertEquals(hgvsStr, hgvsVar.toHGVSString());

		SingleAlleleNucleotideVariant saVar = (SingleAlleleNucleotideVariant) hgvsVar;
		GenomeVariant gVar = translator.translateNucleotideVariantToGenomeVariant(saVar);
		Assert.assertEquals("ref:g.58585_58586insATCT", gVar.toString());
	}

}
