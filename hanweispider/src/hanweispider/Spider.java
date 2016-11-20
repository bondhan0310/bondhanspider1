package hanweispider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import hanweispider.ItemRow;

public class Spider {

    private static String outfile1= "d:"+File.separator+File.separator+"互联网编程算法raw.csv";//输出原始爬到的数据
    private static String outfile2 = "d:"+File.separator+File.separator+"互联网编程算法final.csv";//输出去重和排序后的数据
    private static boolean bfile = true;                 
    private static ArrayList<String> datalist = new ArrayList<String>();   
    private static ArrayList<ItemRow> item = new ArrayList<ItemRow>();
    private static String headtitle = "书籍名称等信息,评分,评价人数";               
    private static int countrs = 0;                         

    /*
     * 
     */
    public static void getDouBanList(Document doc) throws Exception {

        Element divNode = doc.getElementById("wrapper"); //根据阅读html源码，逐层次获取各个对象         
        Elements liTag = divNode.select("li[class]");           
        String title,score,amount; //我们要抓取的信息，书名等信息，评分，评论人数           
        
        //逐个书籍提取相关信息
        for (Element liNode : liTag) {              
            Element info = liNode.getElementsByClass("info").first();               
            title = info.getElementsByTag("a").text();            
            datalist.clear();                           
            datalist.add(title);               
            Element foot = liNode.getElementsByClass("star_clearfix").first();  
            Element sc = foot.getElementsByClass("rating_nums").first(); 
            if(sc == null){
            	
            	score = "0";
            }
            else{
            	score = sc.text();
            }
            datalist.add(score);
            Element span = foot.select("span").last();              
            if(span == null){
            	
            	continue;
            }
            else{
            	amount = span.text();                       
            }
            String[] a = amount.split("人");
            String b = a[0].substring(1);
            if(b.equals("少于10") || b.equals("目前无")){continue;}
            int c = Integer.parseInt(b);
            //过滤掉评论数不足2000的书籍
            if(c < 2000 ){
            	
            	continue;
            }
            datalist.add(b);
            ItemRow ir = new ItemRow(title, score, amount);  //将每本书的内容以对象形式存入列表
            item.add(ir);
            outputRs(outfile1);//输出原始数据及打印到控制台观察
        }
    }       
 
    /*
     * 
     */
	private static void outputsfinal(String file,ArrayList<ItemRow> item) throws Exception {

        String strout = "";
        
        for(int i=0;i<item.size();i++){
        	
        	datalist.set(0, item.get(i).title);
        	datalist.set(1, item.get(i).score);
        	datalist.set(2, item.get(i).amount);
        	
        	for (int j = 0; j < datalist.size(); j++) {
                strout = strout + datalist.get(j) + ",";               
            }
            if (bfile) {
                FileWriter fw = new FileWriter(file, true);          
                PrintWriter out = new PrintWriter(fw);                  
                if (countrs == 0)
                    out.println(headtitle);                        
                out.println(strout);                        
                out.close();                                
                fw.close();                             
            }
            countrs = countrs + 1;
            System.out.println(countrs + "  " + strout);  
        	
        }
                
    }
    private static void outputRs(String file) throws Exception {

        String strout = "";
        for (int i = 0; i < datalist.size(); i++) {
            strout = strout + datalist.get(i) + ",";               
        }
        if (bfile) {
            FileWriter fw = new FileWriter(file, true);          
            PrintWriter out = new PrintWriter(fw);                  
            if (countrs == 0)
                out.println(headtitle);                        
            out.println(strout);                        
            out.close();                                
            fw.close();                             
        }
        countrs = countrs + 1;
        System.out.println(countrs + "  " + strout);          
    }
    public static class Spidermarch implements Runnable {
    	
		String surl;
		int n;
		String flag;
		String rawurl;
		
