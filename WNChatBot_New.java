import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class WNChatBot_New {

    // They key in wordLookUp is a word, the value is the words's id
    private static HashMap<String, ArrayList<String>> wordLookUpMap = new HashMap<String, ArrayList<String>>();

    // The key in idLookUp is an id, the value is an ArrayList of words associated
    // with the id
    private static HashMap<String, ArrayList<String>> idLookUpMap = new HashMap<String, ArrayList<String>>();

    // The key in definMap map is an id, the value is a definition.
    private static HashMap<String, String> definMap = new HashMap<String, String>();

    // the key is the word, the value is its hyponym
    private static HashMap<String, String> hypMap = new HashMap<String, String>();

    // IMPORTANT: change this path to point to WordNet_assign on your computer
    private static String inputDir = "C:/Users/parry/Desktop/BFS Wordnet/";

    // read in all synset information from file, store in the hashmaps
    // mentioned above
    public void readSynsets() {

        String[] tempStr;
        String id;
        String defin;
        String[] words;

        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(inputDir + "synsets.txt"));
            String line = reader.readLine();

            // for each line in synsets.txt
            while (line != null) {
                // split line
                tempStr = line.split(",");
                // store id of synset
                id = tempStr[0];
                // store definition of synset
                defin = tempStr[2];
                definMap.put(id, defin);

                words = tempStr[1].split(" ");

                for (int j = 0; j < words.length; j++) {
                    String[] words2 = words[j].split("_");
                    String whole_word = "";
                    for (int i = 0; i < words2.length; i++) {
                        whole_word = whole_word + ' ' + words2[i];
                    }
                    whole_word = whole_word.trim();
                    ArrayList<String> arr = wordLookUpMap.get(whole_word);
                    if (arr == null) {
                        arr = new ArrayList<String>();
                    }
                    arr.add(id);

                    wordLookUpMap.put(whole_word, arr);

                    arr = idLookUpMap.get(id);
                    if (arr == null) {
                        arr = new ArrayList<String>();
                    }
                    arr.add(whole_word);
                    idLookUpMap.put(id, arr);
                } // end of breaking up synonyms

                line = reader.readLine();
            } // read next line
            reader.close();
            // System.out.println("Read in all synsets: there are " + wordLookUpMap.size() +
            // " unique words stored.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // read in hypernyms relationship
            reader = new BufferedReader(new FileReader(inputDir + "hypernyms.txt"));
            String line = reader.readLine();

            while (line != null) {
                String[] list = line.split(",");
                if (list.length >= 2) {
                    hypMap.put(list[0], list[1]);
                }
                line = reader.readLine();
            } // read next line
            reader.close();
            // System.out.println("Read in all synsets: there are " + wordLookUpMap.size() +
            // " unique words stored.");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // Print out the definition of a word
    public void giveDefinition(String word) {
        // get list of ids associated with the word from wordLookUpMap
        ArrayList<String> arrList = new ArrayList<String>();
        if (wordLookUpMap.containsKey(word) == true) {
            arrList.addAll(wordLookUpMap.get(word));
            System.out.println("Found " + arrList.size() + " definitions for the word " + word);
            // for each id use definMap to get the definition
            for (int i = 0; i < arrList.size(); i++) {
                System.out.println("Definition " + arrList.get(i) + ": " + definMap.get(arrList.get(i)));
            }
        } else {
            System.out.println("Could not find the requested word.");
            return;
        }
    }

    // from a list of candidate words, see if one of the words
    // is equal to your "goal" hyponym
    public static boolean isGoal(ArrayList<String> cand, String goal) {
        for (int i = 0; i < cand.size(); i++) {
            if (cand.get(i).equals(goal)) {
                return true;
            }
        }
        return false;
    }

    // This method will use search to discover if there is a
    // hyponym relationship between two words (word1 and
    // word 2 as the hypernym
    public boolean findRel(String word1, String word2) {
        if (wordLookUpMap.get(word1) == null || wordLookUpMap.get(word2) == null) {
            System.out.println("Could not find a requested word.");
            return false;
        } else {
            // get initial list of ids for word1
            ArrayList<String> wordIDS = wordLookUpMap.get(word1);
            // will only look to a depth of 5
            int stop_num = wordIDS.size() * 5;

            // Create a queue, for each id in wordIDS see
            // if the word associated with it is the goal
            // if it is return true otherwise add the
            // word's hyponym (that is the id of the hyponym)
            // to the queue

            PriorityQueue<String> pq = new PriorityQueue<String>();

            for (int i = 0; i < wordLookUpMap.get(word1).size(); i++) {
                for (int j = 0; j < wordLookUpMap.get(word2).size(); j++) {
                    if (isGoal(wordIDS, wordLookUpMap.get(word2).get(j)) == true) {
                        return true;
                    }
                }
                pq.add(hypMap.get(wordLookUpMap.get(word1).get(i)));
            }

            // while either the q is not empty or
            // you get to stop_num number of iterations
            // remove an id from the queue and see
            // if the word associated with it is the goal.
            // if it is the goal return true, otherwise
            // add the id of the hyponym of the current
            // word to the queue
            int n = 0;
            while (n < stop_num || pq.isEmpty()) {
                String temp = pq.poll();
                if (isGoal(wordLookUpMap.get(word2), temp)) {
                    return true;
                } else {
                    if (hypMap.get(temp) != null) {
                        pq.add(hypMap.get(temp));
                    }
                }
                n++;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        WNChatBot_New cb = new WNChatBot_New();
        cb.readSynsets();
        Scanner scan = new Scanner(System.in);
        System.out.println("Ask a question (type exit to end)");
        String line = scan.nextLine();
        while (!line.equalsIgnoreCase("exit")) {
            if (line.contains("what is ")) {
                String term = "";
                ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
                words.remove(0);
                words.remove(0);
                for (int i = 0; i < words.size(); i++) {
                    term = term.concat(words.get(i) + " ");
                }
                term = term.strip();
                cb.giveDefinition(term);
            } else if (line.contains("is ") && (line.contains(" a "))) {
                String term1 = "";
                String term2 = "";
                ArrayList<String> words = new ArrayList<String>(Arrays.asList(line.split(" ")));
                words.remove(0);
                for (int i = 0; i < words.size(); i++) {
                    if (words.indexOf(words.get(i)) < words.indexOf("a")) {
                        term1 = term1.concat(words.get(i) + " ");
                    } else if (words.indexOf(words.get(i)) > words.indexOf("a")) {
                        term2 = term2.concat(words.get(i) + " ");
                    }
                }
                term1 = term1.strip();
                term2 = term2.strip();
                boolean result = cb.findRel(term1, term2);
                if (result) {
                    System.out.println("Yes, " + term1 + " is a " + term2);
                } else {
                    System.out.println("No, " + term1 + " is not a " + term2);
                }
            }
            System.out.println("Ask a question (type exit to end)");
            line = scan.nextLine();
        }
        scan.close();
    }

}