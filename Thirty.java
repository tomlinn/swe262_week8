import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Collections.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

public class Thirty {
    static BlockingQueue<String> word_space = new LinkedBlockingQueue<>();
    static List<String> stopwords;
    static BlockingQueue<Map<String,Integer>> freq_space = new LinkedBlockingQueue<>();
    static List<String> words;
    public static void main(String[] args) throws Exception {
        stopwords = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
        words = Arrays.asList(Files.readString(Paths.get("pride-and-prejudice.txt")).toLowerCase().split("[^a-z]+"));
        for(String word : words){
            word_space.put(word);
        }

        List<Thread> workers = null;
        for (int i = 0; i < 5; i++) {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    process_words();
                }
            });
            workers.add(t1);
        }
        for (int i = 0; i < 5; i++) {
            workers.get(i).join();
        }



        Map<String, Integer> word_freqs = new HashMap<>();
        while (!freq_space.isEmpty()){
            Map<String, Integer> freqs = freq_space.take();
            word_freqs.putAll(freqs);
        }
        word_freqs.entrySet().stream().sorted(comparingByValue(reverseOrder()))
                .limit(25)
                .forEach(word -> System.out.println(word.getKey() + " - " + word.getValue()));
    }


    public static void process_words(){
        Map<String ,Integer> word_freqs = new HashMap<>();
        while(true) {
            String word;
            try {
                word = word_space.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if (!stopwords.contains(word)){
                if(word_freqs.keySet().contains(word)){
                    word_freqs.put(word,word_freqs.get(word)+1);
                }
                else{
                    word_freqs.put(word,1);
                }
            }
            try {
                freq_space.put(word_freqs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
