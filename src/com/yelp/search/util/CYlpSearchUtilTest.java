package com.yelp.search.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.yelp.enums.EylpScanMethod;
import com.yelp.exception.CYlpNullArgumentException;
import com.yelp.search.keyword.CYlpProximitySearchKeyword;

public class CYlpSearchUtilTest {

	CYlpSearchUtil testSearchUtil;

	@Before
	public void setUp() {
		testSearchUtil = new CYlpSearchUtil();
	}

	public static void main(String args[]) {
		org.junit.runner.JUnitCore
				.main("com.yelp.search.uti.CYlpSearchUtilTest");
	}

	@Test
	/**
	 * This unit test tests removeNonQualitativeTermsInSearch() using a test
	 * search string Resultant output should not contain any non-qualitative
	 * words like a, is, this etc..
	 * @throws CYlpNullArgumentException	 * 
	 */
	public void testRemoveNonQualitativeTermsInSearch() throws CYlpNullArgumentException{
		final String testString = "This is a amazing pizza";
		List<String> keywordList = testSearchUtil
				.removeNonQualitativeTermsInSearch(testString);
		List<String> expectedList = new ArrayList<String>();
		expectedList.add("amazing");
		expectedList.add("pizza");

		assertEquals("Expected keyword List to contain only qualitative words",
				expectedList, keywordList);

	}

	@Test
	/**
	 * This unit test tests expectedNonQualitativeTerms() which returns list of
	 * non-qualitative terms If any new terms are added to non qualitative term
	 * then they needed to be added to unit test aswell
	 */
	public void testGetNonQualitativeTerms() {
		// make sure to update this array with new terms as they are added
		String[] expectedNonQualitativeTerms = { "IS", "THIS", "AND", "A" };
		assertArrayEquals("Expected non qualitative term list to match",
				expectedNonQualitativeTerms, testSearchUtil
						.getNonQualitativeTerms().toArray());
	}

	@Test
	/**
	 * This unit test tests testGetCombinations(), 
	 * using a test string "Deep Dish Pizza" 
	 * The expected output should be a map with all the combinations of the strings
	 * 
	 * For example: keywordList is deep dish pizza, then this method will return
	 * a map which looks key | value 
	 * [3 | deep dish pizza]
	 * [ 2 | <deep dish, deep pizza, dish pizza>] 
	 * [1 | <deep,dish,pizza>]
	 * @throws CYlpNullArgumentException	 * 
	 * 
	 */
	public void testGetCombinations() throws CYlpNullArgumentException{

		List<String> keywordList = new ArrayList<String>();
		keywordList.add("Deep");
		keywordList.add("Dish");
		keywordList.add("Pizza");
		Map<Integer, List> combinationTable = testSearchUtil
				.getCombinations(keywordList);

		// expecting three entries in table based on length of keyword strings
		// for length 3 only one entry Deep Dish Pizza
		List expectedList = new ArrayList();
		expectedList.add("Deep Dish Pizza");
		// get list of string combination with keyword length 3
		Object[] output = ((List) combinationTable.get(3)).toArray();
		assertArrayEquals("Expecting combination to match", expectedList
				.toArray(), output);

		// for length 2 expecting Deep Dish, Deep Pizza, Dish Pizza
		expectedList.clear();
		expectedList.add("Deep Dish");
		expectedList.add("Deep Pizza");
		expectedList.add("Dish Pizza");

		// get list of string combination with keyword length 2
		output = ((List) combinationTable.get(2)).toArray();
		assertArrayEquals("Expecting combination to match", expectedList
				.toArray(), output);

		// for length 2 expecting Deep Dish, Deep Pizza, Dish Pizza
		expectedList.clear();
		expectedList.add("Deep");
		expectedList.add("Dish");
		expectedList.add("Pizza");

		// get list of string combination with keyword length 1
		output = ((List) combinationTable.get(1)).toArray();
		assertArrayEquals("Expecting combination to match", expectedList
				.toArray(), output);

	}

