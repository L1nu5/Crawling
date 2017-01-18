import java.util.*;
import java.text.*;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.parser.*;
import org.bson.*;

public class SentimentAnalyzer {
	private static String dataFolder;
	private static String[] ignoreList;
	private static String[] negPrefixList;
	
	private static int minTokenLength = 1;
	private static int maxTokenLength = 15;
	
	private static final String[] classes = {"pos","neg","neu"};
	
	private HashMap<String,Float> classTokCounts;
	private HashMap<String,Float> classDocCounts;
	private HashMap<String,Float> prior;
	private HashMap<String,HashMap<String,Integer>> dictionary;
	
	private static int tokCount = 0;
	private static int docCount = 0;
	
	public SentimentAnalyzer(){
		dataFolder = "/data/";
		
		classDocCounts = new HashMap<String,Float>();
		classTokCounts = new HashMap<String,Float>();
		dictionary = new HashMap<String,HashMap<String,Integer>>();
		prior = new HashMap<String,Float>();
		
		classDocCounts.put("pos", 0f);
		classDocCounts.put("neg", 0f);
		classDocCounts.put("neu", 0f);
		
		classTokCounts.put("pos", 0f);
		classTokCounts.put("neg", 0f);
		classTokCounts.put("neu", 0f);
		
		prior.put("pos", 0.33333333f);
		prior.put("neg", 0.33333333f);
		prior.put("neu", 0.33333333f);
		
		dictionary.put("pos", new HashMap<String, Integer>());
		dictionary.put("neg", new HashMap<String, Integer>());
		dictionary.put("neu", new HashMap<String, Integer>());
		
	}
	
	public boolean setDictionary(String typeClass) throws Exception{
		float temp;
		String[] words = getList(typeClass);
		for(String word : words){
			docCount++;
			temp = classDocCounts.get(typeClass);
			classDocCounts.put(typeClass,temp++);
			
			word = word.trim();	
			if(dictionary.get(typeClass).get(word) < 1){
				int count = 1;
				HashMap<String,Integer> tempMap = new HashMap<String,Integer>();
				tempMap.put(word, count);
				dictionary.put(typeClass, tempMap);
			}
			
			temp = classDocCounts.get(typeClass);
			classTokCounts.put(typeClass,temp++);
			tokCount++;
		}
		return true;
	}
	
	public String[] getList(String type) throws Exception{
		String[] wordList = {};
		String fileName = dataFolder + type +".json";
		
		List<String> words = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray)parser.parse(new FileReader(fileName));
		for(Object obj: jsonArray){
			String str = (String) obj;
			str.replaceAll("\\", "");
			words.add(str.trim());
		}
		
		wordList = new String[words.size()];
		wordList = words.toArray(wordList);
		
		return wordList;
	}
	
	public HashMap<String,Float> score(String sentence){
		
		for(String negPrefix : negPrefixList){
			if(sentence.contains(negPrefix)){
				sentence = negPrefix.replace(negPrefix + " ",negPrefix);
			}
		}
		
		// Tokenize the Document
		String[] tokens = getTokens(sentence);
		HashMap<String,Float> scores = new HashMap<String,Float>();
		
		float totalScore = 0;
		int count = 0;
		
		for(String typeClass : classes){
			scores.put(typeClass, 1f);
			
			for(String token : tokens){
				if(token.length() > minTokenLength && token.length() < maxTokenLength && !Arrays.asList(ignoreList).contains(token)){
					if((count = dictionary.get(typeClass).get(token)) >= 1);
					else count = 0;
				}
				float temp = scores.get(typeClass) * (count + 1);
				scores.put(typeClass, temp);
			}
			
			scores.put(typeClass,prior.get(typeClass) * scores.get(typeClass));
		}
		
		for(String typeClass : classes){
			totalScore += scores.get(typeClass);
		}
		
		for(String typeClass : classes){
			float temp = Math.round(scores.get(typeClass)/totalScore);
		}
		
		return scores;
	}
	
	public String categorize(String sentence){
		HashMap<String,Float> scores = score(sentence);
		Map.Entry<String, Float> maxEntry = null;
		
		for(Map.Entry<String, Float> entry : scores.entrySet()){
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		        maxEntry = entry;
		}
		
		return maxEntry.getKey();
	}
	
	// Load Dictionaries & Create Cache of them.
	private void loadDefaults(){
		try{
			for(String typeClass : classes){
				System.out.println("Dictionary not set for " + typeClass);
			}
			
			if(dictionary.isEmpty())
				System.out.println("Dictionary not set");
			
			ignoreList = getList("ign");
			
			if(ignoreList.length <= 0)
				System.out.println("Error: Ignore List not set");
			
			negPrefixList = getList("prefix");
			
			if(negPrefixList.length <= 0)
				System.out.println("Error: Prefix List not set");
		}catch(Exception ex){
			System.out.println("Exception : loadDefaults()");
		}	
	}	
	
	private String[] getTokens(String str){
		// Takes A string, tokenizes & cleans it & returns an array of tokens
		String[] split = {};
		String normStr = Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("(\\r|\\n|\\r\\n)+","\\s").toLowerCase();
		try{
			split = normStr.split("\\s+");
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		return split;
	}
}
