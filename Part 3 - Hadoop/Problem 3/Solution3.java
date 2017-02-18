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

public class Solution3 {
  
  public static class TokenizerMapper extends Mapper<LongWritable, Text, Text, Text>{

    private Text conferenceLocation = new Text();
    private Text conferenceName = new Text();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
       
      StringTokenizer itr = new StringTokenizer(value.toString(), "\n");        //Splitting document text using newline

      while (itr.hasMoreTokens()) {
        String eachLine = itr.nextToken().toString();
        String[] partString;
        partString = eachLine.split("\t");                                      //Splitting each line using tab
        
        if(partString[2].equals("Conference Location") || partString[0].equals("Conference Acronym")) continue;
        else{
            String modifiedName = partString[0].substring(0, partString[0].length() - 5);
        
            conferenceLocation.set(partString[2]);
            conferenceName.set(modifiedName);
            context.write(new Text(conferenceName), new Text(conferenceLocation));  //Storing Conference Acronym as key and Conference location as value
        } 
      }
    }
  }

  public static class IntSumReducer extends Reducer<Text,Text,Text,Text> {

    public void reduce(Text conferenceName, Iterable<Text> conferenceLocations, Context context) throws IOException, InterruptedException {
   
      String conferenceLocation = "";
      for (Text location : conferenceLocations) {
        conferenceLocation += "\n" + location.toString();
      }
      conferenceLocation += "\n\n";
      context.write(new Text(conferenceName), new Text(conferenceLocation));    //Storing Conference Acronym as key and merged Conference location as value
    }
  }

  public static void main(String[] args) throws Exception {
    
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Solution 3");
    job.setJarByClass(Solution3.class);
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
