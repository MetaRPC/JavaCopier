import pro.mrpc.copier.*;
import java.io.*;
public class LiveAdapter {
 static String q(String s){return "\""+(s==null?"":s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n").replace("\r","\\r").replace("\t","\\t"))+"\"";}
 static String platform(){String p=System.getenv("COPIER_PLATFORM");if(p==null)p="MT5";if(!p.equals("MT4")&&!p.equals("MT5"))throw new IllegalArgumentException("COPIER_PLATFORM must be MT4 or MT5");return p;}
 static Models.Account account(String[] v,int i){Models.Account a=new Models.Account();a.type=platform();a.user=Long.parseLong(v[i]);a.password=v[i+1];a.server=v[i+2];a.id=v[i+3];return a;}
 public static void main(String[] args){try{
  BufferedReader in=new BufferedReader(new InputStreamReader(System.in));String[] v=new String[13];for(int i=0;i<13;i++){v[i]=in.readLine();if(v[i]==null)throw new Exception("Expected 13 configuration lines");}
  try(CopierService c=new CopierService("https://"+v[0],v[1])){
   if(args[0].equals("start")){Models.StartRequest req=new Models.StartRequest();req.master=account(v,2);req.slave=account(v,6);req.riskType=v[10];req.riskValue=v[11];req.fixedMasterBalance=v[12];Models.StartReply r=c.start(req);System.out.println("{\"ok\":"+r.ok+",\"copier_id\":"+q(r.copierId)+",\"error\":"+q(r.error)+"}");}
   else if(args[0].equals("list")){Models.ListReply r=c.list();StringBuilder out=new StringBuilder("{\"ok\":"+r.ok+",\"error\":"+q(r.error)+",\"copiers\":[");boolean first=true;for(Models.CopierSummary s:r.copiers){if(!first)out.append(',');first=false;out.append("{\"id\":"+q(s.id)+",\"paused\":"+s.paused+"}");}System.out.println(out.append("]}"));}
   else {Models.SimpleReply r;if(args[0].equals("pause"))r=c.pause(args[1],Boolean.parseBoolean(args[2]));else if(args[0].equals("remove"))r=c.remove(args[1]);else throw new Exception("Unknown operation");System.out.println("{\"ok\":"+r.ok+",\"error\":"+q(r.error)+"}");}
  }
 }catch(Exception e){System.out.println("{\"ok\":false,\"error\":"+q(e.getMessage())+"}");System.exit(1);}}
}
