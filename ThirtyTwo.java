import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static java.util.Map.Entry.comparingByValue;

public class ThirtyTwo {
    static Integer curLine = 0;
    static Integer maxLines = read_file("pride-and-prejudice.txt").split("\n").length;
    public static void main(String[] args) throws Exception {

        List<List<Object[]>> split = new ArrayList<>();
        split = map("split_words",read_file("pride-and-prejudice.txt"));

        // Debug: make sure data didn't lost
        // while(true){
        //     if(split.size() != maxLines / 200 + 1){
        //         break;
        //     }
        //     split = map("split_words",read_file("pride-and-prejudice.txt"));
        // }
        split = map("split_words",read_file("pride-and-prejudice.txt"));

        HashMap<String, List<Object[]>> splits_per_word = regroup(split);

        ((ConcurrentHashMap<String, Integer>)map("count_words",splits_per_word)).entrySet().stream()
                .sorted(comparingByValue(reverseOrder()))
                .limit(25)
                .forEach(word -> System.out.println(word.getKey() + " - " + word.getValue()));

    }

    public static String partition(String data_str, Integer startLine, Integer nlines) {
        nlines = maxLines - startLine < 200 ? maxLines - startLine : nlines;
        List<String> lines =  Arrays.asList(data_str.split("\n"));
        String result = String.join("\n",lines.subList(startLine,startLine + nlines));
        return result;
    }

    public static List<Object[]> split_words(String data_str) {

        List<Object[]> result = new ArrayList<>();
        List<String> words = _remove_stop_words(_scan(data_str));
        for(String w : words){
            result.add(new Object[]{w,1});
        }
        return result;
    }

    public static List<String> _scan(String str_data) {
        return Arrays.asList(str_data.toLowerCase().split("[^a-z]+"));
    }

    public static List<String> _remove_stop_words(List<String> word_list) {
        try {
            List<String> stopWords = Arrays.asList(Files.readString(Paths.get("stop_words.txt")).split(","));
            return word_list.stream().filter(word -> !stopWords.contains(word) && word.length() >= 2).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static  HashMap<String,List<Object[]>> regroup(List<List<Object[]>> pairs_list) {
        HashMap<String,List<Object[]>> mapping = new HashMap<>();
        for(List<Object[]> pairs : pairs_list){
            for(Object[] p : pairs){
                if(mapping.keySet().contains(p[0])){
                    mapping.get(p[0]).add(p);
                }else{
                    mapping.put((String)p[0],new ArrayList<>(){{add(p);}});
                }
            }
        }
        return mapping;

    }

    public static Object[] count_words(Object[] mapping){
        // input : ["project", [["project",1], ["project",1],,,,,] ]
        // output: ["project", N ]
        return new Object[]{mapping[0],reduce("add", (List) mapping[1])};
    }

    public Integer add(Integer x, Integer y){
        return x+y;
    }

    public static Integer reduce(String functionName, List data){
        Integer sum = 0;
        if(functionName == "add") {
            for (Object object : data) {
                Object[] obj = (Object[]) object;
                sum += (Integer) obj[1];
            }
        }
        return sum;
    }

    public static Object map(String functionName, HashMap<String, List<Object[]>> splits_per_word) throws InterruptedException {

        // Cannot use HashMap bcuz we are porcessing the result in theadpool, which is not safe.
        ConcurrentHashMap<String, Integer> result = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(5);
        for(String key: splits_per_word.keySet()){

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // check the size of Thread pool
                    // System.out.println("Thread # " + Thread.currentThread().getName() + " is doing this task");

                    if(functionName == "count_words") {
                        Object[] obj = count_words(new Object[]{key, splits_per_word.get(key)});
                        result.put((String) obj[0], (Integer) obj[1]);
                    }
                }
            });
            pool.execute(t);
        }
        pool.shutdown();
        while (!pool.awaitTermination(100, TimeUnit.MILLISECONDS)){
            System.out.println("Thread Pool has not fully terminated");
        }
        return (Object) result;
    }
    public static List<List<Object[]>> map(String functionName, String data) throws InterruptedException {
        // Cannot use HashMap bcuz we are porcessing the result in theadpool, which is not safe.
        ConcurrentHashMap<String, Integer> result = new ConcurrentHashMap<>();
        ExecutorService pool = Executors.newFixedThreadPool(5);

        //
        List<List<Object[]>> result3 = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < maxLines; i = i + 200) {
            int finalI = i;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    // check the size of Thread pool
                    // System.out.println("Thread # " + Thread.currentThread().getName() + " is doing this task");
                    result3.add(split_words(partition(read_file("pride-and-prejudice.txt"), finalI, 200)));
                }
            });
            pool.execute(t);
        }


        pool.shutdown();
        while (!pool.awaitTermination(500, TimeUnit.MILLISECONDS)){
            System.out.println("Thread Pool has not fully terminated");
        }
        return result3;
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
