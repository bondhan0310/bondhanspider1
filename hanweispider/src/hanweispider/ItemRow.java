package hanweispider;

/*
 * �鼮�࣬Ϊ�˴洢��list��Ա�������������
 */

/*
 * ccccccccc
 */
public class ItemRow {
	
	public String title;
	public String score;	
	public String amount;
	
	
	public ItemRow(String title, String score, String amount) {
		super();
		this.title = title;
		this.score = score;
		this.amount = amount;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	

}
