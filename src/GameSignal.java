import java.io.Serializable;

public class GameSignal implements Serializable {
	private static final long serialVersionUID = 1L;
	public String code;
	//100번대
	//	101 : 로그인	
	//200번대 (대기실 관련)
	//300번대 (게임방 관련)
	// 0번대 (채팅 관련)
	//	301(ncd) : 채팅 메세지 (선수) client -> server
	//	302(ncd) : 채팅 메세지 (관전자) client -> server 
	//	303(ncd) : 채팅 메세지 server -> client
	//	304(ncd) : 로그 메세지 server -> client
	// 10번대 (게임 진행 관련)
	//	311(ncd) : 1p 공격 위치 client -> server
	//	312(ncd) : 2p 공격 위치 client -> server
	//	313(ncx) : 게임 시작
	//  314(ncx) : 게임 기권
	// 20번대 (게임 입퇴장 관련)
	//	321(ncx) : 게임방 퇴장
	// 30번대 (ReDraw)
	//  331(ncd) : 비게임시 redraw (유저만)
	//  332(ncddd) : 게임시 redraw (전체)
    public String UserName;
    public String data;
    public int[][] p1Data;
    public int[][] p2Data;

    public GameSignal(String UserName, String code, String msg) {
        this.code = code;
        this.UserName = UserName;
        this.data = msg;
    }
    
    public GameSignal(String UserName, String code) { // 간단한 signal
    	this.code = code;
    	this.UserName = UserName;
    }
    
    public GameSignal(String UserName, String code, String msg, int[][] p1data, int[][] p2data) { // 그림데이터
    	this.code = code;
        this.UserName = UserName;
        this.data = msg;
        this.p1Data = p1data;
        this.p2Data = p2data;
    }
}