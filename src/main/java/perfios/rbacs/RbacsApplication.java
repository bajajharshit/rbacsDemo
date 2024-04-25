package perfios.rbacs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import perfios.rbacs.Model.Users.User;

@SpringBootApplication
public class RbacsApplication {

	public static void main(String[] args) {
		System.out.println("this is working fine");
		SpringApplication.run(RbacsApplication.class, args);
	}




	public static void printString(String str){
		System.out.println(str);
	}

	public static void check2(User user){
		System.out.println(user);
	}

	public static void check3(int a, int b){
		System.out.println(a + "  " + b);
	}


}
