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

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector RoomVec = new Vector(); // ����� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����
	
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
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
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
	
	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					new_user.start(); // ���� ��ü�� ������ ����
				} catch (IOException e) {
					AppendText("accept() error");
					//System.exit(0);
				}
			}
		}
	}

	public void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}


	class RoomService extends Thread {
		public String roomCode;
		public Vector UserVec = new Vector(); // ����� ����ڵ��� ������ ����
		public UserService p1;
		public UserService p2;
		private GameBoard p1Board;
		private GameBoard p2Board;
		private int turn; // 0 = �����, 1 = 1p, 2 = 2p | 1, 2�϶��� ������ ��������
		private boolean gameStarted; // ������ ���� �Ǿ�����? �α����� �Է��Ҷ� �ʱ�ȭ���� �̾�� �����ϱ� ����
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
			
			WriteLog("������ ���� �Ǿ����ϴ�.");
			WriteLog(p1.UserName + "(p1)�� ���Դϴ�.");
		}
		
		public void GameOver(String player, boolean normal) { // player : �¸��� / normal : �������� ����(�������� ��������, ���, ��������� ��������)
			if(normal) {
				if(player.equals("1")) WriteLog(p1.UserName + "(p1)�� ��� ������ ��ħ�Ͽ� �¸��߽��ϴ�!");
				else WriteLog(p2.UserName + "(p2)�� ��� ������ ��ħ�Ͽ� �¸��߽��ϴ�!");
			} else {
				if(player.equals("1")) WriteLog(p2.UserName + "(p2)�� ������ �����Ͽ� " + p1.UserName + "(p1)�� �¸��߽��ϴ�!");
				else WriteLog(p1.UserName + "(p1)�� ������ �����Ͽ� " + p2.UserName + "(p2)�� �¸��߽��ϴ�!");
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
			
			if(player.equals("1")) { //TODO �¸� �˻�
				switch(p2Board.hit(row, col)) {
				case 0: // �� �Ƕ���
					p1.WriteOne("�̹� ������ ���Դϴ�. ������ ���� �ٽ� ������ �ֽʽÿ�.");
					break;
				case 1: // ����
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� ������ ���������ϴ�.");
					WriteLog(p2.UserName + "(p2)�� ���Դϴ�.");
					redrawBoard();
					turn = 2;
					break;
				case 2: // �Ɽ
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� ������ ��Ȯ�� �Լ��� �����߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 3: // dd �ı�
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� �������� �� �������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 4: // cl �ı�
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� �������� �� �������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 5: // bb �ı�
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� �������� �� ������ ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 6: // cv �ı�
					WriteLog(p1.UserName + "(p1)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p1)�� �������� �� �װ������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					break;
				}
			}
			else if(player.equals("2")) {
				switch(p1Board.hit(row, col)) {
				case 0: // �� �Ƕ���
					p2.WriteOne("�̹� ������ ���Դϴ�. ������ ���� �ٽ� ������ �ֽʽÿ�.");
					break;
				case 1: // ����
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p2.UserName + "(p2)�� ������ ���������ϴ�.");
					WriteLog(p1.UserName + "(p1)�� ���Դϴ�.");
					redrawBoard();
					turn = 1;
					break;
				case 2: // �Ɽ
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p2.UserName + "(p2)�� ������ ��Ȯ�� �Լ��� �����߽��ϴ�.");
					WriteLog("�ٽ� " + p2.UserName + "(p2)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 3: // dd �ı�
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p2.UserName + "(p2)�� �������� �� �������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p2.UserName + "(p2)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 4: // cl �ı�
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p2)�� �������� �� �������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p2.UserName + "(p2)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 5: // bb �ı�
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p1.UserName + "(p2)�� �������� �� ������ ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p2.UserName + "(p2)�� ���Դϴ�.");
					redrawBoard();
					break;
				case 6: // cv �ı�
					WriteLog(p2.UserName + "(p2)�� [" + colCh + "/" + row + "]�� �����߽��ϴ�.");
					WriteLog(p2.UserName + "(p2)�� �������� �� �װ������� ħ���߽��ϴ�.");
					WriteLog("�ٽ� " + p1.UserName + "(p2)�� ���Դϴ�.");
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
				if(!gameStarted) { // ������ ���� ���������� �ʱ�ȭ�ϰ� �ٽþ���.
					fos = new FileOutputStream(".\\logs\\" + roomCode + ".txt");
					gameStarted = true;
				}
				else fos = new FileOutputStream(".\\logs\\" + roomCode + ".txt", true); // ������ �������̸� �̾��
				fos.write((str + "\n").getBytes());
				fos.close();
			} catch (IOException e) { e.printStackTrace(); }
		}
		
		// ���� ��� ���� list�� �����ؼ� data�� �� String���� �����ϴ� �޼ҵ�
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
			// �Ű������� �Ѿ�� �ڷ� ����
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
			
			for(i = 0; i < RoomVec.size(); i++) { // Room ���͸� ���ư��鼭 �����ͷο� �ڵ带 ���� Room�ִ��� �˻�
				RoomService room = (RoomService)RoomVec.elementAt(i);
				if(code.equals(room.roomCode)) { // �ڵ尡 ���� Room�� �߰��ߴٸ� room�� user�� �̾��ش�.
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
			if(i == RoomVec.size()) { // �ش� �ڵ带 ���� ���� �߰����� ���ߴٸ�
				RoomService room = new RoomService(code); // �� ���� ������ �Ŀ� room�� user�� �̾��ش�.					
				room.UserVec.add(this);
				room.p1 = this; // ù �� �����ڴ� ������ p1�� �����ȴ�.
				RoomVec.add(room);
				registeredRoom = room;
			}
			AppendText(registeredRoom.roomCode + "�� �濡 ���ο� ������ " + UserName + " ����.");
			WriteOne("Welcome to BattleShip Game\n");
			WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			WriteOthers(msg);
			if(registeredRoom.isGaming()) { // �������̸� ����ȭ���̶� ���� �� ���� update, ���� ���»������ �α� update
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
			else WriteAllObject(new GameSignal("Server", "331", registeredRoom.dataizeUser())); // ������ �ƴϸ� �������� update
		}

		public void Logout() {
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			WriteOthers(msg); // ���� ������ �ٸ� User�鿡�� ����
			this.client_socket = null;
			AppendText(registeredRoom.roomCode + "�� ���� [" + UserName + "] ����. ���� ������ �� : " + (registeredRoom.UserVec.size() - 1));
			if(registeredRoom.p1 == this) {
				registeredRoom.GameOver("2", false);
				registeredRoom.p1 = null;
			}
			else if(registeredRoom.p2 == this) {
				registeredRoom.GameOver("1", false);
				registeredRoom.p2 = null;
			}
			registeredRoom.UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
			WriteAllObject(new GameSignal(UserName, "331", registeredRoom.dataizeUser()));
		}
		
		// ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public synchronized void WriteAll(String str) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				user.WriteOne(str);
			}
		}
		
		// ��� User�鿡�� Object�� ���. ä�� message�� ���� �� �ִ�
		public synchronized void WriteAllObject(GameSignal obj) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				user.WriteGameSignal(obj);
			}
		}

		// ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public synchronized void WriteOthers(String str) {
			for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
				UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
				if(user != this) user.WriteOne(str);
			}
		}

		// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
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

		// UserService Thread�� ����ϴ� Client ���� 1:1 ����
		public synchronized void WriteOne(String msg) {
			GameSignal obcm = new GameSignal("SERVER", "301", msg);
			WriteGameSignal(obcm);
		}

		// 1:1�� GameSignal ����
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
				if(gs.code.equals("301")) AppendText(registeredRoom.roomCode + "�� ���� [" + UserName + "] : " + gs.data);
				return gs;
			} else return null;
		}
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				GameSignal gs = null; 
				if (client_socket == null)
					break;
				gs = ReadGameSignal();
				if (gs==null)
					break;
				if (gs.code.length()==0)
					break;
				if (gs.code.matches("101")) { // login �۾�
					UserName = gs.UserName;
					Login(gs.data);
				} else if (gs.code.matches("301")) { // �Ϲ� chat �۾�
					if(registeredRoom.isGaming()) { // ���� ���̸� ������ ä���� ������ ������ �� �� �ְ� ó����
						if(this == registeredRoom.p1 || this == registeredRoom.p2) WriteAllObject(gs);
						else {
							gs.UserName = "������][" + gs.UserName;
							for (int i = 0; i < registeredRoom.UserVec.size(); i++) {
								UserService user = (UserService) registeredRoom.UserVec.elementAt(i);
								if(user != registeredRoom.p1 && user != registeredRoom.p2)
									user.WriteGameSignal(gs);
							}
						}
					}
					else WriteAllObject(gs);
				} else if (gs.code.matches("311")) { // p1�� ������
					if(!registeredRoom.isGaming()) continue; // �������� �ƴϰų� p1�� �ƴϰų�, p1�� ���� �ƴϸ� ��Ƽ��
					else if(registeredRoom.p1 != this) continue;
					else if(registeredRoom.turn != 1) continue;
					
					registeredRoom.attack(gs.data, "1");
				} else if (gs.code.matches("312")) { // p2�� ������
					if(!registeredRoom.isGaming()) continue; // �������� �ƴϰų� p2�� �ƴϸ� ��Ƽ��
					else if(registeredRoom.p2 != this) continue;
					else if(registeredRoom.turn != 2) continue;
					
					registeredRoom.attack(gs.data, "2");
					
				} else if (gs.code.matches("313")) { // ���� ���� �۾�
					if(!registeredRoom.isGaming()) {
						if(registeredRoom.p1 != this) { // p1�� ���ӽ��� ����
							WriteOne("p1���� ������ ������ �� �ֽ��ϴ�.");
							continue; 
						}
						if(registeredRoom.p1 == null || registeredRoom.p2 == null) { // p1�̳� p2�� �Ѹ��̶� ������ ���� ���� �Ұ�
							WriteOne("p1, p2�� ��� �־�� ������ ������ �� �ֽ��ϴ�.");
							continue; 
						}
						registeredRoom.GameStart();
					} else WriteOne("�̹� ���� ���� �� �Դϴ�.");
				} else if (gs.code.matches("314")) { // ��� �۾�
					if(registeredRoom.isGaming()) {
						if(this == registeredRoom.p1) registeredRoom.GameOver("2", false);
						else if(this == registeredRoom.p2) registeredRoom.GameOver("1", false);
						else WriteOne("�÷��� ������ ����� �� �ֽ��ϴ�.");
					}
				} else if (gs.code.matches("321")) { // ���� �۾�
					Logout();
				}
			} // while
		} // run
	}
}
