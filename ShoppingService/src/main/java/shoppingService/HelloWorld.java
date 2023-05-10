package shoppingService;

import org.springframework.web.bind.annotation.*;

@RestController
public class HelloWorld {

    @RequestMapping("/hello")
    @ExceptionHandler(Exception.class)
    public String show() {
        return "Hello world !!!!";
    }

    @RequestMapping("/bonjour")
    public String bonjour(@RequestParam(value = "name", defaultValue = "Jean") String name) {
        return "Hello "+ name;
    }

}
