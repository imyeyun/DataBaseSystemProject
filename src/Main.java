import java.sql.*;
import java.util.Scanner;

public class Main {
    private static Connection conn;
    // 1. 동아리 개설하기
    private static void createClub(Scanner scanner) {
        try {
            System.out.print("동아리 이름을 입력하세요: ");
            String Cname = scanner.nextLine();
            System.out.print("동아리 정보를 입력하세요: ");
            String club_info = scanner.nextLine();

            String sql = "INSERT INTO Club (Cname, club_info) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, Cname);
                stmt.setString(2, club_info);
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
            String clubQuery = "SELECT club_id FROM Club WHERE Cname = ?";
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

                if (roleRs.next() && roleRs.getString("role").equals("동아리장")) {
                    // 회장일 경우 동아리 정보 수정
                    System.out.print("새로운 동아리 정보를 입력하세요: ");
                    String newInfo = scanner.nextLine();

                    String updateQuery = "UPDATE Club SET club_info = ? WHERE club_id = ?";
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
            System.out.print("동아리 이름을 입력하세요: ");
            String clubName = scanner.nextLine();

            // 동아리 이름으로 club_id 찾기
            String clubQuery = "SELECT club_id FROM Club WHERE Cname = ?";
            try (PreparedStatement clubStmt = conn.prepareStatement(clubQuery)) {
                clubStmt.setString(1, clubName);
                ResultSet clubRs = clubStmt.executeQuery();

                if (clubRs.next()) {
                    int clubId = clubRs.getInt("club_id");

                    // club_id로 Performance 테이블에서 실적 조회
                    String performanceQuery = "SELECT * FROM Performance WHERE club_id = ?";
                    try (PreparedStatement performanceStmt = conn.prepareStatement(performanceQuery)) {
                        performanceStmt.setInt(1, clubId);
                        ResultSet performanceRs = performanceStmt.executeQuery();

                        System.out.println("[" + clubName + "] 실적 내역");
                        while (performanceRs.next()) {
                            System.out.println("실적 ID: " + performanceRs.getInt("performance_id")+" 실적 정보: " + performanceRs.getString("performance_info")+"("+performanceRs.getString("performance_date")+")");
                            System.out.println("-----------");
                        }
                    }
                } else {
                    System.out.println("입력한 동아리를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("동아리 실적 내역 조회 중 오류 발생: " + e.getMessage());
        }
    }
    // 4. 실적 내용 등록
    private static void registerClubPerformance(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                System.out.print("실적 내용을 입력하세요: ");
                String performance_info = scanner.nextLine();
                System.out.print("실적 날짜를 입력하세요: ");
                String performance_date = scanner.nextLine();

                String insertQuery = "INSERT INTO Performance (club_id, performance_info,performance_date) VALUES (?, ?,?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setInt(1, clubId);
                    stmt.setString(2, performance_info);
                    stmt.setString(3, performance_date);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("실적 내용이 성공적으로 등록되었습니다.");
                    } else {
                        System.out.println("실적 내용 등록에 실패했습니다.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("실적 내용 등록 중 오류 발생: " + e.getMessage());
        }
    }

    // 4. 동아리 실적 내역 수정
    private static void updatePerformance(Scanner scanner) {
        try {
            System.out.print("실적 ID를 입력하세요: ");
            int performanceId = scanner.nextInt();
            scanner.nextLine();  // 버퍼 비우기
            System.out.print("수정할 항목을 선택하세요 ");
            System.out.print("1: 실적 정보  2: 실적 날짜 ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // 버퍼 비우기
            String NewValue = null;
            String sql = null;

            switch (choice)
            {
                case 1:
                    System.out.print("수정할 실적 정보를 입력하세요: ");
                    NewValue = scanner.nextLine();
                    sql = "UPDATE Performance SET performance_info = ? WHERE performance_id = ?";
                    break;
                case 2:
                    System.out.print("수정할 실적 날짜를 입력하세요: ");
                    NewValue = scanner.nextLine();
                    sql = "UPDATE Performance SET performance_date = ? WHERE performance_id = ?";
                    break;
            }



            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, NewValue);
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
                System.out.println("동아리 ID: " + rs.getInt("club_id") + ", 이름: " + rs.getString("Cname") + ", 정보: " + rs.getString("club_info"));
            }
        } catch (SQLException e) {
            System.out.println("동아리 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 6. 부원 신청
    private static void applyForMembership(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        // 동아리 이름으로 club_id 찾기
        String clubQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubQuery)) {
            clubStmt.setString(1, clubName);
            ResultSet clubRs = clubStmt.executeQuery();

            if (clubRs.next()) {
                int clubId = clubRs.getInt("club_id");

                // 부원 정보 입력
                System.out.print("학번을 입력하세요: ");
                int studentId = scanner.nextInt();
                scanner.nextLine(); // 버퍼 비우기
                System.out.print("부원의 이름을 입력하세요: ");
                String Mname = scanner.nextLine();
                System.out.print("부원의 학년을 입력하세요 (숫자): ");
                int grade = scanner.nextInt();
                scanner.nextLine(); // 버퍼 비우기
                System.out.print("부원의 성별을 입력하세요 (M/F): ");
                char gender = scanner.nextLine().charAt(0);
                System.out.print("부원의 상태를 입력하세요 (예: 활동 중, 휴학 중): ");
                String status = scanner.nextLine();
                System.out.print("부원의 역할을 선택하세요 (동아리장/부회장/일반부원): ");
                String role = scanner.nextLine();

                // 유효성 검사: 역할
                if (!role.equals("동아리장") && !role.equals("부회장") && !role.equals("일반부원")) {
                    System.out.println("잘못된 역할 입력입니다. 신청을 종료합니다.");
                    return;
                }

                // 데이터 삽입
                String insertQuery = "INSERT INTO Member (student_id,Mname, grade, gender, status, club_id, role) " +
                        "VALUES (?, ?, ?, ?, ?, ?,?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                    insertStmt.setInt(1, studentId);
                    insertStmt.setString(2, Mname);
                    insertStmt.setInt(3, grade);
                    insertStmt.setString(4, String.valueOf(gender));
                    insertStmt.setString(5, status);
                    insertStmt.setInt(6, clubId);
                    insertStmt.setString(7, role);

                    int rowsAffected = insertStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("부원 신청이 완료되었습니다.");
                    } else {
                        System.out.println("부원 신청에 실패했습니다.");
                    }
                }
            } else {
                System.out.println("입력하신 동아리를 찾을 수 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("부원 신청 중 오류 발생: " + e.getMessage());
        }
    }

    // 7. 부원 목록 조회
    private static void listMembers(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        try {
            // 동아리 이름으로 club_id 조회
            String clubQuery = "SELECT club_id FROM Club WHERE Cname = ?";
            try (PreparedStatement clubStmt = conn.prepareStatement(clubQuery)) {
                clubStmt.setString(1, clubName);
                ResultSet clubRs = clubStmt.executeQuery();

                if (clubRs.next()) {
                    int clubId = clubRs.getInt("club_id");

                    // club_id를 기준으로 부원 목록 조회
                    String memberQuery = "SELECT * FROM Member WHERE club_id = ?";
                    try (PreparedStatement memberStmt = conn.prepareStatement(memberQuery)) {
                        memberStmt.setInt(1, clubId);
                        ResultSet memberRs = memberStmt.executeQuery();

                        System.out.println("[" + clubName + "] 부원 목록");
                        while (memberRs.next()) {
                            System.out.println("학번: " + memberRs.getInt("student_id") + ", 이름: " + memberRs.getString("Mname"));
                        }
                    }
                } else {
                    System.out.println("입력한 동아리를 찾을 수 없습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("부원 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }

    // 8. 부원 정보 수정
    // 부원 정보 수정
    private static void updateMemberInfo(Scanner scanner) {
        System.out.print("학번를 입력하세요: ");
        int studentId = scanner.nextInt();
        scanner.nextLine(); // 버퍼 비우기

        // 현재 부원 정보 조회
        String query = "SELECT * FROM Member WHERE student_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, studentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                System.out.println("현재 부원 정보:");
                System.out.println("이름: " + rs.getString("Mname"));
                System.out.println("학년: " + rs.getInt("grade"));
                System.out.println("성별: " + rs.getString("gender"));
                System.out.println("상태: " + rs.getString("status"));
                System.out.println("역할: " + rs.getString("role"));
                System.out.println();

                // 수정할 정보 선택
                System.out.println("수정할 항목을 선택하세요:");
                System.out.println("1. 이름");
                System.out.println("2. 학년");
                System.out.println("3. 성별");
                System.out.println("4. 상태");
                System.out.println("5. 역할");
                int choice = scanner.nextInt();
                scanner.nextLine(); // 버퍼 비우기

                String updateQuery = null;
                String newValue = null;

                switch (choice) {
                    case 1: // 이름 수정
                        System.out.print("새로운 이름을 입력하세요: ");
                        newValue = scanner.nextLine();
                        updateQuery = "UPDATE Member SET Mname = ? WHERE student_id = ?";
                        break;
                    case 2: // 학년 수정
                        System.out.print("새로운 학년을 입력하세요: ");
                        newValue = String.valueOf(scanner.nextInt());
                        updateQuery = "UPDATE Member SET grade = ? WHERE student_id = ?";
                        break;
                    case 3: // 성별 수정
                        System.out.print("새로운 성별 (M/F)을 입력하세요: ");
                        newValue = scanner.nextLine();
                        updateQuery = "UPDATE Member SET gender = ? WHERE student_id = ?";
                        break;
                    case 4: // 상태 수정
                        System.out.print("새로운 상태를 입력하세요: ");
                        newValue = scanner.nextLine();
                        updateQuery = "UPDATE Member SET status = ? WHERE student_id = ?";
                        break;
                    case 5: // 역할 수정
                        System.out.print("새로운 역할을 입력하세요 (동아리장/부회장/일반부원): ");
                        newValue = scanner.nextLine();
                        updateQuery = "UPDATE Member SET role = ? WHERE student_id = ?";
                        break;
                    default:
                        System.out.println("잘못된 선택입니다.");
                        return;
                }

                // 업데이트 실행
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, newValue);
                    updateStmt.setInt(2, studentId);
                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("부원 정보가 성공적으로 수정되었습니다.");
                    } else {
                        System.out.println("부원 정보 수정에 실패했습니다.");
                    }
                }

            } else {
                System.out.println("해당 ID를 가진 부원을 찾을 수 없습니다.");
            }

        } catch (SQLException e) {
            System.out.println("부원 정보 수정 중 오류 발생: " + e.getMessage());
        }
    }


    // 9. 회비 납부
    private static void makePayment(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine(); // 동아리 이름 입력받기

        System.out.print("학번을 입력하세요: ");
        int studentId = scanner.nextInt();
        System.out.print("회비 금액을 입력하세요: ");
        double amount = scanner.nextDouble();
        // 현재 날짜를 가져오기 (yyyy-MM-dd 형식)
        java.sql.Date currentDate = new java.sql.Date(System.currentTimeMillis());

        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";

        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                // 회비 납부 기록 삽입
                try (PreparedStatement paymentStmt = conn.prepareStatement(
                        "INSERT INTO Payment (student_id, amount, payment_date, club_id) VALUES (?, ?, ?, ?)")) {
                    paymentStmt.setInt(1, studentId);
                    paymentStmt.setDouble(2, amount);
                    paymentStmt.setDate(3, currentDate); // 현재 날짜를 payment_date로 설정
                    paymentStmt.setInt(4, clubId); // club_id 추가
                    paymentStmt.executeUpdate();
                    System.out.println("회비가 납부되었습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("회비 납부 중 오류 발생: " + e.getMessage());
        }
    }



    // 10. 회비 납부 내역
    private static void viewPaymentHistory(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");



                try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM Payment WHERE student_id IN (SELECT student_id FROM Member WHERE club_id = ?)")) {
                    stmt.setInt(1, clubId);
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        System.out.println("학번: " + rs.getInt("student_id") + ", 금액: " + rs.getDouble("amount") + " 납부날짜" + rs.getDate("payment_date"));
                    }
                }
            }

        }
        catch (SQLException e) {
            System.out.println("회비 납부 내역 조회 중 오류 발생: " + e.getMessage());
        }

    }
    private static void listNotices(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                String noticeQuery = "SELECT * FROM Board WHERE club_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(noticeQuery)) {
                    stmt.setInt(1, clubId);

                    try (ResultSet rs = stmt.executeQuery()) {
                        System.out.println("공지사항 목록:");
                        while (rs.next()) {
                            System.out.println("공지사항 ID: " + rs.getInt("board_id"));
                            System.out.println("내용: " + rs.getString("content"));
                            System.out.println("---------------");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("공지사항 조회 중 오류 발생: " + e.getMessage());
        }
    }
    private static void postNotice(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");



                System.out.print("공지 내용을 입력하세요: ");
                String content = scanner.nextLine();

                String insertNoticeQuery = "INSERT INTO Board (club_id, content) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertNoticeQuery)) {
                    stmt.setInt(1, clubId);
                    stmt.setString(2, content);
                    stmt.executeUpdate();

                    System.out.println("공지사항이 게시되었습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("공지사항 게시 중 오류 발생: " + e.getMessage());
        }
    }
    private static void listEvents(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        // 동아리 ID 조회
        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                // 행사 목록 조회
                String eventQuery = "SELECT * FROM Event WHERE club_id = ?";
                try (PreparedStatement eventStmt = conn.prepareStatement(eventQuery)) {
                    eventStmt.setInt(1, clubId);
                    try (ResultSet rs = eventStmt.executeQuery()) {
                        while (rs.next()) {
                            System.out.println("행사 ID: " + rs.getInt("event_id"));
                            System.out.println("행사 이름: " + rs.getString("Ename"));
                            System.out.println("행사 날짜: " + rs.getDate("event_date"));
                            System.out.println("-----------");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("행사 목록 조회 중 오류 발생: " + e.getMessage());
        }
    }
    private static void participateInEvent(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        // 동아리 ID 조회
        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                // 행사 목록 조회
                System.out.println("참여할 행사 목록:");
                String eventQuery = "SELECT * FROM Event WHERE club_id = ?";
                try (PreparedStatement eventStmt = conn.prepareStatement(eventQuery)) {
                    eventStmt.setInt(1, clubId);
                    try (ResultSet rs = eventStmt.executeQuery()) {
                        while (rs.next()) {
                            System.out.println("행사 ID: " + rs.getInt("event_id") + ", 행사 이름: " + rs.getString("Ename"));
                        }
                    }
                }

                System.out.print("참여할 행사 ID를 입력하세요: ");
                int eventId = scanner.nextInt();
                scanner.nextLine();  // 버퍼 비우기

                // 부원 학번 입력
                System.out.print("부원 학번을 입력하세요: ");
                int studentId = scanner.nextInt();

                // 행사 참여 등록
                String insertParticipationQuery = "INSERT INTO Participate_in (event_id, student_id) VALUES (?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertParticipationQuery)) {
                    stmt.setInt(1, eventId);
                    stmt.setInt(2, studentId);
                    stmt.executeUpdate();
                    System.out.println("행사 참여가 등록되었습니다.");
                }

            }
        } catch (SQLException e) {
            System.out.println("행사 참여 중 오류 발생: " + e.getMessage());
        }
    }

