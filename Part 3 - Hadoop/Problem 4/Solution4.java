import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Solution4 {
  
  public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable>{
    private final static IntWritable frequency = new IntWritable(1);
    private Text conferenceLocationYear = new Text();

    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
       
      StringTokenizer itr = new StringTokenizer(value.toString(), "\n");        //Splitting document text using newline

      while (itr.hasMoreTokens()) {
        String eachLine = itr.nextToken().toString();
        String[] partString;
        partString = eachLine.split("\t");                                      //Splitting each line using tab
        
        if(partString[2].equals("Conference Location") || partString[0].equals("Conference Acronym")) continue;
        else{
            String Year = partString[0].substring(partString[0].length() - 4, partString[0].length());
    
            if(!(Year.charAt(0) >= '0' && Year.charAt(0) <= '9')) Year = "20" + Year.substring(2, 4);
            
            conferenceLocationYear.set(partString[2] + "\t" + Year);            //Merging conference location and year
            
            context.write(new Text(conferenceLocationYear), frequency);         //Storing Conference location and year as key and number of Conferences as value
        } 
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text,IntWritable,Text,IntWritable> {
    private IntWritable combinedFrequency = new IntWritable();

    public void reduce(Text conferenceLocationYear, Iterable<IntWritable> frequency, Context context) throws IOException, InterruptedException {
   
      int sumFrequency = 0;
      for (IntWritable data : frequency) {
        sumFrequency += data.get();
      }
      combinedFrequency.set(sumFrequency);
      context.write(conferenceLocationYear, combinedFrequency);                 //Storing Conference location and year as key and reduced number of Conferences as value
    }
  }

  public static void main(String[] args) throws Exception {
    
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Solution 4");
    job.setJarByClass(Solution4.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}