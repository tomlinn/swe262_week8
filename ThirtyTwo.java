import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ThirtyTwo {
    static Integer curLine = 0;
    public static void main(String[] args) throws Exception {
        // #
        // # The main function
        // #
        // splits = map(split_words, partition(read_file(sys.argv[1]), 200))
        // splits_per_word = regroup(splits)
        // word_freqs = sort(map(count_words, splits_per_word.items()))
        //
        // for (w, c) in word_freqs[0:25]:
        //     print(w, '-', c)
    }

    public String partition(String data_str, Integer nlines) {
        List<String> lines =  Arrays.asList(data_str.split("\n"));
        curLine += nlines;
        return String.join("\n",lines.subList(curLine,curLine+nlines));
    }

    public Map<String,Integer> split_words(String data_str) {
        //     # The actual work of the mapper
        Map<String,Integer> result = new HashMap<>();
        List<String> words = _remove_stop_words(_scan(data_str));
        for(String w : words){
            result.put(w, 1);
        }
        return result;
    }

    public List<String> _scan(String str_data) {
        return  Arrays.asList(str_data.toLowerCase().split("[^a-z]+"));
    }

    public List<String> _remove_stop_words(List<String> word_list) {
        try {
            return Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Map<String,Integer> regroup(List<Map<String,Integer>> pairs_list) {

        Map<String,Integer> mapping = new HashMap<>();
        for(Map<String,Integer> pairs : pairs_list){
            for(String key: pairs.keySet()){
                if(!mapping.keySet().contains(key)){
                    mapping.put(key,pairs.get(key));
                }else{
                    mapping.put(key,mapping.get(key) + pairs.get(key));
                }
            }
        }
        return mapping;

    }

    public void count_words(String mapping){
        //     return (mapping[0], reduce(add, (pair[1] for pair in mapping[1])))
    }
    public Integer add(Integer x, Integer y){
        return x+y;
    }





    public String read_file(String path_to_file){
        //     with open(path_to_file) as f:
        //         data = f.read()
        //     return data
    }

    public Map<String, Integer> sort(Map<String, Integer> word_freq){
        //     return sorted(word_freq, key=operator.itemgetter(1), reverse=True)
    }



}
