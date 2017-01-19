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
	
	private static String[] classes ;
	
	private HashMap<String,Float> classTokCounts;
	private HashMap<String,Float> classDocCounts;
	private HashMap<String,Float> prior;
	private HashMap<String,HashMap<String,Integer>> dictionary;
	
	private static int tokCount = 0;
	private static int docCount = 0;
	
	public SentimentAnalyzer(){
		System.out.println("In Constructor");
		dataFolder = "C:\\Users\\sarvadnya\\workspace\\Test\\data\\";
		
		classDocCounts = new HashMap<String,Float>();
		classTokCounts = new HashMap<String,Float>();
		dictionary = new HashMap<String, HashMap<String,Integer>>();
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
		
		classes = new String[3];
		classes[0] = "pos";
		classes[1] = "neu";
		classes[2] = "neg";
				
		dictionary.put("pos", new HashMap<String,Integer>());
		dictionary.put("neg", new HashMap<String,Integer>());
		dictionary.put("neu", new HashMap<String,Integer>());
		
		loadDefaults();
		
	}
	
	private void printDictionary(){
		for (Map.Entry<String, HashMap<String,Integer>> entry : dictionary.entrySet()) {
		    System.out.println(entry.getKey()+" : "+entry.getValue());
		}

	}
	
	public boolean setDictionary(String typeClass) throws Exception{
		System.out.println("In setDictionary()....");
		float temp;
		String[] words = getList(typeClass);
		for(String word : words){
			docCount++;
			temp = classDocCounts.get(typeClass);
			++temp;
			classDocCounts.put(typeClass, temp);
			
			word = word.trim();	
			if(!dictionary.get(typeClass).containsKey(word)){
				dictionary.get(typeClass).put(word, 1);
			}
			
			temp = classDocCounts.get(typeClass);
			classTokCounts.put(typeClass,++temp);
			tokCount++;
		}
		
		return true;
	}
	
	public String[] getList(String type) throws Exception{
		System.out.println("In getList()....");
		String[] wordList = null;
		String fileName = dataFolder + type +".json";
		
		List<String> words = new ArrayList<String>();
		JSONParser parser = new JSONParser();
		JSONArray jsonArray = (JSONArray)parser.parse(new FileReader(fileName));
		for(Object obj: jsonArray){
			String str = (String) obj;
			str.replaceAll("\\\\", "");
			words.add(str.trim());
		}
		
		wordList = new String[words.size()];
		wordList = words.toArray(wordList);
		
		System.out.println("In WordList()... Dict?"+type);
		return wordList;
	}
	
	public HashMap<String,Float> score(String sentence){
		System.out.println("In Score()....");
		for(String negPrefix : negPrefixList){
			if(sentence.contains(negPrefix)){
				sentence = negPrefix.replace(negPrefix + " ",negPrefix);
				System.out.println("Sentence: "+sentence);
			}
			System.out.println("SentenceAfterLoop: "+sentence);
		}
		
		// Tokenize the Document
		String[] tokens = getTokens(sentence);
		System.out.println("Tokens: "+ Arrays.toString(tokens));
		HashMap<String,Float> scores = new HashMap<String,Float>();
		
		float totalScore = 0;
		int count = 0;
		
		for(String typeClass : classes){
			scores.put(typeClass, 1f);
			
			for(String token : tokens){
				if(token.length() > minTokenLength && token.length() < maxTokenLength && !Arrays.asList(ignoreList).contains(token)){
					//if((count = dictionary.get(typeClass).get(token)) >= 1);
					System.out.println("Isreachable? Token : " + token);
					//printDictionary();
					
					if(dictionary.get(typeClass).containsKey(token)){
						count = dictionary.get(typeClass).get(token);
						System.out.println("Unreachable? Count : "+count);
					}
					else count = 0;
				}
				float temp = scores.get(typeClass) * (count + 1);
				scores.put(typeClass, temp);
			}
			
			scores.put(typeClass,prior.get(typeClass) * scores.get(typeClass));
			System.out.println("Score: "+scores.get(typeClass));
		}
		
		for(String typeClass : classes){
			totalScore += scores.get(typeClass);
			System.out.println("Total Score: " + totalScore);
		}
		
		for(String typeClass : classes){
			float temp = Math.round(scores.get(typeClass)/totalScore);
			scores.put(typeClass, temp);
		}
		
		System.out.println("Final Score: "+scores.toString());
		return scores;
	}
	
	public String categorize(String sentence){
		System.out.println("In Categorize()....");
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
		System.out.println("In loadDefaults()....");
		try{
			for(String typeClass : classes){
				System.out.println("loadClass: "+typeClass);
				if(!setDictionary(typeClass)){
					System.out.println("Dictionary not set for " + typeClass);
				}
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
			ex.printStackTrace();
		}	
		
		System.out.println("Size NegPrefixList: " + negPrefixList.length);
	}	
	
	private String[] getTokens(String str){
		System.out.println("In getTokens()....");
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
	
	public static void main(String[] args){
		SentimentAnalyzer sentiment = new SentimentAnalyzer();
		String[] strings = {"This is good", "This is worse", "This is fine"};

		for(String str : strings){
			System.out.println("Sentence : " + str);
			String cls = sentiment.categorize(str);
			System.out.println("Category : " + cls);
		}
	}
}
