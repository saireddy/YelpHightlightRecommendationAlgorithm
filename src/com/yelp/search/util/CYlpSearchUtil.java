package com.yelp.search.util;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yelp.enums.EylpScanMethod;
import com.yelp.exception.CYlpNullArgumentException;
import com.yelp.search.keyword.CYlpProximitySearchKeyword;

/**
 * Search utility class with utility methods that can be used for various search operations
 * @author bsaireddy
 *
 */
public class CYlpSearchUtil {

	/**
	 * Method removes all non qualitative words(: words that do not add value)
	 * from search string and returns a list of keywords that can be used to
	 * search As a first step it breaks the search string into words, by using
	 * delimiter as " " Then constructs a list with words not contained in list
	 * returned by getNonQualitativeTerms()
 	 * @throws CYlpNullArgumentException
	 * @param searchString
	 * @return list of keywords that are qualitative(add value to search)
	 */
	public List<String> removeNonQualitativeTermsInSearch(String searchString) throws CYlpNullArgumentException{

		if (searchString == null) {
			throw new CYlpNullArgumentException(
					"search string is null ");
		}    	
    			
		List<String> finalSearchWords = new ArrayList<String>();
		String delimiterRegex = getDelimiterRegex();
		String[] searchWords = searchString.split(delimiterRegex);
		List<String> nonQualitativeTerms = new ArrayList<String>();
		nonQualitativeTerms = getNonQualitativeTerms();
		for (int i = 0; i < searchWords.length; i++) {
			// we always expect the nonQualitativeTerms to be maintained in
			// UPPERCASE
			if (nonQualitativeTerms.contains(searchWords[i].toUpperCase())) {
				// skip
				continue;
			}
			finalSearchWords.add(searchWords[i]);
		}

		return finalSearchWords;

	}

	/**
	 * This method returns regex expression for delimiters
	 * @return regex that will be used to break search string into tokens
	 */
	public String getDelimiterRegex() {

		// we can read the regex of delimiter from a text file, for now we can
		// hardcode it
		// todo: read delimiter from file to make it code independent
		// regex that matches tokens that are separated by one or more blank
		String delimiter = "\\s+";
		return delimiter;

	}

	/**
	 * Method that returns list of words that are non qualitative : which do not
	 * add any value to search This method can be made code independent by
	 * reading the word list from a text file instead of hardcoding As a
	 * CONVENTION: the words are always UPPERCASED
	 * 
	 * @return
	 */
	public List<String> getNonQualitativeTerms() {
		List<String> nonQualitativeTerms = new ArrayList<String>();
		//todo: read the non-qualitative terms from file instead of hardcoding
		nonQualitativeTerms.add("IS");
		nonQualitativeTerms.add("THIS");
		nonQualitativeTerms.add("AND");
		nonQualitativeTerms.add("A");

		return nonQualitativeTerms;

	}

	/**
	 * This method is used to generate combination of the keywords in search
	 * query. Generate a Map with key as length of combination and value as list
	 * of different combination of keywords of that length
	 * 
	 * For example: keywordList is deep dish pizza, then this method will return
	 * a map which looks key | value 
	 * 				{	[3 | deep dish pizza] 
	 * 					[2 | <deep dish, deep pizza, dish pizza>]
	 * 					[1 | <deep,dish,pizza>]
	 * 
	 * The algorithm generates combinations based on what bits are set
	 * for example: if keywords = {deep, dish, pizza} then
	 * integer value 5 in binary notation 110--> deep dish  
	 * @param originalList
	 * @throws CYlpNullArgumentException 
	 * @return Map with key of length(number of keywords) and value as list of
	 *         keyword combinations
	 */
	public Map getCombinations(List<String> keywordList) throws CYlpNullArgumentException{

		if (keywordList == null) {
			throw new CYlpNullArgumentException(
					"keyword list is null ");
		}    	
		
		Map keywordCombinationTable = new HashMap<Integer, List<String>>();
		final String SPACE = " ";

		int keywordSize = keywordList.size();
		int threshold = Double.valueOf(Math.pow(2, keywordSize)).intValue() - 1;

		for (int i = 1; i <= threshold; ++i) {

			String currentCombination = new String();
			int count = keywordSize - 1;
			int numberOfKeyWords = 0;

			int valueToShift = i;
			while (count >= 0) {
				if ((valueToShift & 1) != 0) {
					currentCombination = currentCombination
							+ keywordList.get(keywordList.size() - count - 1)
							+ SPACE;
					numberOfKeyWords++;
				}

				valueToShift = valueToShift >>> 1;
				--count;
			}
			// populate the map according to length of keyword string

			if (currentCombination != null) {
				if (keywordCombinationTable.containsKey(numberOfKeyWords)) {
					List<String> keywordCombinations = (List<String>) keywordCombinationTable
							.get(numberOfKeyWords);
					keywordCombinations.add(currentCombination.trim());
					keywordCombinationTable.put(numberOfKeyWords,
							keywordCombinations);
				} else {
					List<String> keywordCombinations = new ArrayList<String>();
					keywordCombinations.add(currentCombination.trim());
					keywordCombinationTable.put(numberOfKeyWords,
							keywordCombinations);

				}
			}
		}

		return keywordCombinationTable;
	}