	public Spidermarch(String surl,int n,String flag,String rawurl){
		
		this.surl = surl;
		this.n = n;
		this.flag = flag;
		this.rawurl= rawurl;
	}
	public synchronized static void skipPage(String surl, int n, String flag, String rawurl) throws Exception{
		//爬虫伪装成浏览器，尽管如此，依然被墙得郁闷，代理服务器也不咋好用
        Document doc1 = Jsoup.connect(surl).userAgent("Mozilla/5.0 (Windows NT 6.3; WOW64) "
                + "AppleWebKit/537.36 (KHTML, like Gecko) "
                + "Chrome/42.0.2311.152 Safari/537.36").get(); 
        String html = doc1.toString();
        html = html.replace("star clearfix","star_clearfix");       
        Document doc = Jsoup.parse(html);
        Element footDiv = doc.getElementsByClass("paginator").first();         
        Element footSpan = footDiv.getElementsByClass("next").first();   
        Element footA = footSpan.select("a[href]").first();         
        String href = footA.attr("href").substring(n);                       
        String http = rawurl+href;      
        Element thispage = doc.getElementsByClass("thispage").first();      
        int end = Integer.parseInt(thispage.text());        
        if(end==1){
            Spider.getDouBanList(doc);
            System.out.println(flag+"=========================="+1+"===================");
        }else{
        Spider.getDouBanList(doc);                   
        }
        System.out.println(flag+"=========================="+(end+1)+"==================="); 
        //用浏览器check发现这三类书籍最多只有50页，51以后的页面点击无结果，想必后边的大概也不会拍到前100吧~~~评论数也不会有2000
        if(end<50){
            skipPage(http,n,flag,rawurl); 
        }else{
            System.out.println(flag+"页面已爬完"); 
            return;
        }
}
	@SuppressWarnings("static-access")
	@Override
	public void run() {
		String surl = this.surl;
		int n = this.n;
		String flag = this.flag;
		String rawurl = this.rawurl;
		try {
			this.skipPage(surl, n, flag, rawurl);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
    public static void main(String[] args) throws Exception{

        String strURL1 = "https://book.douban.com/tag/%E4%BA%92%E8%81%94%E7%BD%91";//互联网书籍标签链接
        String strURL2 = "https://book.douban.com/tag/%E7%BC%96%E7%A8%8B";//编程书籍标签链接
        String strURL3 = "https://book.douban.com/tag/%E7%AE%97%E6%B3%95";//算法书籍标签链接
        
        String rawurl1 = "https://book.douban.com/tag/%E4%BA%92%E8%81%94%E7%BD%91";
        String rawurl2 = "https://book.douban.com/tag/%E7%BC%96%E7%A8%8B";
        String rawurl3 = "https://book.douban.com/tag/%E7%AE%97%E6%B3%95";
        
        int n1 = 8;//互联网书籍next page href的起始位
        int n2 = 7;//编程书籍next page href的起始位
        int n3 = 7;//算法书籍next page href的起始位
        
        String flag1 = "互联网";//控制台打印用，查看线程有无被锁成功
        String flag2 = "编程";//控制台打印用，查看线程有无被锁成功
        String flag3 = "算法";//控制台打印用，查看线程有无被锁成功
       
        Spidermarch[] spider = new Spidermarch[3];
        String[] URL = {strURL1,strURL2,strURL3};
        String[] flag = {flag1,flag2,flag3};
        int[] n = {n1,n2,n3};
        String[] rawURL = {rawurl1,rawurl2,rawurl3};
        
        for(int i=0;i<3;i++){
        	
        	spider[i] = new Spidermarch(URL[i],n[i],flag[i],rawURL[i]);
        	new Thread(spider[i]).start();
        }
        
        /*
         * 冒泡法去掉重复的数据
         */
        for(int i=0;i<item.size()-1;i++){
        	
        	for(int j=0;j<item.size()-1-i;j++){
        		
        		if(item.get(j).title.equals(item.get(j+1).title)){
        			
        			item.remove(j+1);
        		}
        	}
        }
        /*
         * 按评分排序,评分相同按评论数排序
         */
        Comparator<ItemRow> comparator = new Comparator<ItemRow>(){

			@Override
			public int compare(ItemRow o1, ItemRow o2) {
				     
				       //按评分排序
				       if(!o1.score.equals(o2.score)){
				        return o2.score.compareTo(o1.score);
				       }
				       else{
				        //评分相同则按评论数排序
				        return o2.amount.compareTo(o1.amount);
				       }
			}
        };  
        Collections.sort(item, comparator);
        outputsfinal(outfile2,item);
          
    }
}
