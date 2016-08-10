package com.yelp.search.highlight;

import java.util.List;
import java.util.Map;

import com.yelp.exception.CYlpNullArgumentException;
import com.yelp.search.keyword.CYlpProximitySearchKeyword;
import com.yelp.search.util.CYlpSearchUtil;

/**
 * This class has methods to select most relevant document snippet (within a larger document) to display
 * and pad the snippet with user specified padding characters
 * @author saireddy
 *
 */
public class CYlpDocumentHighlight {

	public  int MAX_SNIPPET_LENGTH;
	public String padBefore;
	public String padAfter;
	
	
	public CYlpDocumentHighlight()
	{
		//if user did not pass snippet length fix it to 100
		MAX_SNIPPET_LENGTH = 100;
		padBefore = "[[HIGHLIGHT]]";
		padAfter = "[[ENDHIGHLIGHT]]";
	}
	
	public CYlpDocumentHighlight(int snippetLength)
	{
		MAX_SNIPPET_LENGTH = snippetLength;
	}
	
	
	public int getSnippetLength()
	{
		return MAX_SNIPPET_LENGTH;
	}

	
/**
 * This method takes search query as input, searches the document passed as argument and
 * returns most relevant code snippet with words in search query padded with [[HIGHLIGHT]] and [[ENDHIGHLIGHT]]
 * @param doc: String that is a document to be highlighted
 * @param query: String that contains the search query
 * @return: The the most relevant snippet with the query terms highlighted.
 */
    public String getHighlightDoc(String doc, String query) throws CYlpNullArgumentException{
    	
		if (doc == null || query == null) {
			throw new CYlpNullArgumentException(
					"Operation Not allowed: Either search string or document is null ");
		}    	
    	
    	CYlpSearchUtil searchUtil = new CYlpSearchUtil();
    
    	//1 call the removeNonQualitativeTermsInSearch() which returns list of keywords
    	List<String> keywordList = searchUtil.removeNonQualitativeTermsInSearch(query);
             	
    	
    	//2 get combination of keywords; organized with length of keyword as key and list of combinations as values 
        Map<Integer,List> combinationTable =     searchUtil.getCombinations(keywordList);
              
        
        //3 get hash map with keyword as key and list with start index of first best matched keyword groups in the search string 
        Map<String,List> KeywordIndexTable =     searchUtil.getFirstMatchingKeyword(doc,combinationTable);
               
        
        //4 From the keywords in the map get the best possible snippet that has maximum keywords occurences in proximity of MAX_SNIPPET_LENGTH 
        CYlpProximitySearchKeyword bestProximityKeyword = searchUtil.proximitySearch(doc,query, KeywordIndexTable , MAX_SNIPPET_LENGTH);
        
     
        //5 Replace the keyword with [[HIGHLIGHT]]keyword[[ENDHIGHLIGHT]]
        String mostReleventSnippet = searchUtil.getThePaddedSnippet(bestProximityKeyword.getDocumentSnippet(),query, padBefore,padAfter);
   
        return mostReleventSnippet;
    }
}