	/**
	 * This method tries to return first best possible matched keyword group
	 * 
	 * It starts it search with max length keyword combination, for example:"deep dish pizza"
	 * if found returns map with keyword and list of all startindexes where it occured
	 * 
	 * if it fails to find then it moves to next max length keyword combination, in our example
	 * it searches for matches of all 2 length combinations: deep dish, deep pizza, dish pizza
	 * it again construct map with keywords and indexes
	 * Lets say there are matches for deep dish and deep pizza it inserts two entries in map
	 * with above keywords and their list of start indexes -> this is to give equal weight to
	 * keyword matches with same length
	 * 
	 * 
	 * if it fails to find even that it moves to next length combinations until length is 0
	 * @return map with keyword as key and list with start index of keyword in the search string
	 * @throws CYlpNullArgumentException
	 */
	public Map getFirstMatchingKeyword(String documentToSearch,
			Map keywordCombinationTable) throws CYlpNullArgumentException {

		if (documentToSearch == null || keywordCombinationTable == null) {
			throw new CYlpNullArgumentException(
					"Either documentToSearch or combination map of keywords is null ");
		}

		Set<Integer> keySet = keywordCombinationTable.keySet();
		Map<String, List> KeywordIndexTable = new HashMap<String, List>();
		ArrayList<Integer> keysList = new ArrayList<Integer>(keySet);
		// sort in descending order using comparator
		Comparator<Integer> comparator = Collections.reverseOrder();
		Collections.sort(keysList, comparator);

		// flag which indicates if first match is found in which case we exit
		boolean foundMatch = false;

		// start from largest keyword length and look for a match in search
		// string

		for (int currentkeywordLength : keysList) {

			// get list of string combination corresponding to the keyword
			// length
			List<String> currentKeywordList = (List) keywordCombinationTable
					.get(currentkeywordLength);

			// we donot need to search for all occurence, just best possible
			// search string combination
			if (foundMatch) {
				// we found atleast one match we can quit our search and begin
				// proximity search now..
				break;
			}

			// iterate over list of string combinations and check if there is
			// match in search string
			for (String keyword : currentKeywordList) {
				// use regex to find for match
				// define regex which starting with (?i) to enable
				// case-insensitive matching

				String regex = "(?i)" + keyword;
				List<Integer> currKeywordStartIndexes = new ArrayList<Integer>();

				//
				// Obtain the required matcher
				//
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(documentToSearch);

				//
				// Find every match and store start index in a array
				//
				while (matcher.find()) {
					// if we find a match then add it to currKeywordStartIndexes List
					currKeywordStartIndexes.add(matcher.start());

				}
				// add it to map if we found atleast one match for keyword
				if (!currKeywordStartIndexes.isEmpty()) {
					KeywordIndexTable.put(keyword, currKeywordStartIndexes);
					foundMatch = true;
				}

			}
		}
		return KeywordIndexTable;

	}

