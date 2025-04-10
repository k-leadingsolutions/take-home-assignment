import dto.BettingGame;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import service.BettingService;
import service.*;

import java.math.BigDecimal;
import java.util.Scanner;

@SpringBootApplication()
public class BettingGameApplication {
    public static void main(String[] args) {
        //read inputs
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter Betting file name:");
        String filename = scanner.nextLine();

        System.out.println("Enter Betting amount:");
        BigDecimal betting_amount = scanner.nextBigDecimal();

        // inject service for processing file and betting
        BettingService bettingService = new BettingServiceImpl();

        // process file and create betting object
        BettingGame bettingGame = bettingService.processFile(filename);

        // bet
        String result = bettingService.bet(betting_amount, bettingGame);

        scanner.close();

        System.out.println("Bet Result: " + result);
    }
}
