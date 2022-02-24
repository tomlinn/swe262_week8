import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

public class TwentyNine {

    public static void main(String[] args) throws Exception {

        WordFrequencyManager word_freq_manager = new WordFrequencyManager();
        StopWordManager stop_word_manager = new StopWordManager();
        send(stop_word_manager, new Object[]{"init", word_freq_manager});
        DataStorageManager storage_manager = new DataStorageManager();
        send(storage_manager, new Object[]{"init", "pride-and-prejudice.txt", stop_word_manager});
        WordFrequencyController wfcontroller = new WordFrequencyController();
        send(wfcontroller, new Object[]{"run", storage_manager});

        word_freq_manager.join();
        stop_word_manager.join();
        storage_manager.join();
        wfcontroller.join();

    }
    public static void send(ActiveWFObject receiver, Object[] message) {
        receiver.queue.offer(message);
    }
}



class ActiveWFObject extends Thread {
    BlockingQueue<Object[]> queue;
    boolean stopMe;

    public ActiveWFObject() {
        this.queue = new LinkedBlockingQueue<>();
        this.stopMe = false;
        start();
    }

    @Override
    public void run() {
        while (!this.stopMe) {
            Object[] message;

            try {
                message = queue.take(); // it will wait if it's null
                dispatch(message);
                if (message[0].equals("die")) {
                    this.stopMe = true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void dispatch(Object[] message) {
        return;
    }

}

class DataStorageManager extends ActiveWFObject {
    String _data;
    StopWordManager _stop_word_manager;

    @Override
    public void dispatch(Object[] message) {
        //System.out.println("DataStorageManager dispatch");

        if ("init".equals(message[0])) {
            this._init(message);
        }else if ("send_word_freqs".equals(message[0])) {
            this._process_words(message);
        }else{
            TwentyNine.send(_stop_word_manager, message);
        }
    }

    public void _init (Object[] message){
        String path_to_file = (String) message[1];
        _stop_word_manager = (StopWordManager) message[2];
        System.out.println("DataStorageManager _init");
        try {
            _data = Files.readString(Paths.get(path_to_file)).toLowerCase();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void _process_words(Object[] message) {
        System.out.println("DataStorageManager _process_words");
        Object recipient = message[1];
        String data_str = String.join("",this._data);
        List<String> words = Arrays.asList(data_str.split("[^a-z]+"));
        for( String word : words){
            TwentyNine.send(_stop_word_manager,new Object[]{"filter",word});
        }
        TwentyNine.send(_stop_word_manager,new Object[]{"top25",recipient});
    }

}

class StopWordManager extends ActiveWFObject {
    List<String> _stop_words;
    WordFrequencyManager _word_freqs_manager;


    public StopWordManager() {
        this._stop_words = new ArrayList<>();

    }

    @Override
    public void dispatch(Object[] message) {
        //System.out.println("StopWordManager dispatch");
        if ("init".equals(message[0])) {
            this._init(message);
        }else if ("filter".equals(message[0])) {
            this._filter(message);
        }else{
            TwentyNine.send(this._word_freqs_manager, message);
        }

    }

    public void _init(Object[] message) {
        try {
            System.out.println("StopWordManager _init");
            _stop_words = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
            this._word_freqs_manager = (WordFrequencyManager) message[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _filter(Object[] message) {
        System.out.println("StopWordManager _filter");
        String word = (String) message[1];
        if (this._stop_words.contains(word)){
            TwentyNine.send(this._word_freqs_manager, new Object[]{"word", word});
        }

    }

}


class WordFrequencyManager extends ActiveWFObject {
    Map<String, Integer> _word_freqs;

    public WordFrequencyManager() {
        this._word_freqs = new HashMap<>();
    }

    @Override
    public void dispatch(Object[] message) {
        //System.out.println("WordFrequencyManager dispatch");
        if ("word".equals(message[0])) {

            this._increment_count(message);
        }else if ("top25".equals(message[0])) {
            this._top25(message);
        }
    }

    public void _increment_count(Object[] message) {
        System.out.println("WordFrequencyManager _increment_count");
        String word = (String) message[1];
        _word_freqs.put(word, _word_freqs.get(word)!=null ? _word_freqs.get(word) + 1: 1);
    }
    public void _top25(Object[] message) {
        System.out.println("WordFrequencyManager _top25");
        Object recipient = message[1];
        Map<String, Integer> freqs_sorted = _word_freqs.entrySet().stream()
                .sorted(comparingByValue(reverseOrder()))
                .limit(25)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        TwentyNine.send((ActiveWFObject) recipient, new Object[]{"top25", freqs_sorted});

    }

}

class WordFrequencyController extends ActiveWFObject {

    DataStorageManager _storage_manager = new DataStorageManager();
    @Override
    public void dispatch(Object[] message) {
        //System.out.println("WordFrequencyController dispatch");
        if ("run".equals(message[0])) {

            this._run(message);
        }else if ("top25".equals(message[0])) {
            this._display(message);
        }else {
            //raise Exception("Message not understood "+message[0])
        }
    }
    public void _run(Object[] message){
        System.out.println("WordFrequencyController _run");
        this._storage_manager = (DataStorageManager) message[1];
        TwentyNine.send(this._storage_manager, new Object[]{"send_word_freqs",this});
    }
    public void _display(Object[] message){
        System.out.println("WordFrequencyController _display");
        Map<String, Integer> word_freqs = (Map<String, Integer>) message[1];
        word_freqs.entrySet().stream().forEach(word -> System.out.println(word.getKey() + " - " + word.getValue()));
        TwentyNine.send(this._storage_manager,new Object[]{"die",null});
        this.stopMe = true;
    }

}


