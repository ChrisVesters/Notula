package com.cvesters.notula.meeting;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class MeetingWebSocket {

	@MessageMapping("/chat")
	@SendTo("/topic/messages")
	public String send(final String message) throws Exception {
		return "Echo: " + message;
	}

}

// @MessageMapping("/chat")
// @SendTo("/topic/messages")
// public OutputMessage send(Message message) throws Exception {
// String time = new SimpleDateFormat("HH:mm").format(new Date());
// return new OutputMessage(message.getFrom(), message.getText(), time);
// }

// package com.example.messagingstompwebsocket;

// import org.springframework.messaging.handler.annotation.MessageMapping;
// import org.springframework.messaging.handler.annotation.SendTo;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.util.HtmlUtils;

// @Controller
// public class GreetingController {

// @MessageMapping("/hello")
// @SendTo("/topic/greetings")
// public Greeting greeting(HelloMessage message) throws Exception {
// Thread.sleep(1000); // simulated delay
// return new Greeting("Hello, " + HtmlUtils.htmlEscape(message.getName()) +
// "!");
// }

// }
