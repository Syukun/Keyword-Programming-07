package console_test;

import java.io.UnsupportedEncodingException;

public class LevenshteinDistance
{
  public int edit(String px, String py)
  {
    int len1=px.length(),len2=py.length();
    int[][] row = new int[len1+1][len2+1];
    int i,j;
    int result;

    for(i=0;i<len1+1;i++) row[i][0] = i;
    for(i=0;i<len2+1;i++) row[0][i] = i;
    for(i=1;i<=len1;++i)
    {
      for(j=1;j<=len2;++j)
      {
        row[i][j] = Math.min(Math.min(
           (Integer)(row[i-1][j-1])
           + ((px.substring(i-1,i).equals(py.substring(j-1,j)))?0:1) , // replace
                      (Integer)(row[i][j-1]) + 1),     // delete
                      (Integer)(row[i-1][j]) + 1);  // insert
      }
    }
    result=(Integer)(row[len1][len2]);

    return result;
  }
  public static void main(String[] args) throws UnsupportedEncodingException
  {
    LevenshteinDistance ld = new LevenshteinDistance() ;
    System.out.println(ld.edit("‚¢‚ ‚¤","‚ ‚¢‚¤"));
  }
}