	/**
	 * This method uses three scan method in the proximity of the keyword
	 * occurences(highest length keyword matches), the scan length is fixed at
	 * MAX_LENGTH that can be passed as argument. We start the scan at the
	 * keyword occurence(index stored in map) and count all the occurences of
	 * any keyword. The information of sub string (document snippet) with
	 * maximum occurences is kept track using instance of
	 * CYlpProximitySearchKeyword
	 * 
	 * @param docToSearch:
	 *            document that needs to be searched
	 * @param KeywordIndexTable :
	 *            table with keywords and list of start indexes
	 * @param MAX_LENGTH
	 *            max snippet length
	 * @throws CYlpNullArgumentException            
	 * @return instance of CYlpProximitySearchKeyword which contains the
	 *         document snippet with max occurences of keywords in proximity
	 */
	public CYlpProximitySearchKeyword proximitySearch(String docToSearch,
			String searchString, Map<String, List> KeywordIndexTable,
			int MAX_LENGTH) throws CYlpNullArgumentException {

		
		if (docToSearch == null || searchString == null ||  KeywordIndexTable == null) {
			throw new CYlpNullArgumentException(
					"Either search string or docToSearch or  KeywordIndexTable is null ");
		}
		
		
		// instance of CYlpProximitySearchKeyword will be used to keep track of
		// document snippet with maximum occurences - which is most relevant
		// string

		CYlpProximitySearchKeyword currentProximityKeyWord = new CYlpProximitySearchKeyword();
		
		//special case where there are no matching keywords, then get substring of docToSearch between
		//0 and MAX_LENGTH
		if(KeywordIndexTable.isEmpty()){
			
			//no matching keywords return text of length MAX_LENGTH
			String docToSet = getSubDocumentForEachStrategy(
					EylpScanMethod.BACKWARDSCAN, 0, MAX_LENGTH,
					docToSearch);
			currentProximityKeyWord.setDocumentSnippet(docToSet);
			currentProximityKeyWord.setNumberOfAllKeywordsOccurences(0);
			currentProximityKeyWord.setScanMethodForMaxOccurances(EylpScanMethod.BACKWARDSCAN);
			return currentProximityKeyWord;
		}
		
		

		// regex to match one or more keywords
		String regexToMatchOneOrMoreKeywords = getRegexToMatchOneOrMoreKeywords(searchString);
		Set<String> keySet = KeywordIndexTable.keySet();
		//
		// for each keyword in the keyset
		//
		for (String currentkeyword : keySet) {

			//
			// get start indexes for this keyword
			//
			List<Integer> currentStartIndexList = (List) KeywordIndexTable
					.get(currentkeyword);

			//
			// for each startindexes use three scanning methods to determine
			// document snippet
			// with maximum number of keyword matches
			//

			//
			// for each scan strategy we set startIndex and endIndex, get the
			// document section within those bounds
			// we then look for matches for any keywords within those section
			//
			int startIndex = 0;
			int endIndex = 0;
			int currCountOfAllKeywords = 0;

			for (int currentIndex : currentStartIndexList) {

				// STRATEGY 1: Use FORWARDSCAN

				// get sub document within the bounds to do proximity search for
				// FORWARDSCAN
				String subDocument = getSubDocumentForEachStrategy(
						EylpScanMethod.FORWARDSCAN, currentIndex, MAX_LENGTH,
						docToSearch);
				// get total count of all keyword occurences for FORWARDSCAN
				currCountOfAllKeywords = getCountOfAllKeywords(subDocument,
						regexToMatchOneOrMoreKeywords);

				// check if count of all keywords for current strategy is
				// greater then max count so far
				currentProximityKeyWord.updateProximityKeywordIfRequired(
						currCountOfAllKeywords, currentkeyword, startIndex,
						subDocument, EylpScanMethod.FORWARDSCAN);

				//
				// STRATEGY 2: Use MIDSCAN
				//

				// get sub document within the bounds to do proximity search for
				// MIDSCAN
				subDocument = getSubDocumentForEachStrategy(
						EylpScanMethod.MIDSCAN, currentIndex, MAX_LENGTH,
						docToSearch);
				// get total count of all keyword occurences for MIDSCAN
				currCountOfAllKeywords = getCountOfAllKeywords(subDocument,
						regexToMatchOneOrMoreKeywords);
				// check if count of all keywords for current strategy is
				// greater then max count so far
				currentProximityKeyWord.updateProximityKeywordIfRequired(
						currCountOfAllKeywords, currentkeyword, startIndex,
						subDocument, EylpScanMethod.MIDSCAN);

				//
				// STRATEGY 3: Use BACKWARDSCAN
				//

				// get sub document within the bounds to do proximity search for
				// BACKWARDSCAN
				subDocument = getSubDocumentForEachStrategy(
						EylpScanMethod.BACKWARDSCAN, currentIndex, MAX_LENGTH,
						docToSearch);
				// get total count of all keyword occurences for BACKWARDSCAN
				currCountOfAllKeywords = getCountOfAllKeywords(subDocument,
						regexToMatchOneOrMoreKeywords);
				// check if count of all keywords for current strategy is
				// greater then max count so far
				currentProximityKeyWord.updateProximityKeywordIfRequired(
						currCountOfAllKeywords, currentkeyword, startIndex,
						subDocument, EylpScanMethod.BACKWARDSCAN);

			}

		}

		return currentProximityKeyWord;
	}

