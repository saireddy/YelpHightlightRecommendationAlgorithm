package com.yelp.search.highlight;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yelp.exception.CYlpNullArgumentException;
import com.yelp.search.util.CYlpSearchUtil;

/**
 * unit test class that tests the methods of CYlpDocumentHighlight
 * @author bsaireddy
 *
 */
public class CYlpDocumentHighlightTest {

	CYlpDocumentHighlight testDocHighlight;

	@Before
	public void setUp() {
		testDocHighlight = new CYlpDocumentHighlight();
	}

	public static void main(String args[]) {
		org.junit.runner.JUnitCore
				.main("com.yelp.search.uti.CYlpDocumentHighlightTest");
	}

	@Test
	public void testGetHighlightDoc() throws CYlpNullArgumentException {

		String documentToScan1;
		String searchQuery1;

		/***********************************************************************
		 * Test 1 : Simple test to see if our function returns relevant snippet
		 **********************************************************************/
		documentToScan1 = "I couldn't get that pizza out of my mind after trying it for "
				+ "the first time a few weeks ago so when our plans to go to the drive in movies "
				+ "fell through I told hubby let's get some pizza and watch movies at home."
				+ "So once again we tried the deep dish pepperoni pizza and once again it was amazingly good!"
				+ "We also tried the mozerella bread this time which was tasty but a little too buttery for my taste. "
				+ " While hubby was in the store they gave him a big sample of a calzone. He brought a couple bites back "
				+ "for me and it was great.   They even gave him a magnet which I will totally apply to my refrigerator! "
				+ "Love you Rocky's! Can't wait to try more of the menu.";
		searchQuery1 = "deep dish pizza";

		String expectedOutputFromFunction = "and watch movies at home.So once again we tried the [[HIGHLIGHT]]deep dish "
				+ "[[ENDHIGHLIGHT]]pepperoni [[HIGHLIGHT]]pizza [[ENDHIGHLIGHT]]and once again it was amazingly";

		String actualOutputFromFunction = testDocHighlight.getHighlightDoc(
				documentToScan1, searchQuery1);

		assertEquals(
				"Expecting the document snippet to match snippet with max occurence in proximity ",
				expectedOutputFromFunction, actualOutputFromFunction);

		/***********************************************************************
		 * Test 2 : NULL check on doc expecting CYlpNullArgumentException
		 **********************************************************************/
		String documentToScan2 = null;
		String searchQuery2 = "test";

		try {

			actualOutputFromFunction = testDocHighlight.getHighlightDoc(
					documentToScan2, searchQuery2);
			fail("Should not get here, expecting it to throw exception");

		} catch (CYlpNullArgumentException nullArgEx) {
			// exception means we are ok
			assertTrue(1 == 1);
		}

		/***********************************************************************
		 * Test 3 : NULL check on search expecting CYlpNullArgumentException
		 **********************************************************************/
		String documentToScan3 = "test";
		String searchQuery3 = null;

		try {

			actualOutputFromFunction = testDocHighlight.getHighlightDoc(
					documentToScan3, searchQuery3);
			fail("Should not get here, expecting it to throw exception");

		} catch (CYlpNullArgumentException nullArgEx) {
			// exception means we are ok
			assertTrue(1 == 1);
		}

		/***********************************************************************
		 * Test 4 : NULL check on search expecting CYlpNullArgumentException
		 **********************************************************************/
		String documentToScan4 = null;
		String searchQuery4 = null;

		try {

			actualOutputFromFunction = testDocHighlight.getHighlightDoc(
					documentToScan4, searchQuery4);
			fail("Should not get here, expecting it to throw exception");

		} catch (CYlpNullArgumentException nullArgEx) {
			// exception means we are ok
			assertTrue(1 == 1);
		}

		/***********************************************************************
		 * Test 5 : one word doc with matching word in search
		 **********************************************************************/
		String documentToScan5 = "test";
		String searchQuery5 = "given that it is a test";

		actualOutputFromFunction = testDocHighlight.getHighlightDoc(
				documentToScan5, searchQuery5);
		expectedOutputFromFunction = "[[HIGHLIGHT]]test[[ENDHIGHLIGHT]]";
		assertEquals("Expecting matched one word to be padded ",
				expectedOutputFromFunction, actualOutputFromFunction);

		/***********************************************************************
		 * Test 6 : one word doc with NO matching word in search
		 **********************************************************************/
		String documentToScan6 = "table";
		String searchQuery6 = "given that it is a test";

		actualOutputFromFunction = testDocHighlight.getHighlightDoc(
				documentToScan6, searchQuery6);
		expectedOutputFromFunction = "table";
		assertEquals(
				"When no match return first MAX_lENGTH of documentToScan5 ",
				expectedOutputFromFunction, actualOutputFromFunction);

		/***********************************************************************
		 * Test 7 : multi word doc with NO matching word in search
		 **********************************************************************/
		String documentToScan7 = "that is, a sentence-break iterator returns breaks that each "
				+ "represent the end of one sentence and the beginning of the next. With the " 
				+ "word-break iterator, the characters between two boundaries might be"
				+ " a word, or they might be the punctuation or whitespace between two words.";
		String searchQuery7 = "deep dish pizza";

		actualOutputFromFunction = testDocHighlight.getHighlightDoc(
				documentToScan7, searchQuery7);
		//first MAX_LENGTH chars
		expectedOutputFromFunction = "that is, a sentence-break iterator returns breaks that each represent the end of one sentence and the";
		assertEquals(
				"When no match return first MAX_lENGTH of documentToScan5 ",
				expectedOutputFromFunction, actualOutputFromFunction);

		/***********************************************************************
		 * Test 8 : doc string is less than MAX LENGTH but has matching keywords
		 **********************************************************************/
		String documentToScan8 = "Little star's deep dish pizza sure is fantastic";
		String searchQuery8 = "deep dish pizza";

		actualOutputFromFunction = testDocHighlight.getHighlightDoc(
				documentToScan8, searchQuery8);
		expectedOutputFromFunction = "Little star's [[HIGHLIGHT]]deep dish pizza [[ENDHIGHLIGHT]]sure is fantastic";
		assertEquals(
				"When no match return first MAX_lENGTH of documentToScan5 ",
				expectedOutputFromFunction, actualOutputFromFunction);
		
		
	}

	@After
	public void tearDown() {
		testDocHighlight = null;
		System.gc();
	}
}
