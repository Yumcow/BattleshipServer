import java.util.Random;

public class GameBoard {
	private int[][] cells = new int[10][10]; // 정상 0, 배 없이 피격 1, DD 300, CL 400, BB 500, CV 600
	// hidden 배열의 경우 : 미관찰 cell 2, 명중 했으나 함종 구분 안되는 경우 3
	// 배 100의 자리수 : 함종 구분
	// 배 10의 자리수 : 피격 여부, 미피격 0, 피격 1
	// 배 1의 자리수 : 배 모델링 구분
	// cells 앞의 칸이 행(가로) 뒤의 칸이 열(세로)
	private int carrier = 5;
	private int battleShip = 4;
	private int cruiser = 3;
	private int destroyer = 2;
	
	public GameBoard() {
		for(int i = 0; i < 10; i++)
			for (int j = 0; j < 10; j++)
				cells[i][j] = 0; // 보드 초기화
	}
	
	public void collocateShips() {
		boolean horizon;
		int row, col; // row(행), col(열)
		int i;
		Random random = new Random();
		
		// carrier 배치 //
		horizon = random.nextBoolean();
		if(horizon) { // carrier의 자세가 가로로 정해졌을 경우
			row = random.nextInt(6);
			col = random.nextInt(10); // 시작점, 오른쪽으로 뻗어나감
			
			for(i = 0; i < 5; i++)
				cells[row + i][col] = 600 + i;
		}
		else { // carrier의 자세가 세로로 정해졌을 경우
			row = random.nextInt(10);
			col = random.nextInt(6); // 시작점, 아래로 뻗어나감
			
			for(i = 0; i < 5; i++)
				cells[row][col + i] = 605 + i;
		}
		
		// battleship 배치 //
		while (true) { // 배가 겹치면 안되므로 겹치면 다시 배치하기위해 반복문 사용
			horizon = random.nextBoolean();
			if(horizon) { // battleship의 자세가 가로로 정해졌을 경우
				row = random.nextInt(7);
				col = random.nextInt(10); // 시작점, 오른쪽으로 뻗어나감
				
				for(i = 0; i < 4; i++)
					if(cells[row + i][col] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 4) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 4; i++)
					cells[row + i][col] = 500 + i;
				break;
			}
			else { // battleship의 자세가 세로로 정해졌을 경우
				row = random.nextInt(10);
				col = random.nextInt(7); // 시작점, 아래로 뻗어나감
				
				for(i = 0; i < 4; i++)
					if(cells[row][col + i] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 4) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 4; i++)
					cells[row][col + i] = 504 + i;
				break;
			}
		}
		
		// cruiser 배치 //
		while (true) { // 배가 겹치면 안되므로 겹치면 다시 배치하기위해 반복문 사용
			horizon = random.nextBoolean();
			if(horizon) { // cruiser의 자세가 가로로 정해졌을 경우
				row = random.nextInt(8);
				col = random.nextInt(10); // 시작점, 오른쪽으로 뻗어나감
				
				for(i = 0; i < 3; i++)
					if(cells[row + i][col] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 3) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 3; i++)
					cells[row + i][col] = 400 + i;
				break;
			}
			else { // cruiser의 자세가 세로로 정해졌을 경우
				row = random.nextInt(10);
				col = random.nextInt(8); // 시작점, 아래로 뻗어나감
				
				for(i = 0; i < 3; i++)
					if(cells[row][col + i] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 3) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 3; i++)
					cells[row][col + i] = 403 + i;
				break;
			}
		}
		
		// destroyer 배치 //
		while (true) { // 배가 겹치면 안되므로 겹치면 다시 배치하기위해 반복문 사용
			horizon = random.nextBoolean();
			if(horizon) { // destroyer의 자세가 가로로 정해졌을 경우
				row = random.nextInt(9);
				col = random.nextInt(10); // 시작점, 오른쪽으로 뻗어나감
						
				for(i = 0; i < 2; i++)
					if(cells[row + i][col] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 2) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 2; i++)
					cells[row + i][col] = 300 + i;
				break;
			}
			else { // destroyer의 자세가 세로로 정해졌을 경우
				row = random.nextInt(10);
				col = random.nextInt(9); // 시작점, 아래로 뻗어나감
				
				for(i = 0; i < 2; i++)
					if(cells[row][col + i] != 0) break; // 배가 이미 있는지 검사작업
				if(i != 2) continue; // 배가 있으면 다시배치
				
				for(i = 0; i < 2; i++)
					cells[row][col + i] = 302 + i;
				break;
			}
		}
	} // end of collocateShips()
	
	public int[][] getTrueCells() { // 적나라한 cells 상황 return
		int[][] temp = new int[10][10];
		for(int i = 0; i < 10; i++) 
			for(int j = 0; j <10; j++)
				temp[i][j] = cells[i][j];
		return temp;
	}
	
	public int[][] getHiddenCells() { // 정보가 제한된 cells 상황 return
		int[][] temp = new int[10][10];
		int n100, n10;
		for(int i = 0; i < 10; i++)
			for(int j = 0; j < 10; j++) {
				if(cells[i][j] == 0) { // 정상해양시 미관찰
					temp[i][j] = 2;
					continue;
				}
				else if(cells[i][j] == 1) { // 퐁당시 퐁당 그대로
					temp[i][j] = 1;
					continue;
				}
				n100 = cells[i][j] / 100; // 함종구분
				n10 = (cells[i][j] % 100) / 10; // 피격 여부
				if (n10 == 0) temp[i][j] = 2; // 미피격시 미관찰
				else { // 피격시 완파 or 부분파괴 확인
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
	
	public int hit(int row, int col) { // return값 -1 : 에러 / 0: 쏜데 또 때림 / 1: 배없는 곳 피격 / 2: 배있는 곳 피격 / 3: dd 파괴 / 4: cl 파괴 / 5: bb 파괴 / 6: cv 파괴
		int n100;
		int n10;
		if(cells[row][col] == 0) { // 잔잔
			cells[row][col] = 1;
			return 1;
		} else if(cells[row][col] == 1) { // 퐁당
			return 0;
		} else {
			n100 = cells[row][col] / 100; //함종구분
			n10 = cells[row][col] % 100 / 10; // 피격 여부
			switch(n100) {
			case 3: // dd
				if(n10 == 1) return 0; // 때린데 또때림
				cells[row][col] += 10; // 피격처리
				if(--destroyer == 0) return 3;
				return 2;
			case 4: // cl
				if(n10 == 1) return 0; // 때린데 또때림
				cells[row][col] += 10; // 피격처리
				if(--cruiser == 0) return 4;
				return 2;
			case 5: // bb
				if(n10 == 1) return 0; // 때린데 또때림
				cells[row][col] += 10; // 피격처리
				if(--battleShip == 0) return 5;
				return 2;
			case 6: // cv
				if(n10 == 1) return 0; // 때린데 또때림
				cells[row][col] += 10; // 피격처리
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
