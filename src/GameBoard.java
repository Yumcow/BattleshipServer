import java.util.Random;

public class GameBoard {
	private int[][] cells = new int[10][10]; // ���� 0, �� ���� �ǰ� 1, DD 300, CL 400, BB 500, CV 600
	// hidden �迭�� ��� : �̰��� cell 2, ���� ������ ���� ���� �ȵǴ� ��� 3
	// �� 100�� �ڸ��� : ���� ����
	// �� 10�� �ڸ��� : �ǰ� ����, ���ǰ� 0, �ǰ� 1
	// �� 1�� �ڸ��� : �� �𵨸� ����
	// cells ���� ĭ�� ��(����) ���� ĭ�� ��(����)
	private int carrier = 5;
	private int battleShip = 4;
	private int cruiser = 3;
	private int destroyer = 2;
	
	public GameBoard() {
		for(int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				cells[i][j] = 0; // ���� �ʱ�ȭ
	}
	
	public void collocateShips() {
		boolean horizon;
		int row, col; // row(��), col(��)
		int i;
		Random random = new Random();
		
		// carrier ��ġ //
		horizon = random.nextBoolean();
		if(horizon) { // carrier�� �ڼ��� ���η� �������� ���
			row = random.nextInt(6);
			col = random.nextInt(10); // ������, ���������� �����
			
			for(i = 0; i < 5; i++)
				cells[row + i][col] = 600 + i;
		}
		else { // carrier�� �ڼ��� ���η� �������� ���
			row = random.nextInt(10);
			col = random.nextInt(6); // ������, �Ʒ��� �����
			
			for(i = 0; i < 5; i++)
				cells[row][col + i] = 605 + i;
		}
		
		// battleship ��ġ //
		while (true) { // �谡 ��ġ�� �ȵǹǷ� ��ġ�� �ٽ� ��ġ�ϱ����� �ݺ��� ���
			horizon = random.nextBoolean();
			if(horizon) { // battleship�� �ڼ��� ���η� �������� ���
				row = random.nextInt(7);
				col = random.nextInt(10); // ������, ���������� �����
				
				for(i = 0; i < 4; i++)
					if(cells[row + i][col] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 4) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 4; i++)
					cells[row + i][col] = 500 + i;
				break;
			}
			else { // battleship�� �ڼ��� ���η� �������� ���
				row = random.nextInt(10);
				col = random.nextInt(7); // ������, �Ʒ��� �����
				
				for(i = 0; i < 4; i++)
					if(cells[row][col + i] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 4) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 4; i++)
					cells[row][col + i] = 504 + i;
				break;
			}
		}
		
