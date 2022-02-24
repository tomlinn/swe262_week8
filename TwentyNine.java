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

        // word_freq_manager = WordFrequencyManager()
        // stop_word_manager = StopWordManager()
        // send(stop_word_manager, ['init', word_freq_manager])
        // storage_manager = DataStorageManager()
        // send(storage_manager, ['init', sys.argv[1], stop_word_manager])
        // wfcontroller = WordFrequencyController()
        // send(wfcontroller, ['run', storage_manager])
        
        // # Wait for the active objects to finish
        // [t.join() for t in [word_freq_manager, stop_word_manager, storage_manager, wfcontroller]]
        //
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
        System.out.println("DataStorageManager dispatch");

        if ("init".equals(message[0])) {
            this._init(message);
        }else if ("send_word_freqs".equals(message[0])) {
            this._process_words(message);
        }else{
            TwentyNine.send(_stop_word_manager, message);
        }
    }

    public void _init (Object[] message){
        String path_to_file = (String) message[0];
        _stop_word_manager = (StopWordManager) message[1];

        try {
            _data = Files.readString(Paths.get(path_to_file)).toLowerCase();
        }catch (Exception e){
            System.out.println(e);
        }
    }

    public void _process_words(Object[] message) {

        Object recipient = message[0];
        String data_str = String.join("",this._data);
        List<String> words = Arrays.asList(data_str.split("[^a-z]+"));
        for( String word : words){
            TwentyNine.send(_stop_word_manager,new Object[]{"filter",word});
            TwentyNine.send(_stop_word_manager,new Object[]{"top25",recipient});
        }
    }

}

class StopWordManager extends ActiveWFObject {
    List<String> _stop_words;
    WordFrequencyManager _word_freqs_manager;


    public StopWordManager(WordFrequencyManager wordFrequencyManager) {
        this._stop_words = new ArrayList<>();

    }

    @Override
    public void dispatch(Object[] message) {

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
            _stop_words = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
            this._word_freqs_manager = (WordFrequencyManager) message[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _filter(Object[] message) {

        String word = (String) message[0];
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
        System.out.println("WordFrequencyManager dispatch");
        if ("word".equals(message[0])) {

            this._increment_count(message);
        }else if ("top25".equals(message[0])) {
            this._top25(message);
        }
    }

    public void _increment_count(Object[] message) {
        String word = (String) message[0];
        _word_freqs.put(word, _word_freqs.get(word)!=null ? _word_freqs.get(word) + 1: 1);
    }
    public void _top25(Object[] message) {

        Object recipient = message[0];
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
        if ("run".equals(message[0])) {

            this._run(message);
        }else if ("top25".equals(message[0])) {
            this._display(message);
        }else {
            //raise Exception("Message not understood "+message[0])
        }
    }
    public void _run(Object[] message){
        this._storage_manager = (DataStorageManager) message[0];
        TwentyNine.send(this._storage_manager, new Object[]{"send_word_freqs",this});
    }
    public void _display(Object[] message){
        Map<String, Integer> word_freqs = (Map<String, Integer>) message[0];
        word_freqs.entrySet().stream().forEach(word -> System.out.println(word.getKey() + " - " + word.getValue()));
        TwentyNine.send(this._storage_manager,new Object[]{"die",null});
        this.stopMe = true;
    }

}