	/**
	 * This method returns sub strings based on scan method 
	 * For example: for midscan we return string(currIndex-Max/2,,currIndex +Max/2) 
	 * @param scanMethod
	 * @param currentIndex
	 * @param MAX_LENGTH
	 * @param docToSearch
	 * @throws CYlpNullArgumentException 
	 * @return text(string) that is part of document according to scanMethod passed as arg
	 */
	public String getSubDocumentForEachStrategy(EylpScanMethod scanMethod,
			int currentIndex, int MAX_LENGTH, String docToSearch)throws CYlpNullArgumentException  {
		
		
		if (scanMethod == null || docToSearch == null ) {
			throw new CYlpNullArgumentException(
					"Either scanMethodor or docToSearch is null ");
		}
		
		int startIndex = 0;
		int endIndex = 0;

		if (scanMethod.equals(EylpScanMethod.FORWARDSCAN)) {

			if (currentIndex - MAX_LENGTH < 0) {
				// set start index to 0 and adjust end index to span MAX_LENGTH
				startIndex = 0;
				endIndex = (docToSearch.length() > MAX_LENGTH) ? MAX_LENGTH
						: docToSearch.length();
			} else {
				startIndex = currentIndex - MAX_LENGTH;
				endIndex = currentIndex;
			}
		} else if (scanMethod.equals(EylpScanMethod.MIDSCAN)) {
			if (currentIndex - MAX_LENGTH / 2 < 0) {
				// set start index to 0 and adjust end index to span MAX_LENGTH
				startIndex = 0;
				endIndex = (docToSearch.length() > MAX_LENGTH) ? MAX_LENGTH
						: docToSearch.length();
			} else {
				startIndex = currentIndex - MAX_LENGTH / 2;
				endIndex = currentIndex + MAX_LENGTH / 2;
				endIndex = (docToSearch.length() > currentIndex + MAX_LENGTH
						/ 2) ? currentIndex + MAX_LENGTH / 2 : docToSearch
						.length();
			}

		} else if (scanMethod.equals(EylpScanMethod.BACKWARDSCAN)) {
			if (currentIndex + MAX_LENGTH < docToSearch.length()) {

				startIndex = currentIndex;
				endIndex = currentIndex + MAX_LENGTH;
			} else {
				// set start index to curr and adjust end index to string length
				startIndex = currentIndex;
				endIndex = docToSearch.length();
			}
		}

		return getRoundedOffDocSubString(docToSearch, startIndex, endIndex);

	}

	/**
	 * Returns the string rounded to nearest word boundaries Example: The blue
	 * fox jumped over brown frog, if startindex and endindex is 3 and 14 -> "e
	 * blue fox j" Then Output from this function will be The blue fox jumped
	 * 
	 * @param docToSearch
	 * @param startIndex
	 * @param endIndex
	 * @throws CYlpNullArgumentException 
	 * @return string with text that rounded off to nearest word
	 */
	public String getRoundedOffDocSubString(String docToSearch, int startIndex,
			int endIndex) throws CYlpNullArgumentException {

		if (docToSearch == null) {
			throw new CYlpNullArgumentException(
					"docToSearch string is null ");
		}		
		//we can thrown one more exception for startindex < endIndex
		int newStartIndex = startIndex;
		int newEndIndex = endIndex;

		// give me substring that starts and ends with words not characters
		// using BreakIterator
		BreakIterator bIterator = BreakIterator.getWordInstance();
		bIterator.setText(docToSearch);
		
		startIndex = startIndex < docToSearch.length()? startIndex:docToSearch.length()-1;
		endIndex = endIndex < docToSearch.length()? endIndex:docToSearch.length()-1;	

		// check if the preceding word boundary is not the first word boundary
		if (bIterator.preceding(startIndex) != BreakIterator.DONE) 
		{   
			newStartIndex = bIterator.preceding(startIndex);
		} // check if following word boundary is not the last word boundary
		if (bIterator.following(endIndex) != BreakIterator.DONE) {
			newEndIndex = bIterator.following(endIndex);
		}

		return docToSearch.substring(newStartIndex, newEndIndex);
	}

