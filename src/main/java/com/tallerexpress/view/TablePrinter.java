package com.tallerexpress.view;
import java.util.*;
public final class TablePrinter {
 private TablePrinter(){}
 public static String print(String[] headers,List<String[]> rows){int[] w=new int[headers.length]; for(int i=0;i<headers.length;i++)w[i]=headers[i].length(); for(String[] r:rows)for(int i=0;i<headers.length;i++)w[i]=Math.max(w[i],i<r.length?r[i].length():0); StringBuilder s=new StringBuilder(); String sep="+"; for(int x:w)sep+="-".repeat(x+2)+"+"; s.append(sep).append('\n'); s.append(row(headers,w)).append('\n').append(sep).append('\n'); for(String[] r:rows)s.append(row(r,w)).append('\n'); return s.append(sep).toString(); }
 private static String row(String[] r,int[] w){StringBuilder s=new StringBuilder("|"); for(int i=0;i<w.length;i++){String v=i<r.length&&r[i]!=null?r[i]:""; s.append(' ').append(String.format("%-"+w[i]+"s",v)).append(" |");} return s.toString();}
}
