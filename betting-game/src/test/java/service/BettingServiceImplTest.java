package service;

import dto.BettingGame;
import dto.Output;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

class BettingServiceImplTest {

    BettingService service = new BettingServiceImpl();

    @BeforeEach
    void init(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processFileTest(){
        BettingGame bettingGame = service.processFile("config.json");
        Assertions.assertNotEquals(bettingGame, null);
        Assertions.assertEquals(bettingGame.getProbabilities().getStandardSymbols().size() , 9);
        Assertions.assertEquals(bettingGame.getProbabilities().getStandardSymbols().get(0).getSymbols().get("A") , 1);
    }

    @Test
    void processFileTest_throwsException(){
        Assertions.assertThrows(NullPointerException.class, () -> service.processFile("config12.json"));
    }

    @Test
    void betTest(){
        Output output = new Output();
        output.setMatrix(List.of(List.of("[[\"C\",\"A\",\"B\",\"D\"],[\"E\",\"B\",\"A\",\"C\"],[\"D\",\"B\",\"5x\",\"B\"],[\"C\",\"C\",\"B\",\"C\"]]")));
        output.setAppliedBonusSymbol("10*");
        BettingGame bettingGame = service.processFile("config.json");
        Assertions.assertNotEquals(output.toString(),service.bet(BigDecimal.valueOf(100L),bettingGame));
    }
}