	/**
	 * This method returns count of all keywords(using regexToUse) in docTOSearch
	 * @param docToSearch
	 * @param regexToUse
	 * @throws CYlpNullArgumentException 
	 * @return count of keywords in docToSearch
	 */
	public int getCountOfAllKeywords(String docToSearch, String regexToUse) throws CYlpNullArgumentException{

		if (regexToUse == null || docToSearch == null ) {
			throw new CYlpNullArgumentException(
					"Either regexToUse or docToSearch is null ");
		}
		
		// counter to maintain the number of keyword occurances
		int countOfAllKeywords = 0;

		//
		// Obtain the required matcher
		//
		Pattern pattern = Pattern.compile(regexToUse);
		Matcher matcher = pattern.matcher(docToSearch);

		//
		// Find every match and store start index in a array
		//
		while (matcher.find()) {
			// if we find a match then add it to currKeywordStartIndexes
			// List
			countOfAllKeywords++;

		}

		return countOfAllKeywords;

	}

	/**
	 * method constructs regular expression that matches keywords(ignoring case)
	 * @param searchString
	 * @throws CYlpNullArgumentException 
	 * @return regular expression that matches keywords(ignoring case)
	 */
	public String getRegexToMatchOneOrMoreKeywords(String searchString) throws CYlpNullArgumentException {
		
		if (searchString == null) {
			throw new CYlpNullArgumentException(
					"Search string is null ");
		}		
		

		List<String> keywordList = removeNonQualitativeTermsInSearch(searchString);
		String regexToMatchOneOrMoreKeywords = new String();

		// construct regex expression that matches one or more keywords
		for (String keyword : keywordList) {

			regexToMatchOneOrMoreKeywords = regexToMatchOneOrMoreKeywords
					+ "(?i)" + keyword + "|";
		}

		// remove the additional | from the regex
		if (regexToMatchOneOrMoreKeywords.endsWith("|")) {
			// truncate last character
			int lastIdx = regexToMatchOneOrMoreKeywords.length() - 1;
			regexToMatchOneOrMoreKeywords = regexToMatchOneOrMoreKeywords
					.substring(0, lastIdx);

		}

		return regexToMatchOneOrMoreKeywords;

	}

