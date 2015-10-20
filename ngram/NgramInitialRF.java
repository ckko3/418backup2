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

        private HashMap<String, Integer> NgramMap = new HashMap<>();
        private NgramParser parser;

        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {

            parser.addLine(value.toString());
            List<Character> elms = null;
            while ((elms = parser.next()) != null) {
                StringBuilder sb = new StringBuilder();
                StringBuilder starsb = new StringBuilder();
                for (char elm: elms) {
                    sb.append(elm);
                    sb.append(" ");
		        starsb.append(elm);
                }
                parser.shift();
                sb.deleteCharAt(sb.length()-1);
		        starsb.append(" *");
                String gram = sb.toString();
		        String stargram = sb.toString();

                int count = NgramMap.containsKey(gram) ? NgramMap.get(gram) : 0;
		        int starcnt = NgramMap.containsKey(stargram) ? NgramMap.get(stargram) : 0;
                NgramMap.put(gram, count+1);
		        NgramMap.put(stargram, starcnt+1);
                }
            }

        protected void setup(Context context)
                throws IOException, InterruptedException {
            Configuration conf = context.getConfiguration();
            int adjWordCount = conf.getInt("adjWordCount", 2);
	        double theta = conf.getDouble("theta", 0.6);
            parser = new NgramParser(adjWordCount);
        }

        protected void cleanup(Context context)
                throws IOException, InterruptedException {

            for (Map.Entry<String, Integer> entry : NgramMap.entrySet()) {
                context.write(new Text(entry.getKey()),
                              new IntWritable(entry.getValue()));
            }
        }
    }

    public static class NgramInitialRFReducer extends 
        Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
	    //if (rf >= theta )
            context.write(key, new IntWritable(sum));
        }
    }

    public int run(String[] args) throws Exception {
        Configuration conf = getConf();

        if (args.length < 4) {
            JobBuilder.printUsage(this, "<input> <output> <number of adjacent words> <theta>");
            return -1;
        }
        conf.setInt("adjWordCount", Integer.parseInt(args[2]));
	    conf.setDouble("theta", Double.parseDouble(args[3]));
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
