package ngram;

import ngram.lib.JobBuilder;
import ngram.lib.NgramParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.util.*;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.conf.Configuration;

public class NgramInitialRF extends Configured implements Tool {

    public static class NgramInitialRfMapper extends 
        Mapper<LongWritable, Text, Text, IntWritable> {

       private HashMap<Character, Map<String, Integer>> NgramMap = new HashMap<>();
        private HashMap<String, Integer> Stripe = new HashMap<>();
        private NgramParser parser;

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            parser.addLine(value.toString());
            List<Character> elms = null;
            while ((elms = parser.next()) != null) {
                StringBuilder sb = new StringBuilder();
                char term = '\0';
                for (char elm: elms) {
                    if (term == '\0') term = elm;
                    else {
                        sb.append(elm);
                        sb.append(" ");
                    }
                }
                parser.shift();
                sb.deleteCharAt(sb.length()-1);
                String gram = sb.toString();
                int count = Stripe.containsKey(gram) ? Stripe.get(gram)     : 0;
                Stripe.put(gram, count+1);
                NgramMap.put(term, Stripe);
                }
            }

        protected void setup(Context context)
                throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            int adjWordCount = conf.getInt("adjWordCount", 2);
            parser = new NgramParser(adjWordCount);
        }

        protected void cleanup(Context context)
                throws IOException, InterruptedException {

            for (Map.Entry<Character, Map<String, Integer>> entry : NgramMap.entrySet()) {
                context.write(new Text(entry.getKey()),
                              new MapWritable(entry.getValue()));
            }
        }
    }

    public static class NgramInitialRFReducer extends 
        Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            Configuration conf = context.getConfiguration();
            double theta = Double.parseDouble(conf.get("theta"));
            //for (IntWritable val : values)  {
             //   sum += val.get();
            //}
	    //if (rf >= theta )
            //context.write(key, new IntWritable(sum));
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();

        if (args.length < 4) {
            JobBuilder.printUsage(this, "<input> <output> <number of adjacent words> <theta>");
            return -1;
        }
        conf.setInt("adjWordCount", Integer.parseInt(args[2]));
	    conf.set("theta", args[3]);
        Job job = JobBuilder.parseInputAndOutput(this, conf, args);

        job.setMapperClass(NgramInitialRFMapper.class);
        job.setReducerClass(NgramInitialRFReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        job.waitForCompletion(true);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int exitCode = ToolRunner.run(new NgramInitialRF(), args);
        System.exit(exitCode);
    }
}