	/**
	 * The algorithm first splits searchString to get list of words, then it iterates
	 * over the words in docSnippetToUpdate, for each word in docSnippetToUpdate
	 * we look for matches in list If there is a match it scans the next word to
	 * see it matches any in keyword list, it does this until there is mismatch
	 * thereby finding maximum matching keyword group. This way we can pad keyword groups
	 * using only one begin and end pads 
	 * 
	 * JUSTIFICATION: Since docSnippetToUpdate is short string limited by MAX_LENGTH 
	 * characters this should be fast
	 * 
	 * There are two steps in implementation: In First step we construct a map with start indexes of 
	 * largest keyword group and value as keyword group In step two we iterate over map and 
	 * replace the occurences with padded strings
	 * 
	 * @param docSnippetToUpdate
	 * @param query
	 * @param padBefore
	 * @param padAfter
	 * @throws CYlpNullArgumentException 
	 * @return highlighted most relevant document snippet
	 */
	public String getThePaddedSnippet(String docSnippetToUpdate,
			String searchString, String padBefore, String padAfter) throws CYlpNullArgumentException {
		
		if (docSnippetToUpdate == null || searchString == null || padBefore == null || padAfter == null) {
			throw new CYlpNullArgumentException(
					"Either docSnippetToUpdate or searchString or padBefore or padAfter is null ");
		}		

		int lengthOfDocSnippet = docSnippetToUpdate.length();
		
		//highlightedDocSnippet is the string we return
		String highlightedDocSnippet = new String(docSnippetToUpdate);

		//iterator to parse words in the text 
		BreakIterator bIterator = BreakIterator.getWordInstance();
		bIterator.setText(docSnippetToUpdate);
		
		//keywordStartIndexTable holds the start indexes of largest keyword group and value as keyword group
		Map<Integer,String> keywordStartIndexTable = new HashMap<Integer,String>();		

		// regex to match one or more keywords
		String regexToMatchOneOrMoreKeywords = getRegexToMatchOneOrMoreKeywords(searchString);
		Pattern keyWordMatchpattern = Pattern.compile(regexToMatchOneOrMoreKeywords);
		Matcher keyWordmatcher;
		
		//regex to match delimiters - used to match delimiters and skip them during the iteration of words
		String delimiterRegex = getDelimiterRegex();
		Pattern spaceMatchpattern = Pattern.compile(delimiterRegex);
		Matcher spaceMatcher;

		//initialize indexes
		int currIndex = 0;		
		int highlightStartIndex = 0;
		int highlightEndIndex = 0;
				
		//initialize flags
		boolean readytoUpdate = false;
		boolean sequenceCheckOn = false;

		//STEP1: construct the map with keys as start indexes of largest keyword group and value as keyword group
		while (currIndex != BreakIterator.DONE) {
			//BreakIterator.DONE is returned by  next(), following(int) when either the first or last text boundary has been reached
			// check if it is ready to put in the map
			if (readytoUpdate) {				
				//insert in map with startIndex as key and complete keyword group as value  
				String keywordGroup = docSnippetToUpdate.substring(highlightStartIndex,highlightEndIndex);				
				keywordStartIndexTable.put(highlightStartIndex,keywordGroup);
				readytoUpdate = false;
			}
			
			//find the end of word boundary
			if(currIndex >=  docSnippetToUpdate.length()){
				break;
			}
			
			int endWordBoundary = bIterator.following(currIndex) > 0 ? bIterator.following(currIndex):docSnippetToUpdate.length()-1;			
			String currentWord = docSnippetToUpdate.substring(currIndex,endWordBoundary);			
			
			//check if current word is space
			spaceMatcher = spaceMatchpattern.matcher(currentWord);
			if(spaceMatcher.matches()){				
				//update currIndex
				currIndex = currIndex+currentWord.length() ;
				//skip
				continue;
			}

			//it is not a space let the keyword comparision begin...
			
			keyWordmatcher = keyWordMatchpattern.matcher(currentWord);		
			if (keyWordmatcher.matches()) {

				// only store start index of first match in a sequence
				if (!sequenceCheckOn) {
					highlightStartIndex = currIndex;
				}
				readytoUpdate = false;
				sequenceCheckOn = true;
			} else {
				// subsequent sequence check failed so pad
				if (sequenceCheckOn) {
					highlightEndIndex = currIndex;
					sequenceCheckOn = false;
					readytoUpdate = true;
				}

			}	
			
			int indexToSet = bIterator.next();
			currIndex = indexToSet == lengthOfDocSnippet ? lengthOfDocSnippet -1 :indexToSet ;
			
			//special case to handle pre-mature exit
			if(currIndex == -1){
				if (sequenceCheckOn) {				
					//insert in map with startIndex as key and complete keyword group as value  
					highlightEndIndex = docSnippetToUpdate.length();
					String keywordGroup = docSnippetToUpdate.substring(highlightStartIndex,highlightEndIndex);				
					keywordStartIndexTable.put(highlightStartIndex,keywordGroup);
					readytoUpdate = false;
				}
				
			}
			
		}

		//STEP2: now parse the map and replace the keyword groups
		Set<Integer> keySet = keywordStartIndexTable.keySet();
		
		// sort - this is required to establish order, for adj index as we update
		ArrayList<Integer> indexList = new ArrayList<Integer>(keySet);	
		Collections.sort(indexList);
		
		int lengthOfEachPaddingGroup = padBefore.length() + padAfter.length(); 
		int countOfPaddingGroupsAdded = 0;
		
		for (int currentIndex : indexList) {

			String currKeywordGroup = keywordStartIndexTable.get(currentIndex);
			String keywordGroupWithPadding = padBefore+currKeywordGroup+padAfter;
			
			//since we are adding padding we need to adj indexes
			int indexAdj = countOfPaddingGroupsAdded*lengthOfEachPaddingGroup;
			int currHightlightIndex = currentIndex >0 ? currentIndex+indexAdj:0;
			int endIndex = currHightlightIndex+currKeywordGroup.length();
			
			//we split the doc into 2 pars: docBeforeKeyword, docAfterKeyword. We insert our padded keyword group inbetween
			highlightedDocSnippet = highlightedDocSnippet.substring(0, currHightlightIndex)
									+keywordGroupWithPadding+ highlightedDocSnippet.substring(endIndex,highlightedDocSnippet.length());
									
			countOfPaddingGroupsAdded++;
			
		}
		
		return highlightedDocSnippet;

	}

}