    private static void createEvent(Scanner scanner) {
        System.out.print("동아리 이름을 입력하세요: ");
        String clubName = scanner.nextLine();

        // 동아리 ID 조회
        String clubIdQuery = "SELECT club_id FROM Club WHERE Cname = ?";
        try (PreparedStatement clubStmt = conn.prepareStatement(clubIdQuery)) {
            clubStmt.setString(1, clubName);

            try (ResultSet clubRs = clubStmt.executeQuery()) {
                if (!clubRs.next()) {
                    System.out.println("입력한 동아리가 존재하지 않습니다.");
                    return;
                }

                int clubId = clubRs.getInt("club_id");

                // 행사 정보 입력
                System.out.print("행사 이름을 입력하세요: ");
                String eventName = scanner.nextLine();
                System.out.print("행사 날짜를 입력하세요 (YYYY-MM-DD): ");
                String eventDate = scanner.nextLine();

                String insertEventQuery = "INSERT INTO Event (club_id, Ename, event_date) VALUES (?, ?, ?)";
                try (PreparedStatement eventStmt = conn.prepareStatement(insertEventQuery)) {
                    eventStmt.setInt(1, clubId);
                    eventStmt.setString(2, eventName);
                    eventStmt.setString(3, eventDate);
                    eventStmt.executeUpdate();
                    System.out.println("행사가 성공적으로 등록되었습니다.");
                }
            }
        } catch (SQLException e) {
            System.out.println("행사 등록 중 오류 발생: " + e.getMessage());
        }
    }



