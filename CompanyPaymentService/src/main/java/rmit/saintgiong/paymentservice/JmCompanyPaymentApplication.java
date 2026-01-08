package rmit.saintgiong.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class JmCompanyPaymentApplication {

	public static void main(String[] args) throws InterruptedException {
		// Life hacks
		Thread.sleep(3_600_000);
		SpringApplication.run(JmCompanyPaymentApplication.class, args);
	}

}



