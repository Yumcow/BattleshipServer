import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class BattleshipBoardServer extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector RoomVec = new Vector(); // 방들을 저장할 벡터
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	
	public BattleshipBoardServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BattleshipBoardServer frame = new BattleshipBoardServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					new_user.start(); // 만든 객체의 스레드 실행
				} catch (IOException e) {
					AppendText("accept() error");
					//System.exit(0);
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}


	class RoomService extends Thread {
		public String roomCode;
		public Vector UserVec = new Vector(); // 연결된 사용자들을 저장할 벡터
		public UserService p1;
		public UserService p2;
		private GameBoard p1Board;
		private GameBoard p2Board;
		private int turn; // 0 = 대기중, 1 = 1p, 2 = 2p | 1, 2일때는 게임이 진행중임
		private boolean gameStarted; // 게임이 시작 되었었나? 로그파일 입력할때 초기화할지 이어쓸지 구분하기 위함
		private FileOutputStream fos;
		
		public RoomService(String code) {
			this.roomCode = code;
			this.turn = 0;
			this.gameStarted = false;
		}
		
		public void GameStart() {
			p1Board = new GameBoard();
			p2Board = new GameBoard();
			p1Board.collocateShips();
			p2Board.collocateShips();
			redrawBoard();
			turn = 1;
			
			WriteLog("게임이 시작 되었습니다.");
			WriteLog(p1.UserName + "(p1)의 턴입니다.");
		}
		
		public void GameOver(String player, boolean normal) { // player : 승리자 / normal : 정상종료 여부(게임으로 끝났는지, 기권, 접속종료로 끝났는지)
			if(normal) {
				if(player.equals("1")) WriteLog(p1.UserName + "(p1)이 모든 적함을 격침하여 승리했습니다!");
				else WriteLog(p2.UserName + "(p2)이 모든 적함을 격침하여 승리했습니다!");
			} else {
				if(player.equals("1")) WriteLog(p2.UserName + "(p2)이 게임을 포기하여 " + p1.UserName + "(p1)이 승리했습니다!");
				else WriteLog(p1.UserName + "(p1)이 게임을 포기하여 " + p2.UserName + "(p2)이 승리했습니다!");
			}
			gameStarted = false;
			turn = 0;
			p1Board = null;
			p2Board = null;
		}
		
		public boolean isGaming() {
			if(turn == 0) return false;
			else return true;
		}
		
		public void attack(String axis, String player) {
			String[] axises = axis.split("/");
			int row = Integer.parseInt(axises[0]);
			int col = Integer.parseInt(axises[1]);
			
			char colCh = (char)((int)axises[1].charAt(0) + 17);
			
			if(player.equals("1")) { //TODO 승리 검사
				switch(p2Board.hit(row, col)) {
				case 0: // 쏜데 또때림
					p1.WriteOne("이미 공격한 곳입니다. 공격할 곳을 다시 선택해 주십시오.");
					break;
				case 1: // 퐁당
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격이 빗나갔습니다.");
					WriteLog(p2.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					turn = 2;
					break;
				case 2: // 콰광
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격이 미확인 함선에 적중했습니다.");
					WriteLog("다시 " + p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					break;
				case 3: // dd 파괴
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격으로 적 구축함이 침몰했습니다.");
					WriteLog("다시 " + p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					break;
				case 4: // cl 파괴
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격으로 적 순양함이 침몰했습니다.");
					WriteLog("다시 " + p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					break;
				case 5: // bb 파괴
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격으로 적 전함이 침몰했습니다.");
					WriteLog("다시 " + p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					break;
				case 6: // cv 파괴
					WriteLog(p1.UserName + "(p1)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p1)의 공격으로 적 항공모함이 침몰했습니다.");
					WriteLog("다시 " + p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					break;
				}
			}
			else if(player.equals("2")) {
				switch(p1Board.hit(row, col)) {
				case 0: // 쏜데 또때림
					p2.WriteOne("이미 공격한 곳입니다. 공격할 곳을 다시 선택해 주십시오.");
					break;
				case 1: // 퐁당
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p2.UserName + "(p2)의 공격이 빗나갔습니다.");
					WriteLog(p1.UserName + "(p1)의 턴입니다.");
					redrawBoard();
					turn = 1;
					break;
				case 2: // 콰광
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p2.UserName + "(p2)의 공격이 미확인 함선에 적중했습니다.");
					WriteLog("다시 " + p2.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					break;
				case 3: // dd 파괴
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p2.UserName + "(p2)의 공격으로 적 구축함이 침몰했습니다.");
					WriteLog("다시 " + p2.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					break;
				case 4: // cl 파괴
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p2)의 공격으로 적 순양함이 침몰했습니다.");
					WriteLog("다시 " + p2.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					break;
				case 5: // bb 파괴
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p1.UserName + "(p2)의 공격으로 적 전함이 침몰했습니다.");
					WriteLog("다시 " + p2.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					break;
				case 6: // cv 파괴
					WriteLog(p2.UserName + "(p2)이 [" + colCh + "/" + row + "]을 공격했습니다.");
					WriteLog(p2.UserName + "(p2)의 공격으로 적 항공모함이 침몰했습니다.");
					WriteLog("다시 " + p1.UserName + "(p2)의 턴입니다.");
					redrawBoard();
					break;
				}
			}
			
			if(p1Board.allDoomed()) {
				GameOver("2", true);
			} else if(p2Board.allDoomed()) {
				GameOver("1", true);
			}
		}
		
		private void WriteLog(String str)  {
			UserService user;
			GameSignal gs = new GameSignal("Server", "304", str);
			for(int i = 0; i < UserVec.size(); i++) {
				user = (UserService)UserVec.elementAt(i);
				user.WriteGameSignal(gs);
			}
			try {
				if(!gameStarted) { // 게임이 새로 시작했으면 초기화하고 다시쓴다.
					fos = new FileOutputStream(".\\logs\\" + roomCode + ".txt");
					gameStarted = true;
				}
				else fos = new FileOutputStream(".\\logs\\" + roomCode + ".txt", true); // 게임이 진행중이면 이어쓴다
				fos.write((str + "\n").getBytes());
				fos.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		// 현재 모든 유저 list를 정리해서 data에 들어갈 String으로 리턴하는 메소드
		public String dataizeUser() {
			UserService temp;
			String userData;
			if(p1 == null) { userData = "*/"; }
			else userData = p1.UserName + "/";
			if(p2 == null) { userData = userData.concat("*"); }
			else userData = userData.concat(p2.UserName);
			
			for(int i = 0; i < UserVec.size(); i++) {
				if (UserVec.elementAt(i) == p1) continue;
				else if(UserVec.elementAt(i) == p2) continue;	
				temp = (UserService)UserVec.elementAt(i);
				userData = userData.concat("/" + temp.UserName);
			}
			return userData;
		}
		
		public void redrawBoard() {
			UserService user;
			GameSignal gsToObserver = new GameSignal("Server", "332", dataizeUser(), p1Board.getTrueCells(), p2Board.getTrueCells());
			GameSignal gsToP1 = new GameSignal("Server", "332", dataizeUser(), p1Board.getTrueCells(), p2Board.getHiddenCells());
			GameSignal gsToP2 = new GameSignal("Server", "332", dataizeUser(), p1Board.getHiddenCells(), p2Board.getTrueCells());
			
			for(int i = 0; i < UserVec.size(); i++) {
				user = (UserService)UserVec.elementAt(i);
				if(user == p1) user.WriteGameSignal(gsToP1);
				else if(user == p2) user.WriteGameSignal(gsToP2);
				else user.WriteGameSignal(gsToObserver);
			}
		}
		
		public void run() {
			
		}
	}
	
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;
		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Socket client_socket;
		//private Vector user_vc;
		public String UserName = "";
		private RoomService registeredRoom;

		public UserService(Socket client_socket) {
			// 매개변수로 넘어온 자료 저장
			this.client_socket = client_socket;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Login(String code) {
			int i;
			
			for(i = 0; i < RoomVec.size(); i++) { // Room 벡터를 돌아가면서 데이터로온 코드를 가진 Room있는지 검색
				RoomService room = (RoomService)RoomVec.elementAt(i);
				if(code.equals(room.roomCode)) { // 코드가 같은 Room을 발견했다면 room과 user를 이어준다.
					room.UserVec.add(this);
					if(room.p1 == null) {
						room.p1 = this;
					} else if (room.p2 == null) {
						room.p2 = this;
					}
					registeredRoom = room;
					break;
				}
			}
			if(i == RoomVec.size()) { // 해당 코드를 가진 방을 발견하지 못했다면
				RoomService room = new RoomService(code); // 새 방을 생성한 후에 room과 user를 이어준다.					
				room.UserVec.add(this);
				room.p1 = this; // 첫 방 생성자는 무조건 p1로 배정된다.
				RoomVec.add(room);
				registeredRoom = room;
			}
			AppendText(registeredRoom.roomCode + "번 방에 새로운 참가자 " + UserName + " 입장.");
			WriteOne("Welcome to BattleShip Game\n");
			WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			WriteOthers(msg);
			if(registeredRoom.isGaming()) { // 게임중이면 게임화면이랑 유저 수 같이 update, 이후 들어온사람에게 로그 update
				registeredRoom.redrawBoard();
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(".\\logs\\" + registeredRoom.roomCode + ".txt"));
					while(true) {
						String line = br.readLine();
						if (line == null) break;
						WriteGameSignal(new GameSignal("Server", "304", line));
					}
				} catch (FileNotFoundException e) { e.printStackTrace(); }
				catch (IOException e) { e.printStackTrace(); }
			}
			else WriteAllObject(new GameSignal("Server", "331", registeredRoom.dataizeUser())); // 게임중 아니면 유저수만 update
		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			WriteOthers(msg); // 나를 제외한 다른 User들에게 전송
			this.client_socket = null;
			AppendText(registeredRoom.roomCode + "번 방의 [" + UserName + "] 퇴장. 현재 참가자 수 : " + (registeredRoom.UserVec.size() - 1));
			if(registeredRoom.p1 == this) {
				registeredRoom.GameOver("2", false);
				registeredRoom.p1 = null;
			}
			else if(registeredRoom.p2 == this) {
				registeredRoom.GameOver("1", false);
				registeredRoom.p2 = null;
			}
			registeredRoom.UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WriteAllObject(new GameSignal(UserName, "331", registeredRoom.dataizeUser()));
		}
		
		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public synchronized void WriteAll(String str) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				user.WriteOne(str);
			}
		}
		
		// 모든 User들에게 Object를 방송. 채팅 message를 보낼 수 있다
		public synchronized void WriteAllObject(GameSignal obj) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				user.WriteGameSignal(obj);
			}
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public synchronized void WriteOthers(String str) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				if(user != this) user.WriteOne(str);
			}
		}

		// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
		public byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread가 담당하는 Client 에게 1:1 전송
		public synchronized void WriteOne(String msg) {
			GameSignal obcm = new GameSignal("SERVER", "301", msg);
			WriteGameSignal(obcm);
		}

		// 1:1로 GameSignal 전송
		public synchronized void WriteGameSignal(GameSignal obj) {
			try {
			    oos.writeObject(obj);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Logout();
			}
		}
				
		public GameSignal ReadGameSignal() {
			Object obj = null;
			String msg = null;
			GameSignal gs;
			try {
				obj = ois.readObject();
			} catch (ClassNotFoundException e) {
				Logout();
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				e.printStackTrace();
				Logout();
				return null;
			}
			if (obj instanceof GameSignal) {
				gs = (GameSignal) obj;
				if(gs.code.equals("301")) AppendText(registeredRoom.roomCode + "번 방의 [" + UserName + "] : " + gs.data);
				return gs;
			} else return null;
		}
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				GameSignal gs = null; 
				if (client_socket == null)
					break;
				gs = ReadGameSignal();
				if (gs==null)
					break;
				if (gs.code.length()==0)
					break;
				if (gs.code.matches("101")) { // login 작업
					UserName = gs.UserName;
					Login(gs.data);
				} else if (gs.code.matches("301")) { // 일반 chat 작업
					if(registeredRoom.isGaming()) { // 게임 중이면 관전자 채팅은 관전자 끼리만 볼 수 있게 처리함
						if(this == registeredRoom.p1 || this == registeredRoom.p2) WriteAllObject(gs);
						else {
							gs.UserName = "관전자][" + gs.UserName;
							for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
								UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
								if(user != registeredRoom.p1 && user != registeredRoom.p2)
									user.WriteGameSignal(gs);
							}
						}
					}
					else WriteAllObject(gs);
				} else if (gs.code.matches("311")) { // p1이 공격함
					if(!registeredRoom.isGaming()) continue; // 게임중이 아니거나 p1이 아니거나, p1의 턴이 아니면 컨티뉴
					else if(registeredRoom.p1 != this) continue;
					else if(registeredRoom.turn != 1) continue;
					
					registeredRoom.attack(gs.data, "1");
				} else if (gs.code.matches("312")) { // p2가 공격함
					if(!registeredRoom.isGaming()) continue; // 게임중이 아니거나 p2가 아니면 컨티뉴
					else if(registeredRoom.p2 != this) continue;
					else if(registeredRoom.turn != 2) continue;
					
					registeredRoom.attack(gs.data, "2");
					
				} else if (gs.code.matches("313")) { // 게임 시작 작업
					if(!registeredRoom.isGaming()) {
						if(registeredRoom.p1 != this) { // p1만 게임시작 가능
							WriteOne("p1만이 게임을 시작할 수 있습니다.");
							continue; 
						}
						if(registeredRoom.p1 == null || registeredRoom.p2 == null) { // p1이나 p2중 한명이라도 없을시 게임 시작 불가
							WriteOne("p1, p2가 모두 있어야 게임을 시작할 수 있습니다.");
							continue; 
						}
						registeredRoom.GameStart();
					} else WriteOne("이미 게임 진행 중 입니다.");
				} else if (gs.code.matches("314")) { // 기권 작업
					if(registeredRoom.isGaming()) {
						if(this == registeredRoom.p1) registeredRoom.GameOver("2", false);
						else if(this == registeredRoom.p2) registeredRoom.GameOver("1", false);
						else WriteOne("플레이 선수만 기권할 수 있습니다.");
					}
				} else if (gs.code.matches("321")) { // 종료 작업
					Logout();
				}
			} // while
		} // run
	}
}