		// cruiser ��ġ //
		while (true) { // �谡 ��ġ�� �ȵǹǷ� ��ġ�� �ٽ� ��ġ�ϱ����� �ݺ��� ���
			horizon = random.nextBoolean();
			if(horizon) { // cruiser�� �ڼ��� ���η� �������� ���
				row = random.nextInt(8);
				col = random.nextInt(10); // ������, ���������� �����
				
				for(i = 0; i < 3; i++)
					if(cells[row + i][col] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 3) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 3; i++)
					cells[row + i][col] = 400 + i;
				break;
			}
			else { // cruiser�� �ڼ��� ���η� �������� ���
				row = random.nextInt(10);
				col = random.nextInt(8); // ������, �Ʒ��� �����
				
				for(i = 0; i < 3; i++)
					if(cells[row][col + i] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 3) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 3; i++)
					cells[row][col + i] = 403 + i;
				break;
			}
		}
		
		// destroyer ��ġ //
		while (true) { // �谡 ��ġ�� �ȵǹǷ� ��ġ�� �ٽ� ��ġ�ϱ����� �ݺ��� ���
			horizon = random.nextBoolean();
			if(horizon) { // destroyer�� �ڼ��� ���η� �������� ���
				row = random.nextInt(9);
				col = random.nextInt(10); // ������, ���������� �����
						
				for(i = 0; i < 2; i++)
					if(cells[row + i][col] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 2) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 2; i++)
					cells[row + i][col] = 300 + i;
				break;
			}
			else { // destroyer�� �ڼ��� ���η� �������� ���
				row = random.nextInt(10);
				col = random.nextInt(9); // ������, �Ʒ��� �����
				
				for(i = 0; i < 2; i++)
					if(cells[row][col + i] != 0) break; // �谡 �̹� �ִ��� �˻��۾�
				if(i != 2) continue; // �谡 ������ �ٽù�ġ
				
				for(i = 0; i < 2; i++)
					cells[row][col + i] = 302 + i;
				break;
			}
		}
	} // end of collocateShips()
	
	public int[][] getTrueCells() { // �������� cells ��Ȳ return
		int[][] temp = new int[10][10];
		for(int i = 0; i < 10; i++) 
			for(int j = 0; j <10; j++)
				temp[i][j] = cells[i][j];
		return temp;
	}
	
	public int[][] getHiddenCells() { // ������ ���ѵ� cells ��Ȳ return
		int[][] temp = new int[10][10];
		int n100, n10;
		for(int i = 0; i < 10; i++)
			for(int j = 0; j < 10; j++) {
				if(cells[i][j] == 0) { // �����ؾ�� �̰���
					temp[i][j] = 2;
					continue;
				}
				else if(cells[i][j] == 1) { // ����� ���� �״��
					temp[i][j] = 1;
					continue;
				}
				n100 = cells[i][j] / 100; // ��������
				n10 = (cells[i][j] % 100) / 10; // �ǰ� ����
				if (n10 == 0) temp[i][j] = 2; // ���ǰݽ� �̰���
				else { // �ǰݽ� ���� or �κ��ı� Ȯ��
					switch(n100) {
						case 3: // dd
							if (destroyer == 0) temp[i][j] = cells[i][j];
							else temp[i][j] = 3;
							continue;
						case 4: // cl
							if (cruiser == 0) temp[i][j] = cells[i][j];
							else temp[i][j] = 3;
							continue;
						case 5: // bb
							if (battleShip == 0) temp[i][j] = cells[i][j];
							else temp[i][j] = 3;
							continue;
						case 6: // cv
							if (carrier == 0) temp[i][j] = cells[i][j];
							else temp[i][j] = 3;
							continue;
					}
				}
			}
		return temp;
	}
	
	public int hit(int row, int col) { // return�� -1 : ���� / 0: �� �� ���� / 1: ����� �� �ǰ� / 2: ���ִ� �� �ǰ� / 3: dd �ı� / 4: cl �ı� / 5: bb �ı� / 6: cv �ı�
		int n100;
		int n10;
		if(cells[row][col] == 0) { // ����
			cells[row][col] = 1;
			return 1;
		} else if(cells[row][col] == 1) { // ����
			return 0;
		} else {
			n100 = cells[row][col] / 100; //��������
			n10 = cells[row][col] % 100 / 10; // �ǰ� ����
			switch(n100) {
			case 3: // dd
				if(n10 == 1) return 0; // ������ �Ƕ���
				cells[row][col] += 10; // �ǰ�ó��
				if(--destroyer == 0) return 3;
				return 2;
			case 4: // cl
				if(n10 == 1) return 0; // ������ �Ƕ���
				cells[row][col] += 10; // �ǰ�ó��
				if(--cruiser == 0) return 4;
				return 2;
			case 5: // bb
				if(n10 == 1) return 0; // ������ �Ƕ���
				cells[row][col] += 10; // �ǰ�ó��
				if(--battleShip == 0) return 5;
				return 2;
			case 6: // cv
				if(n10 == 1) return 0; // ������ �Ƕ���
				cells[row][col] += 10; // �ǰ�ó��
				if(--carrier == 0) return 6;
				return 2;
			default: return -1;
			}
		}
	}
	
	public boolean allDoomed() {
		if(destroyer == 0 && cruiser == 0) {
			if(battleShip ==0 && carrier == 0) return true;
			return false;
		}
		return false;
	}
}
