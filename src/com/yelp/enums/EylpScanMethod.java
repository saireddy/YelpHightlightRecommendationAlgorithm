package com.yelp.enums;

/**
 * The enumeration has the approaches for proaximity scanning, all the approaches scan string of length 
 * MAXLENGTH, each method differs in start and end indexes
 * @author bsaireddy
 *
 */
public enum EylpScanMethod {
	//
	//currentIndex is the current index of the string to scan
	//MAXLENGTH is the maximum allowable snippet length
	//Three approaches are:
	//
	FORWARDSCAN, //scan starts from (currentIndex - MAXLENGTH) to currentIndex
	MIDSCAN,//scan starts from (currentIndex - MAXLENGTH/2) to currentIndex + MAXLENGTH/2)
	BACKWARDSCAN,//scan starts from (currentIndex) to (currentIndex + MAXLENGTH) 
}
