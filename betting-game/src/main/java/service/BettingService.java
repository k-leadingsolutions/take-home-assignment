package service;

import dto.BettingGame;

import java.math.BigDecimal;

public interface BettingService {

    BettingGame processFile(String filename);

    String bet(BigDecimal bet_amount, BettingGame bettingGame);

}
