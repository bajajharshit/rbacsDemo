package perfios.rbacs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import perfios.rbacs.Model.Users.User;

import java.util.HashMap;
import java.util.Set;

@SpringBootApplication
public class RbacsApplication {

	public static void main(String[] args) {
		System.out.println("this is working fine");
		SpringApplication.run(RbacsApplication.class, args);
	}




	public static void printMap(HashMap<String,Integer> hm){
		System.out.println(hm);
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

	public static void printSet(Set<Integer> a){
		System.out.println(a);
	}

}
