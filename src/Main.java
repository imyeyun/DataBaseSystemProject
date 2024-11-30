import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Connection conn;
    // 1. 동아리 개설하기
    private static void createClub(Scanner scanner) {
        try {
            System.out.print("동아리 이름을 입력하세요: ");
            String name = scanner.nextLine();
            System.out.print("동아리 정보를 입력하세요: ");
            String description = scanner.nextLine();

            String sql = "INSERT INTO Club (name, description) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, name);
                stmt.setString(2, description);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("동아리가 개설되었습니다.");
                } else {
                    System.out.println("동아리 개설에 실패했습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("동아리 개설 중 오류 발생: " + e.getMessage());
        }
    }

    // 2. 동아리 정보 수정 (회장만 가능)
    private static void updateClubInfo(Scanner scanner) {
        try {
            // 동아리 이름 입력받기
            System.out.print("동아리 이름을 입력하세요: ");
            String clubName = scanner.nextLine();

            // 동아리 이름에 해당하는 club_id 찾기
            String clubQuery = "SELECT club_id FROM Club WHERE name = ?";
            PreparedStatement clubStmt = conn.prepareStatement(clubQuery);
            clubStmt.setString(1, clubName);
            ResultSet clubRs = clubStmt.executeQuery();

            if (clubRs.next()) {
                int clubId = clubRs.getInt("club_id");

                // 회장인지 확인하기 위해 부원 테이블에서 role 확인
                System.out.print("회장 ID를 입력하세요: ");
                int memberId = scanner.nextInt();
                scanner.nextLine();  // 버퍼 비우기

                String roleQuery = "SELECT role FROM Member WHERE student_id = ? AND club_id = ?";
                PreparedStatement roleStmt = conn.prepareStatement(roleQuery);
                roleStmt.setInt(1, memberId);
                roleStmt.setInt(2, clubId);
                ResultSet roleRs = roleStmt.executeQuery();

                if (roleRs.next() && roleRs.getString("role").equals("동아리 회장")) {
                    // 회장일 경우 동아리 정보 수정
                    System.out.print("새로운 동아리 정보를 입력하세요: ");
                    String newInfo = scanner.nextLine();

                    String updateQuery = "UPDATE Club SET description = ? WHERE club_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setString(1, newInfo);
                    updateStmt.setInt(2, clubId);
                    int rowsAffected = updateStmt.executeUpdate();

                    if (rowsAffected > 0) {
                        System.out.println("동아리 정보가 수정되었습니다.");
                    } else {
                        System.out.println("동아리 정보 수정에 실패했습니다.");
                    }
                } else {
                    System.out.println("회장만 동아리 정보를 수정할 수 있습니다.");
                }
            } else {
                System.out.println("해당 동아리를 찾을 수 없습니다.");
            }
        } catch (SQLException e) {
            System.out.println("동아리 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }

    // 3. 동아리 실적 내역 확인
    private static void viewClubPerformance(Scanner scanner) {
        try {
            System.out.print("동아리 ID를 입력하세요: ");
            int clubId = scanner.nextInt();
            String sql = "SELECT * FROM Activity WHERE club_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, clubId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        System.out.println("실적 ID: " + rs.getInt("activity_id"));
                        System.out.println("실적 정보: " + rs.getString("description"));
                        System.out.println("-----------");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("동아리 실적 내역 조회 중 오류 발생: " + e.getMessage());
        }
    }
    // 4. 동아리 실적 내역 수정
    private static void updatePerformance(Scanner scanner) {
        try {
            System.out.print("실적 ID를 입력하세요: ");
            int performanceId = scanner.nextInt();
            scanner.nextLine();  // 버퍼 비우기
            System.out.print("새로운 실적 정보를 입력하세요: ");
            String performanceInfo = scanner.nextLine();

            String sql = "UPDATE Activity SET description = ? WHERE activity_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, performanceInfo);
                stmt.setInt(2, performanceId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("실적 내역이 수정되었습니다.");
                } else {
                    System.out.println("실적 수정에 실패했습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("실적 수정 중 오류 발생: " + e.getMessage());
        }
    }

    // 5. 동아리 목록 조회
    private static void listClubs() {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Club")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("동아리 ID: " + rs.getInt("club_id") + ", 이름: " + rs.getString("name") + ", 정보: " + rs.getString("description"));
            }
        } catch (SQLException e) {
            System.out.println("동아리 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 6. 부원 신청
    private static void applyForMembership(Scanner scanner) {
        System.out.print("동아리 ID를 입력하세요: ");
        int clubId = scanner.nextInt();
        System.out.print("부원 이름을 입력하세요: ");
        scanner.nextLine();  // 버퍼 비우기
        String memberName = scanner.nextLine();

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Member (club_id, name) VALUES (?, ?)")) {
            stmt.setInt(1, clubId);
            stmt.setString(2, memberName);
            stmt.executeUpdate();
            System.out.println("부원이 신청되었습니다.");
        } catch (SQLException e) {
            System.out.println("부원 신청 중 오류 발생: " + e.getMessage());
        }
    }

    // 7. 부원 목록 조회
    private static void listMembers(Scanner scanner) {
        System.out.print("동아리 ID를 입력하세요: ");
        int clubId = scanner.nextInt();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Member WHERE club_id = ?")) {
            stmt.setInt(1, clubId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("부원 ID: " + rs.getInt("member_id") + ", 이름: " + rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("부원 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 8. 부원 정보 수정
    // 부원 정보 수정
    private static void updateMemberInfo(Scanner scanner) {
        System.out.print("부원 ID를 입력하세요: ");
        int memberId = scanner.nextInt();
        System.out.print("새로운 부원 이름을 입력하세요: ");
        scanner.nextLine();  // 버퍼 비우기
        String newMemberInfo = scanner.nextLine();

        try (PreparedStatement stmt = conn.prepareStatement("UPDATE Member SET name = ? WHERE member_id = ?")) {
            stmt.setString(1, newMemberInfo);
            stmt.setInt(2, memberId);
            stmt.executeUpdate();
            System.out.println("부원 정보가 수정되었습니다.");
        } catch (SQLException e) {
            System.out.println("부원 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }


    // 9. 회비 납부
    private static void makePayment(Scanner scanner) {
        System.out.print("부원 ID를 입력하세요: ");
        int memberId = scanner.nextInt();
        System.out.print("회비 금액을 입력하세요: ");
        double amount = scanner.nextDouble();

        try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO Payment (member_id, amount) VALUES (?, ?)")) {
            stmt.setInt(1, memberId);
            stmt.setDouble(2, amount);
            stmt.executeUpdate();
            System.out.println("회비가 납부되었습니다.");
        } catch (SQLException e) {
            System.out.println("회비 납부 중 오류 발생: " + e.getMessage());
        }
    }


    // 10. 회비 납부 내역
    private static void viewPaymentHistory(Scanner scanner) {
        System.out.print("동아리 ID를 입력하세요: ");
        int clubId = scanner.nextInt();

        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Payment WHERE member_id IN (SELECT member_id FROM Member WHERE club_id = ?)")) {
            stmt.setInt(1, clubId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                System.out.println("부원 ID: " + rs.getInt("member_id") + ", 금액: " + rs.getDouble("amount"));
            }
        } catch (SQLException e) {
            System.out.println("회비 납부 내역 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 메뉴 출력 함수
    public static void displayMenu() {
        System.out.println("\n===== 동아리 관리 시스템 =====");
        System.out.println("1. 동아리 개설");
        System.out.println("2. 동아리 정보 수정");
        System.out.println("3. 동아리 실적 내역 확인");
        System.out.println("4. 동아리 실적 내역 수정");
        System.out.println("5. 동아리 목록 조회");
        System.out.println("6. 부원 신청");
        System.out.println("7. 부원 목록 조회");
        System.out.println("8. 부원 정보 수정");
        System.out.println("9. 회비 납부");
        System.out.println("10. 회비 납부 내역 조회");
        System.out.println("0. 종료");
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://192.168.56.102:4567/cbnuclub?useSSL=false", "iyy1001", "1234");
            System.out.println("Database connected!");
            Scanner scanner = new Scanner(System.in);
            // 메뉴 구현
            boolean running = true;
            while (running) {
                displayMenu();
                System.out.print("원하는 작업 번호를 입력하세요: ");
                int choice = scanner.nextInt();
                scanner.nextLine();  // 버퍼 비우기
                switch (choice) {

                    case 1:
                        createClub(scanner);
                        break;
                    case 2:
                        updateClubInfo(scanner);
                        break;
                    case 3:
                        viewClubPerformance(scanner);
                        break;
                    case 4:
                        updatePerformance(scanner);
                        break;
                    case 5:
                        listClubs();
                        break;
                    case 6:
                        applyForMembership(scanner);
                        break;
                    case 7:
                        listMembers(scanner);
                        break;
                    case 8:
                        updateMemberInfo(scanner);
                        break;
                    case 9:
                        makePayment(scanner);
                        break;
                    case 10:
                        viewPaymentHistory(scanner);
                        break;
                    case 0:
                        System.out.println("프로그램을 종료합니다.");
                        running = false;  // 종료
                        break;
                    default:
                        System.out.println("잘못된 입력입니다. 다시 시도하세요.");

                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
