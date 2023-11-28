# hisnet-login

설명: 한동대학교 hisnet 로그인 API 호출 라이브러리

# 사용 방법

1. 클론
2. yml 파일 위치 시키기 (resources 폴더 아래)
3. 빌드
4. out/artifacts/hisnet_login_jar 폴더 내의 jar 파일 확인
5. 타 프로젝트에 외부 라이브러리 추가 - 빌드한 jar 파일 선택

# 사용 예시 코드 

~~~
import hisnet.login.AuthService;
import hisnet.login.LoginForm;

import java.util.Map;

public class Main {

    private static AuthService authService;

    public static void main(String[] args) {
        authService = new AuthService();
        LoginForm loginForm = new LoginForm(
            "22000630",
            "비일번호오옹!"
        );
        Map<String, Object> result = authService.hisnetLogin(loginForm);

        val studentForm = StudentForm(
                sid = result["user_number"].toString(),
                name = result["user_name"].toString(),
                department = result["dept_name"].toString(),
                major1 = result["major1_name"].toString(),
                major2 = result["major2_name"].toString(),
                year = result["grade"].toString().toInt(),
                semesterCount = result["reg_semester"].toString().toInt(),
        )
    }
}
~~~

***아래의 정보 참고해서 Map에서 가져와서 사용하기***


user_name:	    사용자 이름	

user_number:	  사용자 번호(직번 or 학번)	

dept_name:	    사용자 소속 학부 혹은 부서명	

grade:	        현재 학년	교직원인 경우엔 null 값

reg_semester:	현재 등록학기	교직원인 경우엔 0 값

major1_name:	  1전공명	교직원인 경우엔 null 값

major2_name:	  2전공명	교직원인 경우엔 null 값

email:        이메일	
