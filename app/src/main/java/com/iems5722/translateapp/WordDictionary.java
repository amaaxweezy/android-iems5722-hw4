package com.iems5722.translateapp;

import java.util.HashMap;
import java.util.Map;

public class WordDictionary {

	public Map<String, String> wordDict = new HashMap<String, String>();
	
	public WordDictionary() {
		wordDict.put("zero", "0");
		wordDict.put("one", "一");
		wordDict.put("two", "二");
		wordDict.put("three", "三");
		wordDict.put("four", "四");
		wordDict.put("five", "五");
		wordDict.put("six", "六");
		wordDict.put("seven", "七");
		wordDict.put("eight", "八");
		wordDict.put("nine", "九");
		wordDict.put("ten", "十");
	}
	
	public Map<String, String> getDictionary() {
		return wordDict;
	}
}
