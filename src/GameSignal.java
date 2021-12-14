import java.io.Serializable;

public class GameSignal implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code;
	//100����
	//	101 : �α���	
	//200���� (���� ����)
	//300���� (���ӹ� ����)
	// 0���� (ä�� ����)
	//	301(ncd) : ä�� �޼��� (����) client -> server
	//	302(ncd) : ä�� �޼��� (������) client -> server 
	//	303(ncd) : ä�� �޼��� server -> client
	//	304(ncd) : �α� �޼��� server -> client
	// 10���� (���� ���� ����)
	//	311(ncd) : 1p ���� ��ġ client -> server
	//	312(ncd) : 2p ���� ��ġ client -> server
	//	313(ncx) : ���� ����
	//  314(ncx) : ���� ���
	// 20���� (���� ������ ����)
	//	321(ncx) : ���ӹ� ����
	// 30���� (ReDraw)
	//  331(ncd) : ����ӽ� redraw (������)
	//  332(ncddd) : ���ӽ� redraw (��ü)
    public String UserName;
    public String data;
    public int[][] p1Data;
    public int[][] p2Data;

    public GameSignal(String UserName, String code, String msg) {
        this.code = code;
        this.UserName = UserName;
        this.data = msg;
    }
    
    public GameSignal(String UserName, String code) { // ������ signal
    	this.code = code;
    	this.UserName = UserName;
    }
    
    public GameSignal(String UserName, String code, String msg, int[][] p1data, int[][] p2data) { // �׸�������
    	this.code = code;
        this.UserName = UserName;
        this.data = msg;
        this.p1Data = p1data;
        this.p2Data = p2data;
    }
}