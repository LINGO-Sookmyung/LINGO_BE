package Sookmyung.Lingo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	public static final String version = "v1.0.0";

	@GetMapping("/")
	public String home(){
		return "cicd with docker. version"+ version;
	}

}
