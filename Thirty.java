import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Thirty {
    public static void main(String[] args) throws Exception {
        List<String> stopwords = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
        BlockingQueue<String> word_space = new LinkedBlockingQueue<>();
        BlockingQueue<Map<String,Integer>> freq_space = new LinkedBlockingQueue<>();
        List<String> words = Arrays.asList(Files.readString(Paths.get("pride-and-prejudice.txt")).toLowerCase().split("[^a-z]+"));
        for(String word : words){
            word_space.put(word);
        }
    }
    // # Let's create the workers and launch them at their jobs
    // workers = []
    // for i in range(5):
    //     workers.append(threading.Thread(target = process_words))
    // [t.start() for t in workers]
    //
    // # Let's wait for the workers to finish
    // [t.join() for t in workers]
    //
    // # Let's merge the partial frequency results by consuming
    // # frequency data from the frequency space
    // word_freqs = {}
    // while not freq_space.empty():
    //     freqs = freq_space.get()
    //     for (k, v) in freqs.items():
    //         if k in word_freqs:
    //             count = sum(item[k] for item in [freqs, word_freqs])
    //         else:
    //             count = freqs[k]
    //         word_freqs[k] = count
    //
    // for (w, c) in sorted(word_freqs.items(), key=operator.itemgetter(1), reverse=True)[:25]:
    //     print(w, '-', c)

    public void process_words(){
        // word_freqs = {}
        //    while True:
        //        try:
        //            word = word_space.get(timeout=1)
        //        except queue.Empty:
        //            break
        //        if not word in stopwords:
        //            if word in word_freqs:
        //                word_freqs[word] += 1
        //            else:
        //                word_freqs[word] = 1
        //    freq_space.put(word_freqs)
    }
}
