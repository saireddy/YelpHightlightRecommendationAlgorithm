package com.yelp.search.keyword;

import com.yelp.enums.EylpScanMethod;

/**
 * This class stores information about results of proximity search. Details include keyword which is selected 
 * for proximity search, scan method, max occurence, start index where max occurence occur etc...
 * @author bsaireddy
 *
 */
public class CYlpProximitySearchKeyword {

	//data members - define properties of class
	
	//search keyword around which proximity search is done
	private String proximitySearchkeyword;
	
	//name of scan method
	private EylpScanMethod scanMethodForMaxOccurances;
	
	//total number of all keyword occurences around this keyword by doing scan
	private int numberOfAllKeywordsOccurences;
	
	//startindex corresponding to max keyword occurence
	private int startIndexWithMaxKeywordOccurences;
	
	//document snippet where max keyword occurence is found - this is most relevant snippet
	private String documentSnippet;
	
	
	//METHODS: To access the data members
	
	public void setProximitySearchkeyword(String proxSearchkeyword){
		proximitySearchkeyword = proxSearchkeyword;
	}
	
	public String getProximitySearchkeyword(){
		return proximitySearchkeyword ;
	}	
	
	public void setScanMethodForMaxOccurances(EylpScanMethod scanMethod){
		scanMethodForMaxOccurances = scanMethod;
	}
	
	public EylpScanMethod getScanMethodForMaxOccurances(){
		return scanMethodForMaxOccurances ;
	}	
	
	public void setNumberOfAllKeywordsOccurences(int countOfKeywords){
		numberOfAllKeywordsOccurences = countOfKeywords;
	}
	
	public int getNumberOfAllKeywordsOccurences(){
		return numberOfAllKeywordsOccurences ;
	}	
	
	public void setStartIndexWithMaxKeywordOccurences(int startIndex){
		startIndexWithMaxKeywordOccurences = startIndex;
	}
	
	public int getStartIndexWithMaxKeywordOccurences(){
		return startIndexWithMaxKeywordOccurences ;
	}	
	
	public void setDocumentSnippet(String docSnippet){
		documentSnippet = docSnippet;
	}
	
	public String getDocumentSnippet(){
		return documentSnippet ;
	}		
	

	/**
	 * This method updates the atributes of current obj based on what is passed as arguments
	 * KNOWN ISSUE: if currCountOfAllKeywords = getNumberOfAllKeywordsOccurences it does not update
	 * => even if there are multiple proximity searches which have count equal only first proximity search is stored
	 * In future we can modifiy this to store them all and select one of them using some advanced algorithm
	 * @param currCountOfAllKeywords
	 * @param currentkeyword
	 * @param startIndex
	 * @param docSnippet
	 * @param scanMethod
	 */
	public void updateProximityKeywordIfRequired(int currCountOfAllKeywords,String currentkeyword,int startIndex,String docSnippet,EylpScanMethod scanMethod){
		
		//Check if the current count is greater then the best possible match count so far
		if(currCountOfAllKeywords>getNumberOfAllKeywordsOccurences()){
			//set the CYlpProximitySearchKeyword with new attrs
			setProximitySearchkeyword(currentkeyword);
			setScanMethodForMaxOccurances(scanMethod);
			setNumberOfAllKeywordsOccurences(currCountOfAllKeywords);
			setStartIndexWithMaxKeywordOccurences(startIndex);
			setDocumentSnippet(docSnippet);				
		}
		
		//do nothing for other cases
		
		
		
	}
	
}
