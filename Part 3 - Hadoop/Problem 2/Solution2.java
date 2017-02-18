import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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

public class Solution2 {
  
  public static class TokenizerMapper extends Mapper<LongWritable, Text, Text, Text>{

    private final static IntWritable one = new IntWritable(1);
    private Text conferenceLocation = new Text();
    private Text conferenceName = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
       
      StringTokenizer itr = new StringTokenizer(value.toString(), "\n");        //Splitting document text using newline

      while (itr.hasMoreTokens()) {
        String hello = itr.nextToken().toString();
        String[] partString;
        partString = hello.split("\t");                                         //Splitting each line using tab
        
        if(partString[2].equals("Conference Location") || partString[0].equals("Conference Acronym")) continue;
        else{
        conferenceLocation.set(partString[2]);
        conferenceName.set(partString[1]);
        context.write(new Text(conferenceLocation), new Text(conferenceName));  //Storing Conference location as key and Conference name as value
        }
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text,Text,Text,Text> {
    private IntWritable result = new IntWritable();

    public void reduce(Text conferenceLocation, Iterable<Text> conferenceNames, Context context) throws IOException, InterruptedException {
   
      String conferenceName = "";
      for (Text name : conferenceNames) {
        conferenceName += "\n" + name.toString();
      }
      conferenceName += "\n\n";
      context.write(new Text(conferenceLocation), new Text(conferenceName));    //Storing Conference location as key and reduced Conference name as value
    }
  }

  public static void main(String[] args) throws Exception {
    
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Solution 2");
    job.setJarByClass(Solution2.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