	@Test
	/**
	 * This unit test tests getFirstMatchingKeyword(), 
	 * For example
	 * if input string is
	 * "I like fish. Little star's deep dish pizza sure is fantastic. Dogs are
	 * funny. Deep Dish pizza is yummy" 
	 * 
	 * then this method will return a map which looks (key:keyword, value:list of start indexes)
	 * key | value 
	 * [deep dish pizza | <27,78>]
	 * @throws CYlpNullArgumentException
	 */
	public void testGetFirstMatchingKeyword() throws CYlpNullArgumentException {

		/***********************************************************************
		 * Test 1 with exact same search word present in doc to search
		 **********************************************************************/

		final String docString1 = "I like fish. Little star's deep dish pizza sure is fantastic."
				+ " Dogs are funny. Deep Dish pizza is yummy";

		Map<Integer, List> combinationTable = new HashMap<Integer, List>();
		List combListLength1 = new ArrayList();
		combListLength1.add("deep");
		combListLength1.add("dish");
		combListLength1.add("pizza");
		combinationTable.put(1, combListLength1);

		List combListLength2 = new ArrayList();
		combListLength2.add("deep dish");
		combListLength2.add("dish pizza");
		combListLength2.add("deep pizza");
		combinationTable.put(2, combListLength2);

		List combListLength3 = new ArrayList();
		combListLength3.clear();
		combListLength3.add("deep dish pizza");
		combinationTable.put(3, combListLength3);

	

		Map<String, List> keywordMatchMap = testSearchUtil
				.getFirstMatchingKeyword(docString1, combinationTable);

		// since getFirst Matching keywords returns first matching keyword of
		// length l here l=3, Example: Deep Dish Pizza
		// expecting one entry Deep Dish Pizza and its two occurences at 27,78

		// assert that map has only one entry
		assertEquals("Expecting only one entry in map ", 1, keywordMatchMap
				.size());

		// That one entry should have a list of occurences for deep dish pizza,
		// at 27,78
		Integer[] expectedKeywordStartIndexes1 = new Integer[] { 27, 78 };
		// get list of indexes for keyword deep pizza
		List indexList = keywordMatchMap.get("deep dish pizza");
		Object[] actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=3)",
				expectedKeywordStartIndexes1, actualStartIndexesFromFunction);

		/***********************************************************************
		 * Test 2 with only combination of length2 present in doc to search.
		 * Example: Deep Pizza or Deep Dish
		 **********************************************************************/

		final String docString2 = "This pizza is great, is it a deep dish one? In 1971 Lou and his wife Jean opened "
				+ "a pizzeria in the suburb of Lincolnwood, north of Chicago.   The rest, as they say, is history. Deep Pizza is also good. "
				+ "Pizza deep will be ignored but dEEp piZZa would not be";

		keywordMatchMap = testSearchUtil.getFirstMatchingKeyword(docString2,
				combinationTable);


		// since getFirst Matching keywords returns first matching keyword of
		// length l here l = 2 because there are no matching keywords with l=3
		// there will be two elements in map: 1 deep dish with occurences at
		// <29>
		// 2 deep pizza with occurences at <177,233>

		// assert that map has two entries one for deep pizza and disha pizza
		assertEquals("Expecting two entries in map ", 2, keywordMatchMap.size());

		Integer[] expectedKeywordStartIndexes2 = new Integer[] { 29 };
		// get list of indexes for keyword deep pizza
		indexList = keywordMatchMap.get("deep dish");
		actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=2)",
				expectedKeywordStartIndexes2, actualStartIndexesFromFunction);

		Integer[] expectedKeywordStartIndexes3 = new Integer[] { 177, 233 };
		indexList = keywordMatchMap.get("deep pizza");
		actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=2)",
				expectedKeywordStartIndexes3, actualStartIndexesFromFunction);

		/***********************************************************************
		 * Test 3 with only combination of length 1 present in doc to search.
		 * Example: Pizza or Deep or Dish
		 **********************************************************************/

		final String docString3 = "This pizza is great, is it a deep brand one? In 1971 Lou and his wife Jean opened "
				+ "a pizzeria in the suburb of Lincolnwood, north of Chicago.   The rest, as they say, is history. Dish is also good. "
				+ "Pizza deep will not be ignored this time";

		keywordMatchMap = testSearchUtil.getFirstMatchingKeyword(docString3,
				combinationTable);
	

		// since getFirst Matching keywords returns first matching keyword of
		// length l here l = 1 because there are no matching keywords with l=3
		// or 2
		// there will be three elements in map: 1 dish with occurences at <178>
		// 2 pizza with occurences at <5,197>
		// 3 deep with occurences at <29,203>

		// assert that map has two entries one for deep pizza and disha pizza
		assertEquals("Expecting three entries in map ", 3, keywordMatchMap
				.size());

		Integer[] expectedKeywordStartIndexes4 = new Integer[] { 178 };
		// get list of indexes for keyword deep pizza
		indexList = keywordMatchMap.get("dish");
		actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=1)",
				expectedKeywordStartIndexes4, actualStartIndexesFromFunction);

		Integer[] expectedKeywordStartIndexes5 = new Integer[] { 5, 197 };
		indexList = keywordMatchMap.get("pizza");
		actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=1)",
				expectedKeywordStartIndexes5, actualStartIndexesFromFunction);

		Integer[] expectedKeywordStartIndexes6 = new Integer[] { 29, 203 };
		indexList = keywordMatchMap.get("deep");
		actualStartIndexesFromFunction = indexList.toArray();
		assertArrayEquals(
				"Expecting start indexes from function to match expected(l=1)",
				expectedKeywordStartIndexes6, actualStartIndexesFromFunction);

	}

	/**
	 * Test that the method is able to construct the regular expression Input a
	 * search string, expected the output string to be appended by | and (?i)
	 * added to front
	 * 
	 * @throws CYlpNullArgumentException
	 */
	@Test
	public void testGetegexToMatchOneOrMoreKeywords() throws CYlpNullArgumentException {

		final String searchString = "Deep Dish Pizza is Amazing";
		String actualOutputReturnedByFunc = testSearchUtil
				.getRegexToMatchOneOrMoreKeywords(searchString);
		String expectedOutput = "(?i)Deep|(?i)Dish|(?i)Pizza|(?i)Amazing";
		assertEquals("Expected the regex expression to match ", expectedOutput,
				actualOutputReturnedByFunc);

	}

	/**
	 * Test to verify that getCountOfAllKeywords() returns count of all keywords
	 * in docString that match regular expression
	 * 
	 * @throws CYlpNullArgumentException
	 */
	@Test
	public void testGetCountOfAllKeyword() throws CYlpNullArgumentException {

		final String docString = "I like fish. Little star's deep dish pizza sure is fantastic."
				+ " Dogs are funny. Deep Dish pizza is yummy. Pizzas are full of nutrition,esp deep dish pizzas are good. They can be"
				+ "made using deep dish too. I have a flat dish. Can we order pizza tonight?";
		String regex = "(?i)Deep|(?i)Dish|(?i)Pizza";
		int expectedCount = 14; // count of all keywords in doc string
		int ActualCountReturnedByFunc = testSearchUtil.getCountOfAllKeywords(
				docString, regex);
		assertEquals("Expected the count of all keywords to match ",
				expectedCount, ActualCountReturnedByFunc);

	}

	/**
	 * tests proximitySearch() by using a sample string, proximity search is
	 * expected to return the document snippet that has maximum number of
	 * keyword occurences in the proximity as defined by MAX_LENGTH One known
	 * issue is if there are two document snippets with same number of
	 * occurences it will return first document snippet (that was scanned)
	 * This method provides the core logic of selecting relevant doc snippet
	 * @throws CYlpNullArgumentException
	 */

	@Test
	public void testProximitySearch() throws CYlpNullArgumentException {

		final String docToSearch = "I like fish. Little star's deep dish pizza sure is fantastic."
				+ " Dogs are funny. Deep Dish pizza is yummy. Pizzas are full of nutrition,esp deep dish pizzas are good. They can be"
				+ "made using deep dish too. I have a flat dish. Can we order pizza tonight? What makes Chicago deep dish pizza different"
				+ " is of course that it is not very much like a  classic Italian pizza, with thin crusts and delicate toppings.  During the"
				+ " depression of the thirties, followed by the war years of the forties, Americans ate one-dish meals of casseroles"
				+ " -- easily procured ingredients that would satisfy the stomach, stretch the budget and not cost many ration coupons."
				+ " Therefore, the more you could load onto a pizza crust, the better it would be, and doubtless a deep pan would be more "
				+ "like a casserole.   Moreover, the crust would not need the fancy stretching and pushing, even tossing, that the traditional"
				+ "  Italian thin pizza would require.  The mozzarella cheese would be on the bottom and the crust and toppings would all  "
				+ "bake and ooze together and become one of America's legacies to fat foods nationwide and waistwide.  "
				+ "It's an indulgence a teenager could hardly resist after a ballgame, let alone someone who might be escaping the chill"
				+ " of the Windy City on a snowy night.";

		// construct map with key as largest keyword match and value as list of
		// start indexes in the document to search

		Map<String, List> KeywordIndexTable = new HashMap<String, List>();
		List<Integer> startIndexes = Arrays.asList(27, 78, 137, 268);
		KeywordIndexTable.put("deep dish pizza", startIndexes);

		final String searchString = "Deep Dish Pizza";
		int MAX_LENGTH = 100;

		CYlpProximitySearchKeyword outputProximitySearchKeyword = testSearchUtil
				.proximitySearch(docToSearch, searchString, KeywordIndexTable,
						MAX_LENGTH);

		// assert based on max occurences
		int expectedNumberOfKeywordInBestMatch = 7;
		int expectedStartIndexOfBestMatch = 10;
		String expectedDocumentSnippet = "deep dish pizza sure is fantastic. Dogs are funny. Deep Dish pizza is yummy. Pizzas are full of nutrition";
		EylpScanMethod expectedScanMethod = EylpScanMethod.BACKWARDSCAN;

	}

	/**
	 * Tests the function getRoundedOffDocSubString(), sets up a sample string
	 * which is truncated un-evenly at word boundaries this string is passed to
	 * getRoundedOffDocSubString(), we expect a string rounded off at word
	 * boundaries
	 * @throws CYlpNullArgumentException 
	 */
	@Test
	public void testGetRoundedOffDocSubString() throws CYlpNullArgumentException {

		final String docString = "I like fish. Little star's deep dish pizza sure is fantastic.";
		int startIndex = 16;
		int endIndex = 28;
		// the substring would be: "tle star's d"
		// calling getRoundedOffDocSubString() should result in Little star's
		// deep"
		String expectedOutputFromRoundOffFunc = "Little star's deep";
		String actualOutputFromRoundOffFunc = testSearchUtil
				.getRoundedOffDocSubString(docString, startIndex, endIndex);
		assertEquals(
				"Expecting the input string to be truncated to nearest word",
				expectedOutputFromRoundOffFunc, actualOutputFromRoundOffFunc);

	}

	/**
	 * Tests the function getThePaddedSnippet(), which pads the keyword occurences with
	 * padBefore and padAfter strings
	 * 
	 * @throws CYlpNullArgumentException 
	 */
	@Test
	public void testGetThePaddedSnippet() throws CYlpNullArgumentException{

		final String docString = "deep dish pizza sure is fantastic. Dogs are funny. Deep Dish pizza is yummy. Dish should be deep. "
				+ "Deep dish is best when hot. Dish pizza is also ok";
		final String searchString = "deep dish pizza";

		String padBefore = "[[HIGHLIGHT]]";
		String padAfter = "[[ENDHIGHLIGHT]]";

		String expectedOutputFromRoundOffFunc = "[[HIGHLIGHT]]deep dish pizza [[ENDHIGHLIGHT]]sure is fantastic. Dogs are funny. " +
												"[[HIGHLIGHT]]Deep Dish pizza [[ENDHIGHLIGHT]]is yummy. [[HIGHLIGHT]]Dish [[ENDHIGHLIGHT]]should" +
												" be [[HIGHLIGHT]]deep. Deep dish [[ENDHIGHLIGHT]]is best when hot. [[HIGHLIGHT]]Dish pizza" +
												" [[ENDHIGHLIGHT]]is also ok";
		String actualOutputFromRoundOffFunc = testSearchUtil
				.getThePaddedSnippet(docString, searchString, padBefore,
						padAfter);


		assertEquals("Expecting the output to be padded",expectedOutputFromRoundOffFunc,actualOutputFromRoundOffFunc);

	}

	@After
	public void tearDown() {
		testSearchUtil = null;
		System.gc();
	}

}