    // 메뉴 출력 함수
    public static void displayMenu() {
        System.out.println("\n===== 동아리 관리 시스템 =====");
        System.out.println("1. 동아리 개설");
        System.out.println("2. 동아리 정보 수정");
        System.out.println("3. 동아리 실적 내역 확인");
        System.out.println("4. 동아리 실적 내역 등록");
        System.out.println("5. 동아리 실적 내역 수정");
        System.out.println("6. 동아리 목록 조회");
        System.out.println("7. 부원 신청");
        System.out.println("8. 부원 목록 조회");
        System.out.println("9. 부원 정보 수정");
        System.out.println("10. 회비 납부");
        System.out.println("11. 회비 납부 내역 조회");
        System.out.println("12. 공지사항 목록");
        System.out.println("13. 공지사항 등록");
        System.out.println("14. 행사 목록 보기");
        System.out.println("15. 행사 참여하기");
        System.out.println("16. 행사 개최");
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
                    case 4: // 실적 내용 등록
                        registerClubPerformance(scanner);
                        break;
                    case 5:
                        updatePerformance(scanner);
                        break;
                    case 6:
                        listClubs();
                        break;
                    case 7:
                        applyForMembership(scanner);
                        break;
                    case 8:
                        listMembers(scanner);
                        break;
                    case 9:
                        updateMemberInfo(scanner);
                        break;
                    case 10:
                        makePayment(scanner);
                        break;
                    case 11:
                        viewPaymentHistory(scanner);
                        break;
                    case 12:
                        listNotices(scanner);
                        break;
                    case 13:
                        postNotice(scanner);
                        break;

                    case 14:
                        listEvents(scanner); // 행사 목록 보기
                        break;
                    case 15:
                        participateInEvent(scanner); // 행사 참여하기
                        break;
                    case 16:
                        createEvent(scanner); // 행사 개최
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
