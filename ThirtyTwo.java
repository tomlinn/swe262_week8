import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class ThirtyTwo {
    static Integer curLine = 0;
    public static void main(String[] args) throws Exception {
        // #
        // # The main function
        // #
        Integer maxLines = read_file("pride-and-prejudice.txt").split("\n").length;
        List<List<Object[]>> split = new ArrayList<>();
        do{
            split.add(split_words(partition(read_file("pride-and-prejudice.txt"), maxLines - curLine > 200 ? 200 : maxLines - curLine)));
        } while(curLine < maxLines);
        HashMap<String, List<Object[]>> splits_per_word = regroup(split);
        System.out.println("\n");
        // splits = map(split_words, partition(read_file(sys.argv[1]), 200))
        // splits_per_word = regroup(splits)
        // word_freqs = sort(map(count_words, splits_per_word.items()))
        //
        // for (w, c) in word_freqs[0:25]:
        //     print(w, '-', c)
    }

    public static String partition(String data_str, Integer nlines) {
        List<String> lines =  Arrays.asList(data_str.split("\n"));
        String result = String.join("\n",lines.subList(curLine,curLine+nlines));
        curLine += nlines;
        return result;
    }

    public static List<Object[]> split_words(String data_str) {
        //     # The actual work of the mapper
        List<Object[]> result = new ArrayList<>();
        List<String> words = _remove_stop_words(_scan(data_str));
        for(String w : words){
            result.add(new Object[]{w,1});
        }
        return result;
    }

    public static  List<String> _scan(String str_data) {
        return  Arrays.asList(str_data.toLowerCase().split("[^a-z]+"));
    }

    public static  List<String> _remove_stop_words(List<String> word_list) {
        try {
            List<String> stopWords = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
            return word_list.stream().filter(word -> !stopWords.contains(word) && word.length()>=2).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static  HashMap<String,List<Object[]>> regroup(List<List<Object[]>> pairs_list) {
        //     """
        //     Takes a list of lists of pairs of the form
        //     [[(w1, 1), (w2, 1), ..., (wn, 1)],
        //      [(w1, 1), (w2, 1), ..., (wn, 1)],
        //      ...]
        //     and returns a dictionary mapping each unique word to the
        //     corresponding list of pairs, so
        //     { w1 : [(w1, 1), (w1, 1)...],
        //       w2 : [(w2, 1), (w2, 1)...],
        //       ...}
        //     """
        HashMap<String,List<Object[]>> mapping = new HashMap<>();
        for(List<Object[]> pairs : pairs_list){
            for(Object[] p : pairs){
                if(mapping.keySet().contains(p[0])){
                    mapping.get(p[0]).add(p);
                }else{
                    List<Object[]> tmp = new ArrayList<>();
                    tmp.add(p);
                    mapping.put((String)p[0],tmp);
                }
            }
        }
        return mapping;
        //     for pairs in pairs_list:
        //         for p in pairs:
        //             if p[0] in mapping:
        //                 mapping[p[0]].append(p)
        //             else:
        //                 mapping[p[0]] = [p]
        //     return mapping
    }

    public void count_words(String mapping){
        //     return (mapping[0], reduce(add, (pair[1] for pair in mapping[1])))
    }
    public Integer add(Integer x, Integer y){
        return x+y;
    }





    public static String read_file(String path_to_file){
        try {
            return Files.readString(Paths.get(path_to_file)).toLowerCase();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }





}
