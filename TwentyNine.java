import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.*;

public class TwentyNine {

    public static void main(String[] args) throws Exception {
        // TODO
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
                message = queue.take(); // it will wait if null
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
            // TODO
            // self._init(message[1:])
        }else if ("send_word_freqs".equals(message[0])) {
            // TODO
            // self._process_words(message[1:])
        }else{
            // TODO
            // forward
            // send(self._word_freqs_manager, message)
        }
    }

    public void _init (Object[] message){
        String path_to_file = (String) message[0];
        _stop_word_manager = (StopWordManager) message[1];
        // TODO
        // with open(path_to_file) as f:
        // self._data = f.read()
        // pattern = re.compile('[\W_]+')
        // self._data = pattern.sub(' ', self._data).lower()
    }

    public void _process_words(Object[] message) {
        // TODO
        // recipient = message[0]
        // data_str = ''.join(self._data)
        // words = data_str.split()
        // for w in words:
        // send(self._stop_word_manager, ['filter', w])
        // send(self._stop_word_manager, ['top25', recipient])
    }

}

class StopWordManager extends ActiveWFObject {
    List<String> stopWords;
    WordFrequencyManager wordFrequencyManager;


    public StopWordManager(WordFrequencyManager wordFrequencyManager) {
        this.stopWords = new ArrayList<>();

    }

    @Override
    public void dispatch(Object[] message) {

        if ("init".equals(message[0])) {
            // TODO
            // self._init(message[1:])
        }else if ("filter".equals(message[0])) {
            // TODO
            // self._filter(message[1:])
        }else{
            // TODO
            // forward
            // send(self._word_freqs_manager, message)
        }

    }

    public void _init(Object[] message) {
        try {
            stopWords = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
            this.wordFrequencyManager = (WordFrequencyManager) message[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void _filter(Object[] message) {
        // TODO
        // word = message[0]
        // if word not in self._stop_words:
        // send(self._word_freqs_manager, ['word', word])
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
            // TODO
            // self._increment_count(message[1:])
        }else if ("top25".equals(message[0])) {
            // TODO
            // self._top25(message[1:])
        }
    }

    public void _increment_count(Object[] message) {
        // TODO
        // word = message[0]
        // if word in self._word_freqs:
        // self._word_freqs[word] += 1
        // else:
        // self._word_freqs[word] = 1
    }
    public void _top25(Object[] message) {
        // TODO
        // recipient = message[0]
        // freqs_sorted = sorted(self._word_freqs.items(), key=operator.itemgetter(1), reverse=True)
        // send(recipient, ['top25', freqs_sorted])

    }

}


